package brokcu.ca.bf22wk.a2;

import static brokcu.ca.bf22wk.a2.MainActivity.sharedPreferencesEditor;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CreateEventActivity extends AppCompatActivity implements RecyclerViewInterface {
    private EditText eventNameET;
    private TextView chooseDateTV;
    private TextView selectedTimeTV;
    int hour, minute;
    CalendarView calendarView;
    Calendar calendar;

    RecyclerView contactsList;

    Contact meetingContact;
    ManageDatabase DB;

    // This is used so that the participant info is loaded after the ManageContact activity finishes
    public ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    loadParticipantInfo();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        CalendarUtils.creatingEvent = true;
        setContentView(R.layout.activity_create_event);
        initWidgets();
        chooseDateTV.setText(String.format("Selected Date: %s",
                CalendarUtils.formatDate(CalendarUtils.selectedDate)));
    }

    // Saves states for all values. Necessary for landscape functionality
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("selectedTime", selectedTimeTV.getText().toString());
        outState.putInt("hour", hour);
        outState.putInt("minute", minute);

        if (meetingContact != null){
            outState.putString("contact", meetingContact.formatContactString());
        }else{
            outState.putString("contact", "null");
        }
    }

    // Restores saved states for all values. Necessary for landscape functionality
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        hour = savedInstanceState.getInt("hour");
        minute = savedInstanceState.getInt("minute");
        selectedTimeTV.setText(savedInstanceState.getString("selectedTime"));

        String temp = savedInstanceState.getString("contact");

        assert temp != null;
        if (!temp.equals("null")) {
            String[] contactStrings = temp.split(",");

            Contact c;

            // This is string formatting used to grab name, number and email. Since they are not
            // required, this needs to be checked.
            switch (contactStrings.length) {

                case 1:
                    c = new Contact("1", contactStrings[0], "", "");
                    break;
                case 2:
                    c = new Contact("1", contactStrings[0], contactStrings[1], "");
                    break;
                case 3:
                    c = new Contact("1", contactStrings[0], contactStrings[2], contactStrings[2]);
                    break;
                default:
                    c = new Contact("1", "", "", "");
                    break;
            }

            meetingContact = c;
            loadParticipantInfo();
        }

    }

    private void initWidgets()
    {
        calendarView = findViewById(R.id.createCalendarView);
        contactsList = findViewById(R.id.contactsList);
        eventNameET = findViewById(R.id.eventNameET);
        selectedTimeTV = findViewById(R.id.selectedTimeTV);
        chooseDateTV = findViewById(R.id.chooseDateTV);
        calendar = Calendar.getInstance();
        calendarView.setDate(CalendarUtils.selectedDate.getTime());

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // If the selected date is in the past give user an error and bring date back to today
        if (CalendarUtils.selectedDate.before(CalendarUtils.todayDate)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateEventActivity.this);
            builder.setTitle("Error");
            builder.setMessage("Sorry, you cannot add meeting in the past");
            builder.setPositiveButton("OK",null);
            builder.show();
            CalendarUtils.selectedDate = CalendarUtils.todayDate;
            calendarView.setDate(CalendarUtils.selectedDate.getTime());
        }


        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar c = Calendar.getInstance();
            c.set(year, month, dayOfMonth, 0, 0);
            Date temp = c.getTime();

            // If the selected date is in the past give user an error and bring date back to today
            if (temp.before(CalendarUtils.todayDate)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CreateEventActivity.this);
                builder.setTitle("Error");
                builder.setMessage("Sorry, you cannot add meeting in the past");
                builder.setPositiveButton("OK",null);
                builder.show();

                calendarView.setDate(CalendarUtils.selectedDate.getTime());
                return;
            }

            // Set selected date for meeting
            setDate(dayOfMonth, month, year);
            try {
                CalendarUtils.selectedDate = formatter.parse(formatter.format(
                        new Date(calendarView.getDate())));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            assert CalendarUtils.selectedDate != null;
            updateDateDisplay("Selected Date: " + CalendarUtils.formatDate(
                    CalendarUtils.selectedDate));
        });
    }

    private void updateDateDisplay(String date){
        chooseDateTV.setText(date);
    }

    private void setDate (int day, int month, int year){
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        long milli = calendar.getTimeInMillis();
        calendarView.setDate(milli);
        calendarView.setSelected(true);
    }

    // Launch ManageContacts activity to choose a contact from contacts app
    public void selectParticipant(View view) {

        mStartForResult.launch(new Intent(this, ManageContacts.class));

    }

    // Load contact information
    private void loadParticipantInfo() {
        meetingContact = CalendarUtils.contactSelected;

        ArrayList<Contact> contact= new ArrayList<>();
        contact.add(meetingContact);

        ContactAdapter eventAdapter = new ContactAdapter(getApplicationContext(), contact, this);
        contactsList.setAdapter(eventAdapter);
        contactsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }

    // Time selection popup using built in timepickerdialog.
    public void selectTime(View view)
    {
        TimePickerDialog.OnTimeSetListener onTimeSetListener = (timePicker, selectedHour, selectedMinute) -> {
            hour = selectedHour;
            minute = selectedMinute;
            selectedTimeTV.setText(String.format("Selected Time: %s",
                    CalendarUtils.formattedTime(hour, minute) ));
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                /*style,*/ onTimeSetListener, hour, minute, true);

        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();
    }

    // Save the event
    public void saveEventAction(View view)
    {

        String eventName = eventNameET.getText().toString();

        // Makes sure all required fields are filled in.
        if (eventName.equals("") || CalendarUtils.contactSelected == null ){
            Toast.makeText(this, "ERROR! Make sure to fill out all fields before continuing",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Create new event object and add it to the event list
        Event newEvent = new Event(eventName, CalendarUtils.selectedDate, hour, minute,
                meetingContact.formatContactString());
        Event.eventsList.add(newEvent);

        CalendarUtils.creatingEvent = false;
        CalendarUtils.contactSelected = null;

        // add event.
        DB = new ManageDatabase(CreateEventActivity.this);
        DB.AddMeeting(newEvent);

        // end intent and give result code so that main activity refreshes meetings
        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    // end intent
    public void endAction(View view){
        finish();
    }

    @Override
    public void onItemClick(int position) {
    }
}