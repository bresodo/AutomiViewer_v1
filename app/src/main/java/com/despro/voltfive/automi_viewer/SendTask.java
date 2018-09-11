package com.despro.voltfive.automi_viewer;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class SendTask extends AsyncTask<String, String, Void>{
    private static final String TAG = "SendTask";
    private WeakReference<Context> context;

    private CommunicationClient client;

    private boolean isConnected = false;

    private String ip;
    private int port;
    private String name;

    public SendTask(Context context, String ip, int port, String name) {
        this.context = new WeakReference<>(context);
        this.ip = ip;
        this.port = port;
        this.name = name;
    }

    @Override
    protected Void doInBackground(String... strings) {
        Log.d(TAG, "connect: Connecting to server at " + ip + ":" + port);
        this.client = new CommunicationClient(ip, port, name);
        Log.d(TAG, "connect: Connected to server at " + ip + ":" + port);
        isConnected = client.connect();
        publishProgress("Sending Command: ".concat(strings[0]));
        boolean sent = this.client.sendMessage(strings[0]);
        if(sent) {
            publishProgress("Command Sent.");
        } else {
            publishProgress("Unable to send command.");
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        Toast.makeText(this.context.get(), values[0],Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Log.d(TAG, "connect: Disconnecting from server at " + ip + ":" + port);
        this.client.disconnect();
    }
}
