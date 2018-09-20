package com.despro.voltfive.automi_viewer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client {
    private static final String TAG = "Client";

    private String name;
    private String ip;
    private int port;

    private Socket client;
    private DataInputStream in;
    private DataOutputStream out;

    public boolean connect(String ip, int port, String name) {
        this.name = name;
        this.ip = ip;
        this.port = port;

        try{
            Log.d(TAG, "startServer: Connecting client to server at " + ip + ":" + port);
            client = new Socket(ip, port);
            in = new DataInputStream(client.getInputStream());
            out = new DataOutputStream(client.getOutputStream());

            Log.d(TAG, "startServer: Sending name to server.");
            out.write(name.getBytes(), 0, name.getBytes().length);
            return true;

        }catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "startServer: Unable to connect to server. -> " + e.getMessage());
            return false;
        }
    }

    public boolean disconnect() {
        try{
            Log.d(TAG, "disconnect: Disconnecting from server");
            client.close();
            in.close();
            out.close();
            return true;
        }catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "disconnect: Unable to disconnect from server.");
            return false;
        }
    }

    public Bitmap receiveFrame() {
        try{
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
                while ((read = in.read(frameBuffer, 0, Math.min(frameBuffer.length, remaining))) > 0) {
                    totalRead += read;
                    remaining -= read;
                    stringBuffer.append(new String(frameBuffer, 0, read));
                }

                String frameContent = stringBuffer.toString();
                Log.d(TAG, "doInBackground: Frame Received...");
                byte[] imageByte = Base64.decode(frameContent.toString(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
                return bitmap;
            }
        }catch (IOException e) {
            Log.d(TAG, "receiveFrame: Disconnected -> Frame not received.");
        }
        return null;
    }

    public boolean sendCommand(String command) {
        try {
            Log.d(TAG, "sendCommand: Sending command to server.");
            if(command.equals("")){
                command = "alive";
                out.write(command.getBytes());
            } else{
                out.write(command.getBytes());
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "sendCommand: Unable to send command. -> Disconnected");
            return false;
        }
    }

}
