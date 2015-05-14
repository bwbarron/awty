package edu.washington.bbarron.awty;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    private EditText message;
    private EditText phone;
    private EditText time;
    private Button button;
    private boolean isActive;
    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;
    private boolean isMessageValid;
    private boolean isPhoneValid;
    private boolean isTimeValid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            isActive = savedInstanceState.getBoolean("isActive");
            isMessageValid = savedInstanceState.getBoolean("message");
            isPhoneValid = savedInstanceState.getBoolean("phone");
            isTimeValid = savedInstanceState.getBoolean("time");
        } else {
            isActive = false;
            isMessageValid = false;
            isPhoneValid = false;
            isTimeValid = false;
        }

        message = (EditText) findViewById(R.id.messageInput);
        message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                isMessageValid = !message.getText().toString().equals("");
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
                isPhoneValid = !phone.getText().toString().equals("");
                checkInputValidity();
            }
        });

        time = (EditText) findViewById(R.id.timeInput);
        time.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                int freq = Integer.parseInt(time.getText().toString());
                isTimeValid = (freq > 0);
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

                    Intent startAlarm = new Intent(MainActivity.this, AlarmBroadcastReceiver.class);

                    String messageText = message.getText().toString();
                    String phoneNum = phone.getText().toString();
                    long interval = Integer.parseInt(time.getText().toString()) * 60 * 1000; // milliseconds

                    startAlarm.putExtra("message", messageText);
                    startAlarm.putExtra("phone", phoneNum);
                    pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, startAlarm, 0);

                    alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                            interval, pendingIntent);

                } else { // stop alarm
                    if (pendingIntent != null) {
                        alarmManager.cancel(pendingIntent);
                    }
                }
            }
        });

        setButtonText();
        checkInputValidity();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("isActive", isActive);
        savedInstanceState.putBoolean("message", isMessageValid);
        savedInstanceState.putBoolean("phone", isPhoneValid);
        savedInstanceState.putBoolean("time", isTimeValid);

        super.onSaveInstanceState(savedInstanceState);
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
}
