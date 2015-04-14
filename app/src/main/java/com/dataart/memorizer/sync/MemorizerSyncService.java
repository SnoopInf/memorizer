package com.dataart.memorizer.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MemorizerSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static MemorizerSyncAdapter sMemorizerSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("MemorizerSyncService", "onCreate - MemorizerSyncService");
        synchronized (sSyncAdapterLock) {
            if (sMemorizerSyncAdapter == null) {
                sMemorizerSyncAdapter = new MemorizerSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sMemorizerSyncAdapter.getSyncAdapterBinder();
    }
}