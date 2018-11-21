package com.despro.voltfive.automi_viewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class FrameTask_working_backup extends AsyncTask<String, String, Bitmap> {
    private Context context;
    private ImageView frameView;
    private TextView frameCountView;

    private int frameCount;
    private String ip;
    private int port;

    public FrameTask_working_backup(Context context, ImageView frameView, TextView frameCountView, int frameCount, String ip, int port) {
        this.context = context;
        this.frameCountView = frameCountView;
        this.frameView = frameView;

        this.frameCount = frameCount;
        this.ip = ip;
        this.port = port;
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        Bitmap bitmap = null;
        try{
            DatagramSocket socket = new DatagramSocket();
            InetAddress ip = InetAddress.getByName(this.ip);
            byte outBuffer[] = new byte[1024];
            byte inBuffer[] = new byte[1024];
            String sentence = "This is a sentence sent through udp socket which comes from socket.";
            outBuffer = sentence.getBytes();
            DatagramPacket outPacket = new DatagramPacket(outBuffer, outBuffer.length, ip, this.port);
            DatagramPacket inPacket = new DatagramPacket(inBuffer, inBuffer.length);

            boolean isReceived = false;
            while(!isReceived) {
                socket.send(outPacket);

                socket.receive(inPacket);
                int received_size = 0;
                int image_size = Integer.parseInt(new String(inPacket.getData()).trim());
                int bytes_received = 0;

                StringBuilder jpg_builder = new StringBuilder(image_size);
                while(image_size > bytes_received) {
                    socket.receive(inPacket);
                    String data = new String(inPacket.getData()).trim();
                    jpg_builder.append(data);
                    bytes_received += data.length();
                }
                jpg_builder.delete(image_size, jpg_builder.length());
                received_size = jpg_builder.length();

                publishProgress(String.valueOf(received_size == image_size));

                if(received_size == image_size) {
                    isReceived = true;
                    byte[] imageByte = Base64.decode(jpg_builder.toString(), Base64.DEFAULT);
                    bitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
                } else{
                    isReceived = false;
                }
            }

            socket.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        frameView.setImageBitmap(bitmap);
        frameCount++;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        Toast.makeText(context.getApplicationContext(), values[0], Toast.LENGTH_LONG).show();
    }
    //    @Override
//    protected void onPreExecute() {
//        frameCountView.setText("Count: ".concat(Integer.toString(frameCount)));
//    }

}
