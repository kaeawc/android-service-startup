package io.kaeawc.servicestartupapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MessageReceiver extends BroadcastReceiver {

    public static final String TAG = MessageReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String message = String.format("%s message not formed correctly :(", intent.getAction());
        if (intent.getBooleanExtra("success", false)) {
            message = String.format("%s message received %s", intent.getAction(), intent.getStringExtra("message"));
        }
        Log.i(TAG, message);
    }
}