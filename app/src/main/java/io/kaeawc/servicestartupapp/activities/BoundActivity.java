package io.kaeawc.servicestartupapp.activities;

import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;

import io.kaeawc.servicestartupapp.receivers.MessageReceiver;

public class BoundActivity extends ActionBarActivity {

    private MessageReceiver mMessageReceiver;

    @Override
    protected void onResume() {
        super.onResume();

        mMessageReceiver = new MessageReceiver();
        registerReceiver(mMessageReceiver, new IntentFilter(MessageReceiver.TAG));
    }

    @Override
    protected void onStop() {
        super.onStop();

        // unregister any application receivers
        if (mMessageReceiver != null) {
            unregisterReceiver(mMessageReceiver);
        }
    }
}
