package io.kaeawc.servicestartupapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ConnectedReceiver extends BroadcastReceiver {

    public static final String TAG = ConnectedReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String message = String.format("%s failed to start :(", intent.getAction());
        if (intent.getBooleanExtra("success", false)) {
            message = String.format("%s successfully started", intent.getAction());
        }
        Log.i(TAG, message);
    }
}