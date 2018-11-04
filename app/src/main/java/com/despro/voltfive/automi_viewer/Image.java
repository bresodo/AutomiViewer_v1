package com.despro.voltfive.automi_viewer;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;


public class Image {
    private static final String TAG = "Image";
    private WeakReference<Context> context;

    public Image(Context context) {
        this.context = new WeakReference<>(context);
    }
//    public void SaveImage(String imgName, Bitmap frame) {
//        Bitmap _bitmapPreScale = frame;
//        int oldWidth = _bitmapPreScale.getWidth();
//        int oldHeight = _bitmapPreScale.getHeight();
//        int newWidth = 2592;
//        int newHeight = 1936;
//
//        float scaleWidth = ((float) newWidth) / oldWidth;
//        float scaleHeight = ((float) newHeight) / oldHeight;
//
//        Matrix matrix = new Matrix();
//        // resize the bit map
//        matrix.postScale(scaleWidth, scaleHeight);
//        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//        Bitmap _bitmapScaled = Bitmap.createBitmap(_bitmapPreScale, 0, 0,  oldWidth, oldHeight, matrix, true);
//        _bitmapScaled.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
//        File f = new File(Environment.getExternalStorageDirectory()
//                + File.separator + "test.jpg");
//        try{
//            f.createNewFile();
//            FileOutputStream fo = new FileOutputStream(f);
//            fo.write(bytes.toByteArray());
//            fo.close();
//        }catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
    public boolean SaveImage(String filename, Bitmap img) {
//        File filepath = new File(context.get().getFilesDir(), filename);
//        Log.d(TAG, "SaveImage: " + filepath);
//        ContextWrapper wrapper = new ContextWrapper(context.get().getApplicationContext());
//        File storage = null;
//        File image = null;
        try{
//            File file = wrapper.getDir("images", context.get().MODE_PRIVATE);
//            file = new File(file, filename+".jpg");
            //Creating path
//            storage = new File(Environment.getExternalStorageDirectory() + "/automi");
            File filepath_downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            Log.d(TAG, "SaveImage: Saving image in " + filepath_downloads.getAbsolutePath());
            File file = new File(filepath_downloads, filename + ".jpg");
            Log.d(TAG, "SaveImage: File to be saved " + file.getAbsolutePath());
//            if(!storage.exists()) {
                //Create folder for path
//                storage.mkdir();
//            }
//            image = new File(storage, filename + ".jpg");
//            if(!image.exists()) {
//                image.createNewFile();
//                Log.i(TAG, "SaveImage: File Created...");
//            }
            Log.d(TAG, "SaveImage: Saving image...");
            FileOutputStream fout = new FileOutputStream(filename + ".jpg");
            img.compress(Bitmap.CompressFormat.JPEG, 100, fout);
            fout.flush();
            fout.close();
            Log.d(TAG, "SaveImage: Image saved.");
            return true;
        }catch(Exception e) {
            Log.d(TAG, "SaveImage: Unable to save image.");
            e.printStackTrace();
            return false;
        }
    }
}
