package edu.washington.bbarron.awty;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;



public class MainActivity extends ActionBarActivity {

    private EditText phone;
    private Button button;
    private boolean isActive;
    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;
    private boolean isMessageValid;
    private boolean isPhoneValid;
    private boolean isTimeValid;
    private String messageText;
    private String phoneText;
    private int freq;
    private static int CONTACTS_REQUEST_CODE = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isActive = false;
        isMessageValid = false;
        isPhoneValid = false;
        isTimeValid = false;

        EditText message = (EditText) findViewById(R.id.messageInput);
        message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                messageText = s.toString();
                isMessageValid = !messageText.equals("");
                checkInputValidity();
            }
        });

        phone = (EditText) findViewById(R.id.phoneInput);
        phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                phoneText = s.toString();
                isPhoneValid = !phoneText.equals("");
                checkInputValidity();
            }
        });

        // sets listener for button which opens contacts for user to select from
        Button getContacts = (Button) findViewById(R.id.contacts);
        getContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI),
                        CONTACTS_REQUEST_CODE);
            }
        });

        EditText time = (EditText) findViewById(R.id.timeInput);
        time.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String timeText = s.toString();
                if (!timeText.equals("")) {
                    freq = Integer.parseInt(timeText);
                    isTimeValid = (freq > 0);
                }
                checkInputValidity();
            }
        });

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isActive = !isActive;
                setButtonText();

                if (isActive) { // set off alarm
                    Log.i("MainActivity", "messaging active");

                    Intent startAlarm = new Intent(MainActivity.this, AlarmBroadcastReceiver.class);

                    startAlarm.putExtra("message", messageText);
                    startAlarm.putExtra("phone", phoneText);
                    long interval = freq * 60 * 1000; // milliseconds

                    pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, startAlarm,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                            interval, pendingIntent);

                } else { // stop alarm
                    Log.i("MainActivity", "stop button: turning off messaging");
                    if (pendingIntent != null) {
                        alarmManager.cancel(pendingIntent);
                    }
                }
            }
        });

        // set button text and check validity of inputs
        setButtonText();
        checkInputValidity();
    }

    public void checkInputValidity() {
        if (isMessageValid && isPhoneValid && isTimeValid) {
            button.setEnabled(true);
        } else {
            button.setEnabled(false);
        }
    }

    public void setButtonText() {
        if (isActive) {
            button.setEnabled(true);
            button.setText("Stop");
        } else {
            button.setText("Start");
        }
    }

    @Override
    protected void onDestroy() {
        Log.i("MainActivity", "onDestroy() event firing");

        // stop messaging if app is destroyed
        if (pendingIntent != null) {
            Log.i("MainActivity", "onDestroy: turning off messaging");
            alarmManager.cancel(pendingIntent);
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CONTACTS_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri contact = data.getData();
            String contactID = null;
            String contactNumber = null;

            // get contactID
            Cursor cursorID = getContentResolver().query(contact,
                    new String[]{ContactsContract.Contacts._ID},
                    null, null, null);
            if (cursorID.moveToFirst()) {
                contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
            }
            cursorID.close();

            // get contact phone number
            Cursor cursorPhone = getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                            ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                    new String[]{contactID},
                    null);
            if (cursorPhone.moveToFirst()) {
                contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            }

            // set phone number text field
            phone.setText(contactNumber);

            cursorPhone.close();
        }
    }
}