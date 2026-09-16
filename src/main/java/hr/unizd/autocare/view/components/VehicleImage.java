package hr.unizd.autocare.view.components;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
/** Samo mali lokalni resursi; skaliranje cuva omjer stranica. Nema HTTP poziva. */
public final class VehicleImage extends JLabel {
    private static final String FALLBACK="/images/vehicles/fallback-car.jpg";
    private static final String GENERIC="/images/vehicles/generic-vehicle.jpg";
    private static final String ELECTRIC="/images/vehicles/generic-electric.jpg";
    private static final Map<String,ImageIcon> CACHE=new LinkedHashMap<>(32,0.75f,true) {
        protected boolean removeEldestEntry(Map.Entry<String,ImageIcon> e) { return size()>64; }
    };
    public VehicleImage() {
        super("Slika nije dostupna",SwingConstants.CENTER);
        setPreferredSize(new Dimension(150,90)); setOpaque(true);
        setBackground(new Color(0xEAF0F5)); setForeground(Ui.MUTED);
    }
    public void showPath(String path) {
        showVehicle(path, null);
    }
    /**
     * Shows an approved local path, or a generated category illustration when
     * the catalog still contains the neutral fallback. The illustration is
     * deliberately not presented as an exact make/model match.
     */
    public void showVehicle(String path, String fuelType) {
        String categoryFallback=isElectric(fuelType)?ELECTRIC:GENERIC;
        String key=path!=null&&path.startsWith("/images/vehicles/")&&!path.contains("..")&&!path.equals(FALLBACK)?path:categoryFallback;
        ImageIcon icon=load(key); if(icon==null)icon=load(categoryFallback); if(icon==null)icon=load(FALLBACK);
        setIcon(icon); setText(icon==null?"Slika nije dostupna":"");
        setToolTipText(key.equals(GENERIC)||key.equals(ELECTRIC)?"Generička ilustracija — nije fotografija točnog modela.":null);
    }
    private static boolean isElectric(String fuelType) {
        return fuelType!=null&&fuelType.toLowerCase(Locale.ROOT).contains("electric");
    }
    private static synchronized ImageIcon load(String path) {
        if(CACHE.containsKey(path))return CACHE.get(path);
        URL url=VehicleImage.class.getResource(path); if(url==null)return null;
        try {
            BufferedImage image=ImageIO.read(url); if(image==null)return null;
            double ratio=Math.min(140.0/image.getWidth(),80.0/image.getHeight());
            int w=Math.max(1,(int)(image.getWidth()*ratio)),h=Math.max(1,(int)(image.getHeight()*ratio));
            BufferedImage scaled=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
            Graphics2D g=scaled.createGraphics();
            try { g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR); g.drawImage(image,0,0,w,h,null); }
            finally { g.dispose(); }
            ImageIcon icon=new ImageIcon(scaled); CACHE.put(path,icon); return icon;
        } catch(java.io.IOException ex) { return null; }
    }
}
