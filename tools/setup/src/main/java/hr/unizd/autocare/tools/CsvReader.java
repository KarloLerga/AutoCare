package hr.unizd.autocare.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/** Mali RFC-4180 citac za developerski uvoz: zarezi, CRLF, navodnici i novi red unutar polja. */
public final class CsvReader implements AutoCloseable {
  private final PushbackReader reader;
  private boolean first = true;

  public CsvReader(Reader reader) {
    this.reader = new PushbackReader(new BufferedReader(reader), 2);
  }

  public List<String> readRow() throws IOException {
    List<String> fields = new ArrayList<>();
    StringBuilder value = new StringBuilder();
    boolean quoted = false, closed = false, seen = false;
    while (true) {
      if (value.length() > 1000000) {
        throw new IOException("CSV polje je preveliko.");
      }
      int ch = reader.read();
      if (first) {
        first = false;
        if (ch == 0xFEFF) {
          ch = reader.read();
        }
      }
      if (ch == -1) {
        if (quoted) {
          throw new IOException("Nedovrseno navodno polje u CSV-u.");
        }
        if (!seen && fields.isEmpty()) {
          return null;
        }
        fields.add(value.toString());
        return fields;
      }
      seen = true;
      char c = (char) ch;
      if (quoted) {
        if (c == '"') {
          int next = reader.read();
          if (next == '"') {
            value.append('"');
          } else {
            quoted = false;
            closed = true;
            if (next != -1) {
              reader.unread(next);
            }
          }
        } else {
          value.append(c);
        }
        continue;
      }
      if (c == ',') {
        fields.add(value.toString());
        value.setLength(0);
        closed = false;
        continue;
      }
      if (c == '\n' || c == '\r') {
        if (c == '\r') {
          int next = reader.read();
          if (next != '\n' && next != -1) {
            reader.unread(next);
          }
        }
        fields.add(value.toString());
        return fields;
      }
      if (c == '"') {
        if (value.length() != 0 || closed) {
          throw new IOException("Navodnik na nevaljanom mjestu u CSV-u.");
        }
        quoted = true;
        continue;
      }
      if (closed) {
        throw new IOException("Tekst nakon zatvorenog CSV navodnika.");
      }
      value.append(c);
    }
  }

  @Override
  public void close() throws IOException {
    reader.close();
  }
}
