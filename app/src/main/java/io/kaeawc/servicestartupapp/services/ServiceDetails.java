package io.kaeawc.servicestartupapp.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

public class ServiceDetails<T>{

    private ServiceKey mKey;
    private ServiceConnection mConnection;
    private Service mService;
    private Boolean mBound;
    private Class<T> mKlass;

    public ServiceDetails(ServiceKey key, Class<T> klass, ServiceConnection connection) {
        mKey = key;
        mBound = false;
        mKlass = klass;
        mConnection = connection;
    }

    public void setService(Service service) {
        mService = service;
    }

    public Class<T> getKlass() {
        return mKlass;
    }

    public ServiceConnection getConnection() {
        return mConnection;
    }

    public Boolean isBound() {
        return mBound;
    }

    public void setBound() {
        mBound = true;
    }

    public void unbind() {
        mBound = false;
        if (mService != null) {
            mService.unbindService(mConnection);
            mService = null;
        }
        mConnection = null;
    }

    public void bind(Context context) {
        Intent intent = new Intent(context, mKlass);
        context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public boolean shouldRevive() {
        return true;
    }

}