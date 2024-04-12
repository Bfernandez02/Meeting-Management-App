package brokcu.ca.bf22wk.a2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class SpecificEvent extends AppCompatActivity implements RecyclerViewInterface {

    RecyclerView contactsList;
    ManageDatabase DB;
    TextView noContactsTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_event);

        String name = getIntent().getStringExtra("name");
        String date = getIntent().getStringExtra("date");
        String participant = getIntent().getStringExtra("participant");
        int [] time = getIntent().getIntArrayExtra("time");
        int id = getIntent().getIntExtra("id", -1);

        noContactsTV = findViewById(R.id.noContactsTV);
        TextView selectedNameTV = findViewById(R.id.selectedNameTV);
        TextView selectedTimeTV = findViewById(R.id.selectedTimeTV);
        TextView selectedDateTV = findViewById(R.id.selectedDateTV);
        contactsList = findViewById(R.id.contactsList);

        assert date != null;
        selectedDateTV.setText(String.format("%s, %s", date.substring(0, 10),
                date.substring(24)));
        assert time != null;
        selectedTimeTV.setText(CalendarUtils.formattedTime(time[0], time[1]));
        selectedNameTV.setText(name);

        String[] temp = new String[3];

        if (participant!=null){
            temp = participant.split(",");
        }

        Contact c;

        switch (temp.length){

            case 1:
                c = new Contact("1",temp[0], "", "");
                break;
            case 2:
                c = new Contact("1",temp[0], temp[1], "");
                break;
            case 3:
                c = new Contact("1",temp[0], temp[2], temp[2]);
                break;
            case 4:
                c = new Contact("1",temp[1], temp[2], temp[3]);
                break;
            default:
                c = null;
            break;
        }

        ArrayList<Contact> tempList = new ArrayList<>();

        if (c != null){
            tempList.add(c);
        }
        populateContacts(tempList);

    }

    private void populateContacts(ArrayList<Contact> contacts)
    {
        ContactAdapter eventAdapter = new ContactAdapter(getApplicationContext(), contacts, this);
        contactsList.setAdapter(eventAdapter);
        contactsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        if (contacts.isEmpty()){
            noContactsTV.setText(R.string.no_contact);
        }
        else{
            noContactsTV.setText("");
        }
    }

    public void postponeMeeting(View view) {
        DB = new ManageDatabase(SpecificEvent.this);
        CalendarUtils.selectedEvent.postPone();
        DB.UpdateMeeting(CalendarUtils.selectedEvent);
        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    public void deleteMeeting(View view) {
        DB = new ManageDatabase(SpecificEvent.this);
        DB.RemoveMeeting(CalendarUtils.selectedEvent);
        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    public void endAction(View view){
        finish();
    }

    @Override
    public void onItemClick(int position) {

    }
}