package brokcu.ca.bf22wk.a2;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.DebugUtils;
import android.util.Log;
import android.widget.Toast;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ManageDatabase {

    private static final String DATABASE_NAME = "3P97A2";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "Meetings";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_CONTACT = "contact";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TIME = "time";

    private final SQLiteDatabase database;

    public ManageDatabase(Context context) {
        DataHelper dbHelper = new DataHelper(context);
        database = dbHelper.getWritableDatabase();
    }

    // Add meeting to database
    public void AddMeeting(Event event) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, event.getName());
        values.put(COLUMN_DATE, event.getDate().toString());
        int [] time = event.getTime();
        values.put(COLUMN_TIME, CalendarUtils.formattedTime(time[0], time[1]));
        values.put(COLUMN_CONTACT, event.getMeetingContact());


        long result = database.insert(TABLE_NAME, null, values);
        if (result == -1) {
            System.out.println("INSERT FAILED");
            // Insert failed
            // Handle error
        } else {
            System.out.println("ADDED");
            System.out.println(event.getName());
            System.out.println(event.getDate().toString());
            System.out.println(CalendarUtils.formattedTime(time[0], time[1]));
            System.out.println(event.getMeetingContact());
            // Insert successful
        }
    }

    // Deletes specific meeting
    public void RemoveMeeting(Event event) {
        database.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{String.valueOf(event.getID())});
    }

    // Deletes all meetings
    public void RemoveAllMeetings() {
        database.delete(TABLE_NAME, null, null);
    }

    // Update meeting in Database
    public void UpdateMeeting(Event event) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, event.getName());
        values.put(COLUMN_DATE, event.getDate().toString());
        int [] time = event.getTime();
        values.put(COLUMN_TIME, CalendarUtils.formattedTime(time[0], time[1]));
        values.put(COLUMN_CONTACT, event.getMeetingContact());

        database.update(TABLE_NAME, values, COLUMN_ID + "=?", 
                new String[]{String.valueOf(event.getID())});
    }

    public ArrayList<Event> GetAllMeetings() {
        ArrayList<Event> eventList = new ArrayList<>();
        Cursor cursor = database.query(TABLE_NAME, null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex(COLUMN_ID);
                int nameIndex = cursor.getColumnIndex(COLUMN_NAME);
                int contactIndex = cursor.getColumnIndex(COLUMN_CONTACT);
                int dateIndex = cursor.getColumnIndex(COLUMN_DATE);
                int timeIndex = cursor.getColumnIndex(COLUMN_TIME);

                int id = cursor.getInt(idIndex);
                String name = cursor.getString(nameIndex);
                String contact = cursor.getString(contactIndex);
                String dateString = cursor.getString(dateIndex);
                String timeString = cursor.getString(timeIndex);
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault());

                Date date;
                try {
                    date = dateFormat.parse(dateString);
                } catch (ParseException e) {
                    e.printStackTrace();
                    continue;
                }

                String[] time = timeString.split(":");

                Log.d("MeetingData", "ID: " + id + ", Name: " + name + ", Contact: " + contact + ", Date: "  + ", Time: " + time);

                //create a Meeting object and add it to the list
                Event event = new Event(name, date, Integer.parseInt(time[0]),
                        Integer.parseInt(time[1]), contact);
                eventList.add(event);

                event.setID(id);

            } while (cursor.moveToNext());

            cursor.close();
        }

        return eventList;
    }

    private static class DataHelper extends SQLiteOpenHelper {

        public DataHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String createTableQuery = "CREATE TABLE " + TABLE_NAME + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_CONTACT + " TEXT, " +
                    COLUMN_DATE + " TEXT, " +
                    COLUMN_TIME + " TEXT)";
            db.execSQL(createTableQuery);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}

