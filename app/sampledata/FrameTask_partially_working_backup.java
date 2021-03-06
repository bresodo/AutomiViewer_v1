package com.despro.voltfive.automi_viewer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class FrameTask_partially_working_backup extends AsyncTask<String, Void, Bitmap> {
    private ImageView frameView;
    private TextView frameCountView;

    private int frameCount;
    private String ip;
    private int port;

    public FrameTask_partially_working_backup(ImageView frameView, TextView frameCountView, int frameCount, String ip, int port) {
//        this.context = context;
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
            String sentence = "This is a sentence sent through udp socket which comes from client.";
            outBuffer = sentence.getBytes();
            DatagramPacket outPacket = new DatagramPacket(outBuffer, outBuffer.length, ip, this.port);
            DatagramPacket inPacket = new DatagramPacket(inBuffer, inBuffer.length);
            socket.send(outPacket);

            socket.receive(inPacket);
            int image_size = Integer.parseInt(new String(inPacket.getData()).trim());
            int bytes_received = 0;

            StringBuilder jpg_builder = new StringBuilder(image_size);
            while(image_size > bytes_received) {
                socket.receive(inPacket);
                String data = new String(inPacket.getData()).trim();
                jpg_builder.append(data);
//            jpg_builder.append(new String(inPacket.getData()).trim());
                bytes_received += data.length();
            }

            jpg_builder.delete(image_size, jpg_builder.length());

            byte[] imageByte = Base64.decode(jpg_builder.toString(), Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
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

//    @Override
//    protected void onPreExecute() {
//        frameCountView.setText("Count: ".concat(Integer.toString(frameCount)));
//    }

}
