package hr.unizd.autocare.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Provjere koje pripadaju developerskom setup artefaktu, ne runtime JAR-u. */
class SetupDataTest {

  @Test
  void csvReaderHandlesRfc4180Fields() throws IOException {
    try (CsvReader reader =
        new CsvReader(new StringReader("\ufeffa,b\r\n\"x,y\",\"one\nline\"\r\n\"a\"\"b\",c"))) {
      assertEquals(List.of("a", "b"), reader.readRow());
      assertEquals(List.of("x,y", "one\nline"), reader.readRow());
      assertEquals(List.of("a\"b", "c"), reader.readRow());
      assertNull(reader.readRow());
    }
  }

  @Test
  void csvReaderRejectsUnclosedQuotes() {
    assertThrows(
        IOException.class,
        () -> {
          try (CsvReader reader = new CsvReader(new StringReader("\"not closed"))) {
            reader.readRow();
          }
        });
  }

  @Test
  void seedPriceKeepsTheModelledTenEuroBoundary() {
    assertEquals(new BigDecimal("280.00"), SeedFiles.price(Map.of("p", "280.00"), "p"));
    assertNull(SeedFiles.price(Map.of("p", ""), "p"));
    assertThrows(IllegalArgumentException.class, () -> SeedFiles.price(Map.of("p", "284.00"), "p"));
  }
}
