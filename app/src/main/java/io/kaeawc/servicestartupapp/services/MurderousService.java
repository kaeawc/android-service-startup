package io.kaeawc.servicestartupapp.services;

import android.content.Intent;
import android.util.Log;

import java.util.Map;

import io.kaeawc.servicestartupapp.App;

public class MurderousService extends BootstrapService {

    private static final String TAG = MurderousService.class.getSimpleName();

    public MurderousService() {
        super(TAG, 2000);
    }

    @Override
    protected void onHandleCommand(Intent intent, String command) {
        App app = App.getInstance();

        if (app == null) {
            Log.i(TAG, "Can't be killing services without an app.");
            return;
        }

        switch (command) {
            case COMMAND_KILL_ALL:
                Log.i(TAG, "Killing all services");
                Map<ServiceKey, ServiceDetails> services = app.getServices();

                for (ServiceKey key : services.keySet()) {
                    endServiceLife(services.get(key));
                }
                break;
            case COMMAND_KILL_FAST:
                Log.i(TAG, "Killing Fast service");
                endServiceLife(app.getService(ServiceKey.Fast));
                break;
            case COMMAND_KILL_SLOW:

                Log.i(TAG, "Killing Slow service");
                endServiceLife(app.getService(ServiceKey.Slow));
                break;
            default:
                break;
        }

        super.onHandleCommand(intent, command);
    }
}
