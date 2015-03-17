package io.kaeawc.servicestartupapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import io.kaeawc.servicestartupapp.App;
import io.kaeawc.servicestartupapp.services.BootstrapService;
import io.kaeawc.servicestartupapp.services.ServiceKey;

public class DestructionReceiver extends BroadcastReceiver {

    public static final String TAG = DestructionReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive for destruction");
        String service = intent.getStringExtra("service");
        Class<?> klass;
        App app = App.getInstance();

        switch (service) {
            case BootstrapService.FAST:
                klass = app.getService(ServiceKey.Fast).getKlass();
                break;
            case BootstrapService.SLOW:
                klass = app.getService(ServiceKey.Slow).getKlass();
                break;
            case BootstrapService.MURDEROUS:
                klass = app.getService(ServiceKey.Murderous).getKlass();
                break;
            case BootstrapService.SUICIDAL:
                klass = app.getService(ServiceKey.Suicidal).getKlass();
                break;
            default:
                Log.i(TAG, "Can't revive an unknown service.");
                return;
        }

        Log.i(TAG, String.format("Reviving service %s as %s", service, klass));
        context.startService(new Intent(context.getApplicationContext(), klass));
    }
}