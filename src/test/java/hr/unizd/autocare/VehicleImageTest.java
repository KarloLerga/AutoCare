package hr.unizd.autocare;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.unizd.autocare.view.components.VehicleImage;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class VehicleImageTest {
  @Test
  void packagedIllustrationsAreReadable() throws Exception {
    for (String path :
        new String[] {
          "/images/vehicles/generic-vehicle.jpg",
          "/images/vehicles/generic-electric.jpg",
          "/images/vehicles/fallback-car.jpg"
        }) {
      try (InputStream in = VehicleImage.class.getResourceAsStream(path)) {
        assertNotNull(in, path);
        BufferedImage image = ImageIO.read(in);
        assertNotNull(image, path);
        assertTrue(image.getWidth() <= 960 && image.getHeight() <= 600, path);
      }
    }
  }

  @Test
  void categoryFallbacksLoadWithoutNetwork() {
    VehicleImage image = new VehicleImage();
    image.showVehicle("/images/vehicles/fallback-car.jpg", "Gasoline");
    assertNotNull(image.getIcon());
    assertTrue(image.getToolTipText().startsWith("Generička ilustracija"));
    image.showVehicle("/images/vehicles/fallback-car.jpg", "Electric");
    assertNotNull(image.getIcon());
    assertTrue(image.getToolTipText().startsWith("Generička ilustracija"));
  }
}
