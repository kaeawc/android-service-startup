package io.kaeawc.servicestartupapp;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.app.Application;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.kaeawc.servicestartupapp.receivers.CommandReceiver;
import io.kaeawc.servicestartupapp.receivers.ConnectedReceiver;
import io.kaeawc.servicestartupapp.receivers.DestructionReceiver;
import io.kaeawc.servicestartupapp.receivers.MessageReceiver;
import io.kaeawc.servicestartupapp.services.FastStartingService;
import io.kaeawc.servicestartupapp.services.BootstrapService;
import io.kaeawc.servicestartupapp.services.MurderousService;
import io.kaeawc.servicestartupapp.services.SuicidalService;
import io.kaeawc.servicestartupapp.services.ServiceDetails;
import io.kaeawc.servicestartupapp.services.ServiceKey;
import io.kaeawc.servicestartupapp.services.SlowStartingService;

public class App extends Application {

    private static final String TAG = App.class.getSimpleName();

    Map<ServiceKey, ServiceDetails> mServices = new HashMap<>();

    protected static App instance;

    public static App getInstance() {
        return instance;
    }

    private ConnectedReceiver mConnectedReceiver;
    private CommandReceiver mCommandReceiver;
    private DestructionReceiver mDestructionReceiver;

    private boolean terminating;

    @Override
    public void onCreate() {
        Log.i(TAG, "Entering Application onCreate");
        super.onCreate();

        // Make sure the static reference is given an instance
        instance = this;
        terminating = false;

        initializeServices();
        Log.i(TAG, "Finishing Application onCreate");
    }

    /**
     * Makes sure all services get unbound and all app receivers are unregistered
     */
    @Override
    public void onTerminate() {
        terminating = true;

        Set<ServiceKey> keySet = mServices.keySet();

        // Unbind from services
        for (ServiceKey key : keySet) {
            ServiceDetails service = mServices.get(key);

            if (service.isBound()) {
                unbindService(service.getConnection());
            }
        }

        // unregister any application receivers
        if (mConnectedReceiver != null) {
            unregisterReceiver(mConnectedReceiver);
        }
        if (mCommandReceiver != null) {
            unregisterReceiver(mCommandReceiver);
        }
        if (mDestructionReceiver != null) {
            unregisterReceiver(mDestructionReceiver);
        }

        super.onTerminate();
    }

    /**
     * Initializes services and starts binding them asynchronously
     */
    private void initializeServices() {

        ServiceDetails fastDetails = new ServiceDetails<>(ServiceKey.Fast, FastStartingService.class, mFastConnection);
        ServiceDetails slowDetails = new ServiceDetails<>(ServiceKey.Slow, SlowStartingService.class, mSlowConnection);
        ServiceDetails murderousDetails = new ServiceDetails<>(ServiceKey.Murderous, MurderousService.class, mMurderousConnection);
        ServiceDetails suicidalDetails = new ServiceDetails<>(ServiceKey.Suicidal, SuicidalService.class, mSuicidalConnection);

        mServices.put(ServiceKey.Fast, fastDetails);
        mServices.put(ServiceKey.Slow, slowDetails);
        mServices.put(ServiceKey.Murderous, murderousDetails);
        mServices.put(ServiceKey.Suicidal, suicidalDetails);

        mConnectedReceiver = new ConnectedReceiver();
        mCommandReceiver = new CommandReceiver();
        mDestructionReceiver = new DestructionReceiver();
        registerReceiver(mConnectedReceiver, new IntentFilter(ConnectedReceiver.TAG));
        registerReceiver(mCommandReceiver, new IntentFilter(CommandReceiver.TAG));
        registerReceiver(mDestructionReceiver, new IntentFilter(DestructionReceiver.TAG));

        for (ServiceKey key : mServices.keySet()) {
            mServices.get(key).bind(this);
        }
    }

    /**
     * Simple underloading for when you don't want to explicitly filter for receivers
     * @param key ServiceKey to lookup a bound service with
     * @param message Message to send
     */
    public void sendServiceMessage(ServiceKey key, String message) {
        sendServiceMessage(key, message, null);
    }

