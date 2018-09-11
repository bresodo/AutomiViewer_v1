package com.despro.voltfive.automi_viewer;

import android.content.Intent;
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

    private ImageView frameView;
    private TextView frameCountView;
    private Button cdcButton;
    private EditText ipText;
    private EditText portText;

    private FrameTask frameTask;

    private ArrayList<String> commands;
    private Intent clientIntent;

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
//                frameTask = new FrameTask(MainActivity.this, frameView, cdcButton, frameCountView, ip, port, "Brentjeffson", serverState);
//                frameTask.execute();
                clientIntent.putExtra("IP", ip);
                clientIntent.putExtra("PORT", port);
                clientIntent.putExtra("CLIENT-NAME", name);
                startService(clientIntent);
                break;
            case "disconnect":
                stopService(clientIntent);
                break;
        }
    }

    public void  updateCommands(View view) {
//            Log.d(TAG, "updateCommands: Unable to add more commands");
//            Toast.makeText(this,
//                    "Unable to add more commands",
//                    Toast.LENGTH_SHORT).show();

        switch (view.getId()){
            case R.id.captureBtn:
                break;
            case R.id.recordBtn:
                break;
            case R.id.forwardBtn:
//                this.sendTask.execute("forward");
                commands.add("forward");
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
