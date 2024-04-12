package brokcu.ca.bf22wk.a2;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements RecyclerViewInterface {

    public static SharedPreferences.Editor sharedPreferencesEditor;
    CalendarView calendarView;
    TextView dateText, noMeetingsTV, noMeetingsWeekTV ;
    Calendar calendar;
    Button newEvent;
    RecyclerView meetingRecyclerView, upcomingRecyclerView;

    ManageDatabase DB;

    // This is used so that code can be run after the required action occur in requested activity
    public ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();

                    // If changes occur, reload all the events.
                    getAllEvents();
                    getDayEvents();
                    getWeekEvents();
                    calendarView.setDate(CalendarUtils.selectedDate.getTime());
                    calendarView.setSelected(true);
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initiate views
        setContentView(R.layout.activity_main);
        dateText = (TextView)findViewById(R.id.dateTV);
        calendarView = findViewById(R.id.calendarView);
        noMeetingsTV = findViewById(R.id.noMeetingsTV);
        noMeetingsWeekTV = findViewById(R.id.noMeetingsWeekTV);
        calendar = Calendar.getInstance();
        newEvent = findViewById(R.id.newEvent);
        meetingRecyclerView = findViewById(R.id.meetingRecyclerView);
        upcomingRecyclerView = findViewById(R.id.upcomingRecyclerView);

        // Initiate toolbar on main activity
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // This is used to check whether the recycler view should have an on click event
        CalendarUtils.creatingEvent = false;
        CalendarUtils.contactSelected = null;

        //formats date
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Initialize DB, and get all events from database
        DB = new ManageDatabase(MainActivity.this);
        getAllEvents();

        // Handles case when there are no meetings.
        if (Event.eventsList == null){
            Event.eventsList = new ArrayList<>();
        }
        try {
            // Begins by setting the selected date to todays date
            CalendarUtils.selectedDate
                    = formatter.parse(formatter.format(
                    new Date(calendarView.getDate())));

            // Grabs todays date
            CalendarUtils.todayDate
                    = formatter.parse(formatter.format(
                    new Date(calendarView.getDate())));

            // Note: parsing is used to get rid to set the time to 00:00 for all date variables.
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        assert CalendarUtils.selectedDate != null;
        updateDateDisplay("Date: " + CalendarUtils.formatDate(CalendarUtils.selectedDate));

        // Get all the events for today as well as the current week
        getDayEvents();
        getWeekEvents();

        // Listener for calendar view
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            setDate(dayOfMonth, month, year);


            // Gets selected date and the meetings for that day and week.
            try {
                CalendarUtils.selectedDate = formatter.parse(formatter.format(
                        new Date(calendarView.getDate())));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            ;
            assert CalendarUtils.selectedDate != null;
            updateDateDisplay("Date: " + CalendarUtils.formatDate(CalendarUtils.selectedDate));
            getDayEvents();
            getWeekEvents();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar, menu);
        return true;
    }

    // Menu options and their respective methods
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.newMeeting){
           newEventAction(newEvent);
        }
        if (id == R.id.contacts){
            manageContactsAction(newEvent);
        }
        if (id == R.id.pushToday){
            pushToday();
        }
        if (id == R.id.clearToday){
            clearToday();
        }
        if (id == R.id.clearExpired){
            clearExpired();
        }
        if (id == R.id.clearAll){
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this);
            builder.setTitle("Warning! All meetings will be deleted")
                    .setCancelable(true)
                    .setPositiveButton("Continue", (dialog, which) -> {
                        clearAll();
                        finish();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel()).show();
        }
        return true;
    }

    // Sets the calendar view to whichever date the user chooses
    private void setDate (int day, int month, int year){
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        long milli = calendar.getTimeInMillis();
        calendarView.setDate(milli);
        calendarView.setSelected(true);
    }

    // Updates the display with new selected date
    private void updateDateDisplay(String date){
        dateText.setText(date);
    }

    // Loads all events from the database
    private void getAllEvents(){Event.eventsList = DB.GetAllMeetings();}

    // Loads all events for that day
    private void getDayEvents()
    {
        // List containing events for that day
        ArrayList<Event> dailyEvents = Event.eventsForDate(CalendarUtils.selectedDate);
        EventAdapter eventAdapter = new EventAdapter(getApplicationContext(), dailyEvents,
                this);

        // Sets up recyclerview with the selected date's date
        meetingRecyclerView.setAdapter(eventAdapter);
        meetingRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        // no events that day
        if (dailyEvents.isEmpty()){
            noMeetingsTV.setText(R.string.no_meetings_scheduled_for_today);
        }
        else{
            String s = "# of Meetings: " + dailyEvents.size() + ", Scroll to view all";
            noMeetingsTV.setText(s);
        }
    }

    // Loads all events for that week
    private void getWeekEvents(){

        // List containing events for that week
        ArrayList<Event> weeklyEvents = Event.eventsForWeek(CalendarUtils.selectedDate);

        // Sorts based on date
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Collections.sort(weeklyEvents, Comparator.comparingLong(Event::getDateLong));
        }

        // Sets up recyclerview with events for that week
        EventAdapter eventAdapter = new EventAdapter(this, weeklyEvents, this);
        upcomingRecyclerView.setAdapter(eventAdapter);
        upcomingRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (weeklyEvents.isEmpty()){
            noMeetingsWeekTV.setText(R.string.no_meetings_scheduled_for_week);
        }
        else{
            String s = "# of Meetings: " + weeklyEvents.size() + ", Scroll to view all";
            noMeetingsWeekTV.setText(s);
        }
    }

    // Postpones every event for that day
    public void pushToday(){
        ArrayList<Event> dailyEvents = Event.eventsForDate(CalendarUtils.selectedDate);

        for (Event event: dailyEvents){
            event.postPone();
            DB.UpdateMeeting(event);
        }

        // Updates everything
        getAllEvents();
        getDayEvents();
        getWeekEvents();
    }

    public void newEventAction(View view)
    {
        mStartForResult.launch(new Intent(this, CreateEventActivity.class));
    }

    // Clears all meetings
    private void clearAll(){
        Event.eventsList.clear();
        DB.RemoveAllMeetings();
        Toast.makeText(this, "All Events Have been Cleared", Toast.LENGTH_SHORT).show();
        getAllEvents();
        getDayEvents();
    }

    // Clears all meetings for the day
    private void clearToday(){

        for(Event event : Event.eventsList)
        {
            if(event.getDate().equals(CalendarUtils.selectedDate))
                DB.RemoveMeeting(event);
        }
        getAllEvents();
        getDayEvents();
    }

    // Clears all expired dates
    private void clearExpired(){

        for(Event event : Event.eventsList)
        {
            if(event.getDate().before(CalendarUtils.selectedDate))
                DB.RemoveMeeting(event);
        }
        getAllEvents();
        getDayEvents();
    }


    public void manageContactsAction(View view)
    {
        startActivity(new Intent(this, ManageContacts.class));
    }

    // On item click for recycler views containing the events.
    @Override
    public void onItemClick(int position) {

        // Contains the event that was clicked on.
        Event temp = CalendarUtils.selectedEvent;

        // Passes event values to specific event activity
        Intent intent = new Intent(MainActivity.this, SpecificEvent.class);
        intent.putExtra("name",temp.getName());
        intent.putExtra("time",temp.getTime());
        intent.putExtra("participant",temp.getMeetingContact());
        intent.putExtra("date", temp.getDate().toString());
        intent.putExtra("id", temp.getID());

        mStartForResult.launch(intent);

    }
}