    /**
     * We want to send a message across on a specific service. In order to receive feedback in a
     * BroadcastReceiver we need to construct a PendingIntent that will carry an intent action that
     * matches the string used to register the BroadcastReceiver.
     * @param key ServiceKey to lookup a bound service with
     * @param message Message to send
     * @param filter Will send to MessageReceiver unless otherwise filtered
     */
    public void sendServiceMessage(ServiceKey key, String message, String filter) {

        ServiceDetails details = mServices.get(key);

        if (details == null || !details.isBound()) {
            return;
        }

        Class<?> kclass = details.getKlass();
        Context context = getApplicationContext();

        if (context == null || kclass == null) {
            return;
        }

        if (filter == null) {
            filter = MessageReceiver.TAG;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(),
                0,
                new Intent(filter),
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intent = new Intent(context, kclass);
        intent.putExtra(BootstrapService.MESSAGE, message);
        intent.putExtra("response", pendingIntent);
        startService(intent);
    }

    /**
     * We want to send a message across on a specific service. In order to receive feedback in a
     * BroadcastReceiver we need to construct a PendingIntent that will carry an intent action that
     * matches the string used to register the BroadcastReceiver.
     * @param key ServiceKey to lookup a bound service with
     * @param command Command to send
     */
    public void sendCommand(ServiceKey key, String command) {

        ServiceDetails details = mServices.get(key);

        if (details == null || !details.isBound()) {
            return;
        }

        Class<?> kclass = details.getKlass();
        Context context = getApplicationContext();

        if (context == null || kclass == null) {
            return;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(),
                0,
                new Intent(CommandReceiver.TAG),
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intent = new Intent(context, kclass);
        intent.putExtra(BootstrapService.COMMAND, command);
        intent.putExtra("response", pendingIntent);
        startService(intent);
    }

    public ServiceDetails getService(ServiceKey key) {
        return mServices.get(key);
    }

    public Map<ServiceKey, ServiceDetails> getServices() {
        return mServices;
    }

    /**
     * Callback for when a service connects via the binder. Notify ConnectedReceiver that a service
     * has successfully connected.
     * @param binder LocalBinder that all services will use to bind.
     * @param key ServiceKey for this service. All services should be unique.
     */
    private void onServiceConnect(BootstrapService.LocalBinder binder, ServiceKey key) {

        ServiceDetails details = mServices.get(key);

        if (details == null) {
            return;
        }

        details.setService(binder.getService());
        details.setBound();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(),
                0,
                new Intent(ConnectedReceiver.TAG),
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intent = new Intent(getApplicationContext(), details.getKlass());
        intent.putExtra(BootstrapService.COMMAND, BootstrapService.COMMAND_START);
        intent.putExtra("response", pendingIntent);
        startService(intent);
    }

    /**
     * If a service is disconnected but is capable of reviving itself, it will attempt to rebind.
     * @param key ServiceKey is the unique identifier of this service
     */
    private void onServiceDisconnect(ServiceKey key) {
        Log.i(TAG, "onServiceDisconnect");

        ServiceDetails details = mServices.get(key);

        if (details == null) {
            return;
        }
        Log.i(TAG, String.format("unbinding %s", key));

        details.unbind();

//        if (!terminating && details.shouldRevive()) {
//            Class<?> klass = details.getKlass();
//            ServiceDetails newDetails = new ServiceDetails<>(key, klass, details.getConnection());
//            mServices.put(key, newDetails);
//            mServices.get(key).bind(this);
//        }
    }

    private ServiceConnection mMurderousConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MurderousService.LocalBinder binder = (MurderousService.LocalBinder) service;
            onServiceConnect(binder, ServiceKey.Murderous);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            onServiceDisconnect(ServiceKey.Murderous);
        }
    };

    private ServiceConnection mFastConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            FastStartingService.LocalBinder binder = (FastStartingService.LocalBinder) service;
            onServiceConnect(binder, ServiceKey.Fast);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            onServiceDisconnect(ServiceKey.Fast);
        }
    };

    private ServiceConnection mSlowConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SlowStartingService.LocalBinder binder = (SlowStartingService.LocalBinder) service;
            onServiceConnect(binder, ServiceKey.Slow);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            onServiceDisconnect(ServiceKey.Slow);
        }
    };

    private ServiceConnection mSuicidalConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SuicidalService.LocalBinder binder = (SuicidalService.LocalBinder) service;
            onServiceConnect(binder, ServiceKey.Suicidal);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            onServiceDisconnect(ServiceKey.Suicidal);
        }
    };
}
