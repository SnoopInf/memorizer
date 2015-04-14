package com.dataart.memorizer.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MemorizerAuthenticatorService extends Service {
    private MemorizerAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new MemorizerAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
