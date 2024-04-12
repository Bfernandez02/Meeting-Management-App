package brokcu.ca.bf22wk.a2;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CalendarUtils {

    // This variable keeps track of what date the user chooses.
    public static Date selectedDate;

    // load up date
    public static Date todayDate;

    // contact selected: used for creating events
    public static Contact contactSelected;

    // Whether events are being created
    public static boolean creatingEvent;

    // Selected event, used for specific event activity
    public static Event selectedEvent;

    // Formats date into proper string
    public static String formatDate(Date date){
        return date.toString().substring(0,10) + ", " + date.toString().substring(24);
    }

    public static String formattedTime(int hour, int minute){

        return String.format(
                Locale.getDefault(), "%02d:%02d", hour, minute);
    }
}
