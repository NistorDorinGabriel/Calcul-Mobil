package com.example.lab_09;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class SocketService extends Service {

    private static final String TAG = "SocketService";
    private static final int PORT = 8089;

    private final IBinder binder = new LocalBinder();
    private ServerSocketThread serverThread;

    // Listener setat de Activity (când e bound)
    private ServerSocketThread.MessageListener uiListener;

    public class LocalBinder extends Binder {
        public SocketService getService() {
            return SocketService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "in onCreate");

        serverThread = new ServerSocketThread(PORT, new ServerSocketThread.MessageListener() {
            @Override
            public void onMessage(String msg) {
                if (uiListener != null) uiListener.onMessage(msg);
            }

            @Override
            public void onStatus(String status) {
                if (uiListener != null) uiListener.onStatus(status);
            }
        });
        serverThread.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "in onBind");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "in onUnbind");
        // true => dacă se face rebind, va apela onRebind
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(TAG, "in onRebind");
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "in onDestroy");
        if (serverThread != null) serverThread.shutdown();
        super.onDestroy();
    }

    // Expus către Activity
    public void setUiListener(ServerSocketThread.MessageListener listener) {
        this.uiListener = listener;
    }
}