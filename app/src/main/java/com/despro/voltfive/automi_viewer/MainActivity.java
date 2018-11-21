package com.despro.voltfive.automi_viewer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;

import org.opencv.core.Core;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int DEFAULT_THREAD_POOL_SIZE = 4;
    private static final int START = 1;
    private static final int STOP = 2;
    private static final int CLIENT_CONNECTED = 3;
    private static final int CLIENT_DISCONNECTED = 4;
    private static final int SEND_COMMAND = 11;
    private static final int RECEIVE_FRAME = 21;
    private static final int SAVE_IMAGE = 31;
    private static final int SAVE_VIDEO = 32;

    private ImageView frameView;
    private Switch cdcButton;
    private SeekBar zoomSlider, brightnessSlider;
    private EditText ipText, portText, nameText;

    private NetworkHandlerThread networkHandlerThread;
    private Handler mainThreadHandler;
    private Message response;
    private ArrayList<String> commands;
    private Intent clientIntent;

    private boolean isClientConnected = false;

    static {System.loadLibrary(Core.NATIVE_LIBRARY_NAME);}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_designed);

        frameView = findViewById(R.id.frameView);
        cdcButton = findViewById(R.id.cdcBtn);
        ipText = findViewById(R.id.ipText);
        portText = findViewById(R.id.portText);
        nameText = findViewById(R.id.nameText);
        zoomSlider = findViewById(R.id.zoomSlider);
        brightnessSlider = findViewById(R.id.brightnessSlider);

        commands = new ArrayList<>(50);
        clientIntent = new Intent(this, ClientService.class);

        initListeners();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }

        mainThreadHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case RECEIVE_FRAME:
                        updateFrame((Bitmap) msg.obj);
                        break;
                    case CLIENT_CONNECTED:
                        isClientConnected = true;
                        cdcButton.setText(R.string.label_disconnect_button);
                        cdcButton.setChecked(true);
                        break;
                    case CLIENT_DISCONNECTED:
                        isClientConnected = false;
                        cdcButton.setText(R.string.label_connect_button);
                        cdcButton.setChecked(false);
                        Log.d(TAG, "handleMessage: Client disconnected succesfully.");
                        break;
                }
            }
        };

        networkHandlerThread = new NetworkHandlerThread(this,"Network-Handler-Thread", mainThreadHandler);
        networkHandlerThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        networkHandlerThread.childThreadHandler.sendEmptyMessage(STOP);
        networkHandlerThread.quitSafely();
    }

    private void initListeners() {
        zoomSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Message message = new Message();
                message.what = SEND_COMMAND;
                message.obj = "zoom:" + progress;
                networkHandlerThread.childThreadHandler.sendMessage(message);
                Log.d(TAG, "onProgressChanged: Zoom: " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        brightnessSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Message message = new Message();
                message.what = SEND_COMMAND;
                message.obj = "brightness:" + progress;
                networkHandlerThread.childThreadHandler.sendMessage(message);
                Log.d(TAG, "onProgressChanged: Zoom: " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        cdcButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String ip = ipText.getText().toString();
                String name = nameText.getText().toString();
                if (name.equals("")) {
                    name = "Anonymous";
                    nameText.setText(name);
                    Log.d(TAG, "onCheckedChanged: No name entered?");
                }
                int port;
                try{
                    port = Integer.parseInt(portText.getText().toString());
                }catch (NumberFormatException e) {
                    port = 9765;
                    Log.d(TAG, "connectDisconnect: No port inputted.");
                }

                if (isChecked) {
                    response = new Message();
                    String payload[] = new String[3];
                    payload[0] = ip;
                    payload[1] = String.valueOf(port);
                    payload[2] = name;
                    response.what = START;
                    response.obj = payload;
                    networkHandlerThread.childThreadHandler.sendMessage(response);
                    cdcButton.setText(R.string.label_disconnect_button);
                } else {
                    networkHandlerThread.childThreadHandler.sendEmptyMessage(STOP);
                    cdcButton.setText(R.string.label_connect_button);
                }
            }
        });


    }
    private void updateFrame(Bitmap bitmap){
        Log.i(TAG, "updateFrame: Updating frame");
        frameView.setImageBitmap(bitmap);
    }

    public void connectDisconnect(View view) {
        String cdc = cdcButton.getText().toString().toLowerCase();
        String ip = ipText.getText().toString();
        String name = nameText.getText().toString();
        int port;
        try{
            port = Integer.parseInt(portText.getText().toString());
        }catch (NumberFormatException e) {
            port = 9765;
            Log.d(TAG, "connectDisconnect: No port inputted.");
        }

        switch(cdc) {
            case "connect":
//                Version 1:
//                frameTask = new FrameTask(MainActivity.this, frameView, cdcButton, frameCountView, ip, port, "Brentjeffson", serverState);
//                frameTask.execute();
//                Version 1:
//                Version 2:
//                clientIntent.putExtra("IP", ip);
//                clientIntent.putExtra("PORT", port);
//                clientIntent.putExtra("CLIENT-NAME", name);
//                startService(clientIntent);
//                Version 2:
//                Version 3:
                response = new Message();
                String payload[] = new String[3];
                payload[0] = ip;
                payload[1] = String.valueOf(port);
                payload[2] = "Brent";
                response.what = START;
                response.obj = payload;
                networkHandlerThread.childThreadHandler.sendMessage(response);
//                Version 3:
                break;
            case "disconnect":
//                Version 2:
//                stopService(clientIntent);
//                Version 2:
//                Version 3:
                networkHandlerThread.childThreadHandler.sendEmptyMessage(STOP);
//                Version 3:
                break;
        }
    }


    public void  updateCommands(View view) {
        Message message = new Message();
        switch (view.getId()){
            case R.id.captureBtn:
                networkHandlerThread.childThreadHandler.sendEmptyMessage(SAVE_IMAGE);
                break;
            case R.id.recordBtn:
                networkHandlerThread.childThreadHandler.sendEmptyMessage(SAVE_VIDEO);
                break;
            case R.id.forwardBtn:
//                this.sendTask.execute("forward");
                commands.add("forward");
                message.what = SEND_COMMAND;
                message.obj = "forward";
                networkHandlerThread.childThreadHandler.sendMessage(message);
                break;
            case R.id.backwardBtn:
                commands.add("backward");
                message.what = SEND_COMMAND;
                message.obj = "backward";
                networkHandlerThread.childThreadHandler.sendMessage(message);
//                this.sendTask.execute("backward");
                break;
            case R.id.rightBtn:
                commands.add("right");
                message.what = SEND_COMMAND;
                message.obj = "right";
                networkHandlerThread.childThreadHandler.sendMessage(message);
//                this.sendTask.execute("right");
                break;
            case R.id.leftBtn:
                commands.add("left");
                message.what = SEND_COMMAND;
                message.obj = "left";
                networkHandlerThread.childThreadHandler.sendMessage(message);
//                this.sendTask.execute("left");
                break;
            case R.id.upBtn:
                commands.add("up");
                message.what = SEND_COMMAND;
                message.obj = "up";
                networkHandlerThread.childThreadHandler.sendMessage(message);
//                this.sendTask.execute("up");
                break;
            case R.id.downBtn:
                Log.d(TAG, "updateCommands: Presssing Down Button.");
                commands.add("down");
                message.what = SEND_COMMAND;
                message.obj = "down";
                networkHandlerThread.childThreadHandler.sendMessage(message);
//                this.sendTask.execute("down");
                break;
        }
//        Toast.makeText(this,
//                "Command: " + commands.get(commands.size()-1),
//                Toast.LENGTH_SHORT).show();
        Log.d(TAG, "updateCommands: Commands Size: " + commands.size());
    }
}
