package com.despro.voltfive.automi_viewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;

public class FrameTask extends AsyncTask<Void, Bitmap, Void> {
    private static final String TAG = "FrameTask";

    private WeakReference<Context> context;
    private WeakReference<Button> cdcButton;
    private WeakReference<ImageView> frameView;
    private WeakReference<TextView> frameCountView;

    private Socket socket;
    private BufferedInputStream in;
    private DataOutputStream out;

    private int frameCount = 1;
    private ArrayList<String> commandsQueue;

    private String ip;
    private int port;
    private String name;
    private boolean serverState;

    public FrameTask(Context context, ImageView frameView, Button cdcButton, TextView frameCountView, String ip, int port, String name, boolean serverState) {
        this.context = new WeakReference<>(context);
        this.cdcButton = new WeakReference<>(cdcButton);
        this.frameCountView = new WeakReference<>(frameCountView);
        this.frameView = new WeakReference<>(frameView);

        this.ip = ip;
        this.port = port;
        this.name = name;
        this.serverState = serverState;
    }

    @Override
    protected void onProgressUpdate(Bitmap... values) {
        frameCountView.get().setText("Frames: ".concat(String.valueOf(frameCount)));
        frameView.get().setImageBitmap(values[0]);
        cdcButton.get().setText(context.get().getString(R.string.label_disconnect_button));
        Log.d(TAG, "onProgressUpdate: Frame changed.");
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if(this.socket != null) {
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        cdcButton.get().setText(context.get().getString(R.string.label_connect_button));
        Log.d(TAG, "onPreExecute: Disconnecting from server");
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            Log.d(TAG, "connect: Connecting to server at " + ip + ":" + port);
            this.socket = new Socket(ip, port);
            Log.d(TAG, "connect: Connected to server at " + ip + ":" + port);
            this.in = new BufferedInputStream(this.socket.getInputStream());
            this.out = new DataOutputStream(this.socket.getOutputStream());
            Log.d(TAG, "doInBackground: Sending name");
            byte name_byte[] = this.name.getBytes();
            this.out.write(name_byte, 0, name_byte.length);
            while(serverState) {
                Log.d(TAG, "doInBackground: " + serverState);
                try {

                    byte buffer[] = new byte[8];
                    String stringSize;
                    Log.d(TAG, "doInBackground: Receiving Frame...");
                    int isReceived = in.read(buffer, 0, buffer.length);
                    if(isReceived != -1) {

                        stringSize = new String(buffer, 0, buffer.length);
                        Log.d(TAG, "doInBackground: Converting " + stringSize + " to integer ".concat(String.valueOf(Integer.valueOf(stringSize))));
                        int frameSize = Integer.valueOf(stringSize);
                        byte frameBuffer[] = new byte[4086];

                        StringBuffer stringBuffer = new StringBuffer();
                        int read = 0;
                        int totalRead = 0;
                        int remaining = frameSize;
                        while((read = in.read(frameBuffer, 0, Math.min(frameBuffer.length, remaining))) > 0) {
                            totalRead += read;
                            remaining -= read;
                            stringBuffer.append(new String(frameBuffer, 0, read));
                        }

                        String frameContent = stringBuffer.toString();
                        Log.d(TAG, "doInBackground: Frame Received...");
                        byte[] imageByte = Base64.decode(frameContent.toString(), Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
                        Log.d(TAG, "doInBackground: Changing frame.");
                        publishProgress(bitmap);
                    } else {
                        Log.d(TAG, "doInBackground: Disconnected.");
                        return null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(1);
                    frameCount++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch(ConnectException e) {
            Log.d(TAG, "doInBackground: Connection refuse at " + ip + ": " + port + ":Message -> " + e.getMessage());
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
