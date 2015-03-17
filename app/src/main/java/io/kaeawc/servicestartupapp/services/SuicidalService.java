package io.kaeawc.servicestartupapp.services;

import android.content.Intent;
import android.util.Log;

import io.kaeawc.servicestartupapp.App;

public class SuicidalService extends BootstrapService {

    private static final String TAG = SuicidalService.class.getSimpleName();

    public SuicidalService() {
        super(TAG, 3000);
    }

    @Override
    protected void onHandleCommand(Intent intent, String command) {
        App app = App.getInstance();

        if (app == null) {
            Log.i(TAG, "Can't be killing services without an app.");
            return;
        }

        switch (command) {
            case COMMAND_KILL_SELF:
                Log.i(TAG, "Killing self...");
                endServiceLife(app.getService(ServiceKey.Suicidal));
                break;
            default:
                Log.i(TAG, "I don't really feel like it");
                break;
        }

        super.onHandleCommand(intent, command);
    }
}
