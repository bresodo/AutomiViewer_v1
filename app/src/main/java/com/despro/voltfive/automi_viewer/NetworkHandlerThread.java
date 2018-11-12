package com.despro.voltfive.automi_viewer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NetworkHandlerThread extends HandlerThread {
    private static final String TAG = "Network-Thread";
    private static final int DEFAULT_THREAD_POOL_SIZE = 4;
    private static final int START = 1;
    private static final int STOP = 2;
    private static final int CLIENT_CONNECTED = 3;
    private static final int CLIENT_DISCONNECTED = 4;
    private static final int RECEIVE_COMMAND = 11;
    private static final int SEND_FRAME = 21;
    private static final int SAVE_IMAGE = 31;
    private static final int SAVE_VIDEO = 32;

    private Client client;
    private Image image;

    private WeakReference<Context>context;

    private ExecutorService executorService;
    private Future loopFuture;

    private String command = "";
    private boolean isConnected = false;
    private String name;
    private String ip;
    private int port;

    private Bitmap bitmap;

    public Handler childThreadHandler;
    public Handler mainThreadHandler;


    public NetworkHandlerThread(Context context, String name, Handler mainThreadHandler) {
        super(name);
        this.mainThreadHandler = mainThreadHandler;
        this.context = new WeakReference<>(context);
        this.client = new Client();
        this.image = new Image(context);
        this.executorService = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
        loopFuture = executorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                networkLoop();
                return null;
            }
        });
    }

    @Override
    protected void onLooperPrepared() {

        childThreadHandler = new Handler(getLooper()){
            @Override
            public void handleMessage(Message msg) {
                Message response = new Message();
                switch (msg.what){
                    case START:
                        String[] content = (String[]) msg.obj;
                        ip = content[0];
                        port = Integer.parseInt(content[1]);
                        name = content[2];
                        Log.i(TAG, "handleMessage: Connecting "+ name +" to server at " + ip + ":" + port);
                        isConnected = client.connect(ip, port, name);
                        if (isConnected){
                            mainThreadHandler.sendEmptyMessage(CLIENT_CONNECTED);
                            executorService.submit(new Callable<Object>() {
                                @Override
                                public Object call() throws Exception {
                                    networkLoop();
                                    return null;
                                }
                            });
                            Log.i(TAG, "handleMessage: Client connected is now connected to the server.");
                        }else {
                            mainThreadHandler.sendEmptyMessage(CLIENT_DISCONNECTED);
                            Log.i(TAG, "handleMessage: Client is unable to connect to the server.");
                        }
                        break;
                    case STOP:
                        if(client.disconnect()) {
                            isConnected = false;
                            mainThreadHandler.sendEmptyMessage(CLIENT_DISCONNECTED);
                            Log.i(TAG, "handleMessage: Client disconnected from server.");
                        }else {
                            isConnected = true;
                            mainThreadHandler.sendEmptyMessage(CLIENT_CONNECTED);
                            Log.i(TAG, "handleMessage: Client unable to disconnect from server.");
                        }
                        break;
                    case RECEIVE_COMMAND:
                        Log.i(TAG, "handleMessage: Got new command from main thread.");
                        command = msg.obj.toString();
                        break;
                    case SAVE_IMAGE:
                        Log.i(TAG, "handleMessage: Saving image...");
                        if(isConnected) {
                            String uid = UUID.randomUUID().toString();
                            String filename = "image-"+uid;
//                            image.SaveImage(name, bitmap);
                            File filepath_downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                            Log.d(TAG, "handleMessage: Saving image in " + filepath_downloads.getAbsolutePath());
                            File baseDir = new File(filepath_downloads + File.separator + "automi" + File.separator + "images");
                            if(!baseDir.exists()) {
                                baseDir.mkdirs();
                            }
                            File file = new File(baseDir, filename + ".jpg");
                            Log.d(TAG, "handleMessage: File to be saved " + file.getAbsolutePath());
                            try (FileOutputStream out = new FileOutputStream(file)) {
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
                                // PNG is a lossless format, the compression factor (100) is ignored
                                out.flush();
                            } catch (IOException e) {
                                Log.d(TAG, "handleMessage: Unable to save image...");
                                e.printStackTrace();
                            }
                            Log.i(TAG, "handleMessage: Image \"" + filename + ".jpg\" saved.");
                        }
                        break;
                    case SAVE_VIDEO:
                        Log.i(TAG, "handleMessage: Saving Video");

                        break;
                }
            }
        };
    }

    private void networkLoop() {

        while (isConnected) {
            Log.d(TAG, "networkLoop: Sending command...");
            boolean sent = client.sendCommand(command);
            if(sent) {
                command = "";
                Log.d(TAG, "networkLoop: Receiving frame...");
                bitmap = client.receiveFrame();
                Message response = new Message();
                response.what = SEND_FRAME;
                response.obj = bitmap;
                mainThreadHandler.sendMessage(response);
            } else {
                isConnected = false;
                mainThreadHandler.sendEmptyMessage(CLIENT_DISCONNECTED);
                Log.d(TAG, "handleMessage: Server disconnected client.");
                return;
            }
        }
        Log.d(TAG, "handleMessage: Client disconnected from server.");
    }
}
