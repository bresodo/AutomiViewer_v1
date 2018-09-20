package com.despro.voltfive.automi_viewer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

    private ImageView frameView;
    private TextView frameCountView;
    private Button cdcButton;
    private EditText ipText;
    private EditText portText;

    private FrameTask frameTask;

    private NetworkHandlerThread networkHandlerThread;
    private Handler mainThreadHandler;
    private Message response;
    private ArrayList<String> commands;
    private Intent clientIntent;

    private boolean isClientConnected = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frameView = findViewById(R.id.frameView);
        frameCountView = findViewById(R.id.frameCountView);
        cdcButton = findViewById(R.id.cdcButton);
        ipText = findViewById(R.id.ipText);
        portText = findViewById(R.id.portText);

        commands = new ArrayList<>(50);
        clientIntent = new Intent(this, ClientService.class);

        mainThreadHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case RECEIVE_FRAME:
                        updateFrame((Bitmap) msg.obj);
                        break;
                    case CLIENT_CONNECTED:
                        isClientConnected = true;
                        cdcButton.setText(R.string.disconnect_button_name);
                        break;
                    case CLIENT_DISCONNECTED:
                        isClientConnected = false;
                        cdcButton.setText(R.string.connect_button_name);
                        break;
                }
            }
        };

        networkHandlerThread = new NetworkHandlerThread("Network-Handler-Thread", mainThreadHandler);
        networkHandlerThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        networkHandlerThread.childThreadHandler.sendEmptyMessage(STOP);
        networkHandlerThread.quitSafely();
    }

    private void updateFrame(Bitmap bitmap){
        Log.i(TAG, "updateFrame: Updating frame");
        frameView.setImageBitmap(bitmap);
    }

    public void connectDisconnect(View view) {
        String cdc = cdcButton.getText().toString().toLowerCase();
        String ip = ipText.getText().toString();
        int port;
        try{
            port = Integer.parseInt(portText.getText().toString());
        }catch (NumberFormatException e) {
            port = 9765;
            Log.d(TAG, "connectDisconnect: No port inputted.");
        }
        String name ="Brent";

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
                break;
            case R.id.recordBtn:
                break;
            case R.id.forwardBtn:
//                this.sendTask.execute("forward");
                commands.add("forward");
                message.what = SEND_COMMAND;
                message.obj = "forward";
                networkHandlerThread.childThreadHandler.sendMessage(message);
                break;
            case R.id.bakcwardBtn:
                commands.add("backward");
//                this.sendTask.execute("backward");
                break;
            case R.id.rightBtn:
                commands.add("right");
//                this.sendTask.execute("right");
                break;
            case R.id.leftBtn:
                commands.add("left");
//                this.sendTask.execute("left");
                break;
            case R.id.upBtn:
                commands.add("up");
//                this.sendTask.execute("up");
                break;
            case R.id.downBtn:
                commands.add("down");
//                this.sendTask.execute("down");
                break;
        }
        Toast.makeText(this,
                "Command: " + commands.get(commands.size()-1),
                Toast.LENGTH_SHORT).show();
        Log.d(TAG, "updateCommands: Commands Size: " + commands.size());
    }
}
