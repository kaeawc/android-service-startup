package io.kaeawc.servicestartupapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CommandReceiver extends BroadcastReceiver {

    public static final String TAG = CommandReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String message = String.format("%s command not formed correctly :(", intent.getAction());
        if (intent.getBooleanExtra("success", false)) {
            message = String.format("%s command executed %s", intent.getAction(), intent.getStringExtra("command"));
        }
        Log.i(TAG, message);
    }
}