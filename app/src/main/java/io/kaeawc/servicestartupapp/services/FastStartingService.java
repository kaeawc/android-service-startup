package io.kaeawc.servicestartupapp.services;

public class FastStartingService extends BootstrapService {

    public static final String TAG = FastStartingService.class.getSimpleName();

    public FastStartingService() {
        super(TAG, 100);
    }
}
