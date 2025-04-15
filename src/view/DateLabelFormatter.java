package view;

import javax.swing.JFormattedTextField;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {

    private String datePattern = "yyyy-MM-dd";
    private SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);

    @Override
    public Object stringToValue(String text) throws ParseException {
        return dateFormat.parse(text);
    }

    @Override
    public String valueToString(Object value) {
        if (value != null) {
            if (value instanceof GregorianCalendar) {
                // Convert GregorianCalendar to Date
                Date date = ((GregorianCalendar) value).getTime();
                return dateFormat.format(date);
            }
            return dateFormat.format((Date) value);
        }
        return "";
    }
}
