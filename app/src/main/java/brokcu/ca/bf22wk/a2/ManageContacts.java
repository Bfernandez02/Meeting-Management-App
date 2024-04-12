package brokcu.ca.bf22wk.a2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import android.Manifest;

public class ManageContacts extends AppCompatActivity implements RecyclerViewInterface {

    private static final int PERMISSION_READ_CONTACTS = 777;
    Intent contactsIntent;

    TextView noContactsTV;
    RecyclerView contactsList;

    private final String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY;
    private final String FILTER = DISPLAY_NAME + " NOT LIKE '%@%'";
    private final String ORDER = String.format("%1$s COLLATE NOCASE", DISPLAY_NAME);
    @SuppressLint("InlinedApi")
    private final String[] PROJECTION = {
            ContactsContract.Contacts._ID,
            DISPLAY_NAME,
            ContactsContract.Contacts.HAS_PHONE_NUMBER
    };

    ArrayList<Contact> contacts = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_contacts);

        noContactsTV = findViewById(R.id.noContactsTV);
        contactsList = findViewById(R.id.contactsList);

        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
            loadContacts();
        else
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSION_READ_CONTACTS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) { //Permission granted
                loadContacts();
            } else {
                Toast.makeText(this, "No can do!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("Range")
    public void loadContacts() {

        contacts = new ArrayList<>();
        try {

            ContentResolver cr = getContentResolver();
            Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, PROJECTION, FILTER, null, ORDER);
            if (cursor != null && cursor.moveToFirst()) {

                do {
                    // get the contact's information
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));
                    int hasPhone = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                    // get the user's email address
                    String email = null;
                    Cursor ce = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{id}, null);
                    if (ce != null && ce.moveToFirst()) {
                        email = ce.getString(ce.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                        ce.close();
                    }

                    // get the user's phone number
                    String phone = null;
                    if (hasPhone > 0) {
                        Cursor cp = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                        if (cp != null && cp.moveToFirst()) {
                            phone = cp.getString(cp.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            cp.close();
                        }
                    }

                    // if the user user has an email or phone then add it to contacts
                    if ((!TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                            && !email.equalsIgnoreCase(name)) || (!TextUtils.isEmpty(phone))) {
                        contacts.add(new Contact(id, name, phone, email));
                    }

                } while (cursor.moveToNext());

                // clean up cursor
                cursor.close();
            }
        } catch (Exception ex) {
            contacts = new ArrayList<>();
        }
        
        populateContacts();
    }

    // Sets up recycler view containing contacts
    private void populateContacts()
    {
        ContactAdapter eventAdapter = new ContactAdapter(getApplicationContext(), contacts, this);
        contactsList.setAdapter(eventAdapter);
        contactsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        if (contacts.isEmpty()){
            noContactsTV.setText(R.string.no_meetings_scheduled_for_today);
        }
        else{
            noContactsTV.setText("");
        }
    }

    // Redirects user to contacts app if they want to create or delete contacts.
    public void createContact(View view) {
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){
            contactsIntent = new Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI);
            startActivity(contactsIntent);
            loadContacts();
        }
        else{
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSION_READ_CONTACTS);}
    }

    public void endAction(View view){
        finish();
    }

    // Registers which contact is clicked on (Used for create event activity)
    @Override
    public void onItemClick(int position) {
        CalendarUtils.contactSelected =  contacts.get(position);

        if (CalendarUtils.creatingEvent){
            Intent intent = new Intent();
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }
}