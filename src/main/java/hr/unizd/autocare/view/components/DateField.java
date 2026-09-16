package hr.unizd.autocare.view.components;

import java.text.ParseException;
import java.time.LocalDate;
import javax.swing.JFormattedTextField;
import javax.swing.text.DefaultFormatterFactory;

/** Strogo formatiran datum bez dodatne date-picker biblioteke. */
public final class DateField extends JFormattedTextField {
  public DateField(LocalDate initial) {
    setFormatterFactory(
        new DefaultFormatterFactory(
            new AbstractFormatter() {
              public Object stringToValue(String text) throws ParseException {
                try {
                  return LocalDate.parse(text, Ui.DATE);
                } catch (Exception ex) {
                  throw new ParseException("Datum: dd.MM.gggg.", 0);
                }
              }

              public String valueToString(Object value) {
                return value == null ? "" : ((LocalDate) value).format(Ui.DATE);
              }
            }));
    setValue(initial);
    setColumns(12);
    setToolTipText("Datum u obliku 15.09.2026.");
    setFocusLostBehavior(JFormattedTextField.PERSIST);
  }

  public LocalDate date() {
    try {
      commitEdit();
      return (LocalDate) getValue();
    } catch (ParseException ex) {
      throw new IllegalArgumentException("Datum mora biti valjan, npr. 15.09.2026.");
    }
  }
}
