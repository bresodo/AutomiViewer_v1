package com.despro.voltfive.automi_viewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.Socket;

public class FrameTask_fullyworking extends AsyncTask<Void, Bitmap, Void> {
    private static final String TAG = "FrameTask";

    private WeakReference<Context> context;
    private WeakReference<ImageView> frameView;
    private WeakReference<TextView> frameCountView;

    private Socket socket;
    private BufferedInputStream in;
    private BufferedOutputStream out;

    private int frameCount = 1;
    private String ip;
    private int port;

    public FrameTask_fullyworking(Context context, ImageView frameView, TextView frameCountView, String ip, int port) {
        this.context = new WeakReference<>(context);
        this.frameCountView = new WeakReference<>(frameCountView);
        this.frameView = new WeakReference<>(frameView);

        this.ip = ip;
        this.port = port;
    }
    @Override
    protected void onPreExecute() {

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
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            this.socket = new Socket(ip, port);
            this.in = new BufferedInputStream(this.socket.getInputStream());
            this.out = new BufferedOutputStream(this.socket.getOutputStream());
        } catch(ConnectException e) {
            Log.d(TAG, "doInBackground: Connection refuse at " + ip + ": " + port + ":Message -> " + e.getMessage());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        while(frameCount < 2000) {
            Log.d(TAG, "doInBackground: Changing frame.");
            try {
                byte buffer[] = new byte[8];
                String stringSize;
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
                    Log.d(TAG, "doInBackground: Frame Content -> " + frameContent);
                    byte[] imageByte = Base64.decode(frameContent.toString(), Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
                    publishProgress(bitmap);
                } else {
//                    Toast.makeText(this.context.get(), "Unexpected Disconnection.", Toast.LENGTH_SHORT).show();
                    System.out.println("Unexpected Disconnection.");
                    Log.d(TAG, "doInBackground: Disconnected.");
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
        return null;
    }

    @Override
    protected void onProgressUpdate(Bitmap... values) {
        frameCountView.get().setText("Frames: ".concat(String.valueOf(frameCount)));
        frameView.get().setImageBitmap(values[0]);
        Log.d(TAG, "onProgressUpdate: Frame changed.");
    }
}
