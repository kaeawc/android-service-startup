package io.kaeawc.servicestartupapp.services;

public class SlowStartingService extends BootstrapService {

    private static final String TAG = SlowStartingService.class.getSimpleName();

    public SlowStartingService() {
        super(TAG, 5000);
    }
}
