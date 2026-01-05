package com.example.lab_09;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private boolean mServiceBound = false;
    private SocketService mBoundService;

    private TextView txtmsg;

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBoundService = ((SocketService.LocalBinder) service).getService();
            mServiceBound = true;

            // setăm listener ca să primim mesaje/status în UI
            mBoundService.setUiListener(new ServerSocketThread.MessageListener() {
                @Override
                public void onMessage(String msg) {
                    runOnUiThread(() -> appendLine("MSG: " + msg));
                }

                @Override
                public void onStatus(String status) {
                    runOnUiThread(() -> appendLine("STATUS: " + status));
                }
            });

            appendLine("Bound la service.");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBoundService = null;
            mServiceBound = false;
            appendLine("Service disconnected.");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtmsg = findViewById(R.id.txtmsg);
        Button start = findViewById(R.id.bind);
        Button stop = findViewById(R.id.stop_service);

        start.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SocketService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        });

        stop.setOnClickListener(v -> {
            if (mServiceBound) {
                // scoatem listener-ul ca să nu mai trimitem în UI
                mBoundService.setUiListener(null);
                unbindService(mConnection);
                mServiceBound = false;
                appendLine("Unbound de la service.");
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (mServiceBound) {
            mBoundService.setUiListener(null);
            unbindService(mConnection);
            mServiceBound = false;
        }
        super.onDestroy();
    }

    private void appendLine(String line) {
        String current = txtmsg.getText().toString();
        txtmsg.setText(current + "\n" + line);
    }
}