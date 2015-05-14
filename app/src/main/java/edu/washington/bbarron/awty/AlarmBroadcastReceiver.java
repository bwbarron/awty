package edu.washington.bbarron.awty;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String message = intent.getStringExtra("message");
        String phone = intent.getStringExtra("phone");
        Toast.makeText(context, phone + ": " + message, Toast.LENGTH_SHORT).show();
    }

}
