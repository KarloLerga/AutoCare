package hr.unizd.autocare.tools;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

/** Streaming UTF-8 CSV/GZIP reader and integrity checks for developer-only seed files. */
public final class SeedFiles {
    private SeedFiles() { }
    public static long read(Path file, Consumer<Map<String,String>> consumer) throws IOException {
        InputStream raw=Files.newInputStream(file);
        try(InputStream in=file.toString().endsWith(".gz")?new GZIPInputStream(raw):raw;
            CsvReader csv=new CsvReader(new InputStreamReader(in,StandardCharsets.UTF_8))) {
            List<String> header=csv.readRow();
            if(header==null||new HashSet<>(header).size()!=header.size())throw new IOException("Prazan ili nevaljan CSV header: "+file.getFileName());
            List<String> row; long count=0;
            while((row=csv.readRow())!=null) {
                if(row.size()!=header.size())throw new IOException("Nevaljan broj stupaca, redak "+(count+2)+": "+file.getFileName());
                Map<String,String> m=new LinkedHashMap<>();
                for(int i=0;i<header.size();i++)m.put(header.get(i),row.get(i));
                consumer.accept(m); count++;
            }
            return count;
        }
    }
    public static void verifyManifest(Path dir) throws IOException {
        Path root=dir.toAbsolutePath().normalize();
        Path manifest=root.resolve("checksums.sha256");
        if(!Files.isRegularFile(manifest))throw new IOException("Nedostaje checksums.sha256.");
        for(String line:Files.readAllLines(manifest,StandardCharsets.UTF_8)) {
            if(line.isBlank())continue;
            if(line.length()<67||!line.substring(0,64).matches("[0-9a-f]{64}"))throw new IOException("Nevaljan manifest.");
            Path path=root.resolve(line.substring(66)).normalize();
            if(!path.startsWith(root)||!Files.isRegularFile(path)||Files.isSymbolicLink(path))throw new IOException("Nevaljana putanja u manifestu.");
            if(!sha256(path).equals(line.substring(0,64)))throw new IOException("SHA256 se ne podudara: "+path.getFileName());
        }
    }
    public static String sha256(Path file) throws IOException {
        try(InputStream in=Files.newInputStream(file)) {
            MessageDigest digest=MessageDigest.getInstance("SHA-256");
            byte[] bytes=new byte[65536]; int n; while((n=in.read(bytes))!=-1)digest.update(bytes,0,n);
            return HexFormat.of().formatHex(digest.digest());
        } catch(NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }
    public static String text(Map<String,String> row,String key,int max,boolean required) {
        if(!row.containsKey(key))throw new IllegalArgumentException("Nedostaje CSV stupac: "+key);
        String value=row.get(key); if(value==null||value.isBlank()) {
            if(required)throw new IllegalArgumentException("Obavezan stupac je prazan: "+key); return null;
        }
        if(value.length()>max)throw new IllegalArgumentException("Predugo polje: "+key);
        return value;
    }
    public static Integer integer(Map<String,String> row,String key,int min,int max) {
        String v=text(row,key,32,false); if(v==null)return null;
        int n=Integer.parseInt(v); if(n<min||n>max)throw new IllegalArgumentException("Nevaljan broj: "+key); return n;
    }
    public static BigDecimal price(Map<String,String> row,String key) {
        String v=text(row,key,40,false); if(v==null)return null;
        BigDecimal n=new BigDecimal(v).setScale(2,java.math.RoundingMode.UNNECESSARY);
        if(n.signum()<0||n.compareTo(new BigDecimal("9999999.99"))>0||n.remainder(BigDecimal.TEN).signum()!=0)
            throw new IllegalArgumentException("Modelirana cijena mora biti nenegativna desetica.");
        return n;
    }
}
