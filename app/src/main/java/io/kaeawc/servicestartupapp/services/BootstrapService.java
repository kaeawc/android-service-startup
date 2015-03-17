package io.kaeawc.servicestartupapp.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import io.kaeawc.servicestartupapp.receivers.CommandReceiver;
import io.kaeawc.servicestartupapp.receivers.DestructionReceiver;
import io.kaeawc.servicestartupapp.receivers.MessageReceiver;

public abstract class BootstrapService extends IntentService {

    private static final String TAG = BootstrapService.class.getSimpleName();

    private final IBinder mBinder = new LocalBinder();

    protected int mSleepDuration;

    public static final String MESSAGE = "MESSAGE";

    public static final String COMMAND = "COMMAND";
    public static final String COMMAND_START = "START";
    public static final String COMMAND_STOP = "STOP";
    public static final String COMMAND_RECONNECT = "RECONNECT";
    public static final String COMMAND_DISCONNECT = "DISCONNECT";

    public static final String COMMAND_KILL_SLOW = "KILL_SLOW";
    public static final String COMMAND_KILL_FAST = "KILL_FAST";
    public static final String COMMAND_KILL_ALL = "KILL_ALL";
    public static final String COMMAND_KILL_SELF = "KILL_SELF";

    public static final String STATE = "STATE";
    public static final String STATE_STARTED = "STARTED";
    public static final String STATE_STOPPED = "STOPPED";
    public static final String STATE_RECONNECTED = "RECONNECTED";
    public static final String STATE_DISCONNECTED = "DISCONNECTED";

    public static final String FAST = "FAST";
    public static final String SLOW = "SLOW";
    public static final String MURDEROUS = "MURDEROUS";
    public static final String SUICIDAL = "SUICIDAL";

    private String mCurrentState;

    public BootstrapService(String name, int sleepDuration) {
        super(name);
        mSleepDuration = sleepDuration;
        mCurrentState = STATE_STOPPED;
    }

    public class LocalBinder extends Binder {
        public BootstrapService getService() {
            Log.d(TAG, "getService from Binder");
            return BootstrapService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mBinder;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");

        if (intent == null) {
            Log.d(TAG, String.format("%s unable to process null command", getClass().getSimpleName()));
            return;
        }

        String message = intent.getStringExtra(MESSAGE);

        String command = intent.getStringExtra(COMMAND);

        if (mCurrentState.equals(STATE_STARTED) && message != null) {
            onHandleMessage(intent);
            return;
        }

        if (command == null) {
            return;
        }

        onHandleCommand(intent, command);
    }

    protected void onHandleCommand(Intent intent, String command) {

        switch (command) {
            case COMMAND_START:

                if (mCurrentState.equals(STATE_STARTED)) {
                    Log.w(TAG, String.format("Already in %s state, ignoring command.", mCurrentState));
                    break;
                }

                try {
                    Log.d(TAG, "Going to sleep");
                    Thread.sleep(mSleepDuration);
                    Log.d(TAG, "Waking up...");
                } catch (Exception ex) {
                    Log.d(TAG, "Errored while sleeping");
                } finally {
                    Log.d(TAG, "Finished with nap!");
                }

                mCurrentState = STATE_STARTED;
                break;
            case COMMAND_STOP:

                if (mCurrentState.equals(STATE_STOPPED)) {
                    Log.w(TAG, String.format("Already in %s state, ignoring command.", mCurrentState));
                    break;
                }

                mCurrentState = STATE_STOPPED;
                break;
            case COMMAND_RECONNECT:

                if (mCurrentState.equals(STATE_RECONNECTED)) {
                    Log.w(TAG, String.format("Already in %s state, ignoring command.", mCurrentState));
                    break;
                }

                mCurrentState = STATE_RECONNECTED;
                break;
            case COMMAND_DISCONNECT:

                if (mCurrentState.equals(STATE_DISCONNECTED)) {
                    Log.w(TAG, String.format("Already in %s state, ignoring command.", mCurrentState));
                    break;
                }

                mCurrentState = STATE_DISCONNECTED;
                break;
            default:
                Log.d(TAG, "I don't know how to do that David.");
                break;
        }

        Log.d(TAG, String.format("State is now %s.", mCurrentState));

        PendingIntent from  = intent.getParcelableExtra("response");

        if (from == null) {
            Log.d(TAG, "Service requester did not attach a pending intent to reply with.");
            return;
        }

        Intent reply = new Intent(CommandReceiver.TAG);
        reply.putExtra(STATE, mCurrentState);
        reply.putExtra("success", true);
        reply.putExtra("message", intent.getStringExtra(MESSAGE));

        try {
            Log.d(TAG, "Attempting to send reply on service state");
            from.send(getApplicationContext(), 0, reply);
        } catch (PendingIntent.CanceledException canceled) {
            Log.d(TAG, String.format("Service %s received cancel interrupt.", getClass().getSimpleName()));
        } finally {
            Log.d(TAG, String.format("Finished handling intent for %s.", getClass().getSimpleName()));
        }
    }

    protected void onHandleMessage(Intent intent) {
        Log.d(TAG, "onHandleMessage");

        if (intent == null) {
            return;
        }

        PendingIntent from  = intent.getParcelableExtra("response");

        if (from == null) {
            Log.d(TAG, "Service requester did not attach a pending intent to reply with.");
            return;
        }

        Intent reply = new Intent(MessageReceiver.TAG);
        reply.putExtra(STATE, mCurrentState);
        reply.putExtra("success", true);
        reply.putExtra("message", intent.getStringExtra(MESSAGE));

        try {
            Log.d(TAG, "Attempting to send reply on service state");
            from.send(getApplicationContext(), 0, reply);
        } catch (PendingIntent.CanceledException canceled) {
            Log.d(TAG, String.format("Service %s received cancel interrupt.", getClass().getSimpleName()));
        } finally {
            Log.d(TAG, String.format("Finished handling intent for %s.", getClass().getSimpleName()));
        }
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, String.format("%s has been destroyed", getClass().getSimpleName()));
        super.onDestroy();
    }

    protected void endServiceLife(ServiceDetails details) {
        Log.i(TAG, String.format("%s is being ended", details.getKlass()));
        Intent deathIntent = new Intent(getApplicationContext(), details.getKlass());
        boolean result = stopService(deathIntent);
        Log.i(TAG, String.format("Has %s ended? %s", details.getKlass(), result));

        Intent graveyardIntent = new Intent(DestructionReceiver.TAG);
        graveyardIntent.putExtra("service", details.getKlass().toString());
        sendBroadcast(graveyardIntent);
    }
}
