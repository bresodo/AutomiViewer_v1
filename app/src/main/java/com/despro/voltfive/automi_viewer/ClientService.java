package com.despro.voltfive.automi_viewer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

public class ClientService extends Service {
    private static final String TAG = "ClientService";

    private Button cdcButton;

    private String name;
    private String ip;
    private int port;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: Initializing Client Service");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        name = intent.getStringExtra("CLIENT-NAME");
        ip = intent.getStringExtra("IP");
        port = intent.getIntExtra("PORT", 8888);
        Toast.makeText(this, name + " - " + ip + " - " + " - " + port, Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: Disconnecting client from server at " + ip + ":" + port);
        super.onDestroy();
    }
}
