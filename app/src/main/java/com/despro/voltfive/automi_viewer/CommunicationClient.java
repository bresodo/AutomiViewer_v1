package com.despro.voltfive.automi_viewer;

import android.util.Log;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class CommunicationClient {
    private static final String TAG = "CommunicationClient";

    private String name;
    private String ip;
    private int port;

    private Socket socket;

    private DataOutputStream out;
    private DataInputStream in;

    public CommunicationClient(String ip, int port, String name) {
        this.name = name;
        this.ip = ip;
        this.port = port;
    }

    public boolean connect() {
        try{
            Log.d(TAG, "connect: Connecting to server at " + ip + ":" + port);
            socket = new Socket(ip, port);
            Log.d(TAG, "connect: Connected.");
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            byte name_byte[] = name.getBytes();
            out.write(name_byte, 0, name_byte.length);
            Log.d(TAG, "connect: Sending name to server.");
            return true;
        }
        catch(IOException e) {
            Log.d(TAG, "connect: Unable to connect to server. -> " + e.getMessage());
//            e.printStackTrace();
            return false;
        }
    }

    public boolean disconnect() {
        if(socket != null) {
            try{
                Log.d(TAG, "disconnect: Closing connection to server.");
                socket.close();
                return true;
            }catch (IOException e) {
                Log.d(TAG, "disconnect: Unknown error -> " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean sendMessage(String message) {
        try{
            Log.d(TAG, "sendMessage: Sending message.");
            byte buffer[] = message.getBytes();
            out.write(buffer, 0, buffer.length);
            Log.d(TAG, "sendMessage: Message sent successfully.");
            return true;
        } catch (IOException e) {
            Log.d(TAG, "sendMessage: Unable to send message -> Server cannot be reached," + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
