package hr.unizd.autocare.tools;
import hr.unizd.autocare.app.SqlSettings;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.io.InputStream;
import java.security.MessageDigest;
/** Posljednja faza: odobreni lokalni resursi moraju vec biti u izgradjenom JAR-u. */
public final class ImagePathTool {
    private ImagePathTool() { }
    public static void run(String[] args) {
        if(args.length<2)throw new IllegalArgumentException("import-images approved_image_updates.csv [--apply] [--replace-existing]");
        boolean apply=Arrays.asList(args).contains("--apply"), replace=Arrays.asList(args).contains("--replace-existing");
        List<Map<String,String>> rows=new ArrayList<>(); Set<String> seen=new HashSet<>(); Map<String,String> hashes=new HashMap<>();
        try {
            SeedFiles.read(Path.of(args[1]),r->{
                String code=SeedFiles.text(r,"variant_code",80,true),path=SeedFiles.text(r,"image_path",255,true),sha=SeedFiles.text(r,"image_sha256",64,true);
                if(!seen.add(code))throw new IllegalArgumentException("Ponovljen variant_code: "+code);
                if(!path.matches("/images/vehicles/[a-zA-Z0-9_-]+\\.jpg")||!sha.matches("[0-9a-f]{64}"))throw new IllegalArgumentException("Neispravna putanja/hash slike.");
                String actual=hashes.get(path);
                if(actual==null) {
                    try(InputStream in=ImagePathTool.class.getResourceAsStream(path)) {
                        if(in==null)throw new IllegalArgumentException("Resurs nije u JAR-u: "+path+". Ponovno pokrenite Maven package.");
                        actual=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(in.readAllBytes())); hashes.put(path,actual);
                    } catch(Exception e) { throw new IllegalArgumentException("Ne mogu provjeriti resurs "+path,e); }
                }
                if(!sha.equals(actual))throw new IllegalArgumentException("Hash resursa se razlikuje: "+path);
                rows.add(Map.copyOf(r));
            });
            System.out.println("Provjereno mapiranja slika: "+rows.size()); if(!apply)return;
            SqlSettings cfg=SqlSettings.environment(false);
            if(!cfg.database().equals(System.getenv("AUTOCARE_SEED_TARGET")))throw new IllegalArgumentException("AUTOCARE_SEED_TARGET mora tocno potvrditi ime baze.");
            try(Connection c=cfg.connect(false)) {
                c.setAutoCommit(false);
                try(PreparedStatement find=c.prepareStatement("SELECT image_path FROM dbo.vehicle_variant WITH (UPDLOCK,HOLDLOCK) WHERE code=?");
                    PreparedStatement update=c.prepareStatement("UPDATE dbo.vehicle_variant SET image_path=? WHERE code=?")) {
                    int count=0;
                    for(Map<String,String> r:rows) {
                        find.setString(1,r.get("variant_code")); String old;
                        try(ResultSet rs=find.executeQuery()) { if(!rs.next())throw new IllegalArgumentException("Nepoznata varijanta: "+r.get("variant_code")); old=rs.getString(1); }
                        if(!replace&&old!=null&&!old.endsWith("/fallback-car.jpg")&&!old.equals(r.get("image_path")))continue;
                        update.setString(1,r.get("image_path"));update.setString(2,r.get("variant_code"));update.executeUpdate();count++;
                    }
                    c.commit();System.out.println("Postavljeno imagePath: "+count);
                } catch(Exception ex) { try{c.rollback();}catch(SQLException rollback){ex.addSuppressed(rollback);}throw ex; }
            }
        } catch(Exception ex) { throw new IllegalStateException("Import slika nije zavrsen. Ne pretpostavljajte ishod izgubljenog commita; isti CSV moze se provjeriti i ponoviti.",ex); }
    }
}
