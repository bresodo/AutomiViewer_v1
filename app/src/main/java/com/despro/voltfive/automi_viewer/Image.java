package com.despro.voltfive.automi_viewer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Image {

    public void SaveImage(String imgName, Bitmap frame) {
        Bitmap _bitmapPreScale = frame;
        int oldWidth = _bitmapPreScale.getWidth();
        int oldHeight = _bitmapPreScale.getHeight();
        int newWidth = 2592;
        int newHeight = 1936;

        float scaleWidth = ((float) newWidth) / oldWidth;
        float scaleHeight = ((float) newHeight) / oldHeight;

        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Bitmap _bitmapScaled = Bitmap.createBitmap(_bitmapPreScale, 0, 0,  oldWidth, oldHeight, matrix, true);
        _bitmapScaled.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
        File f = new File(Environment.getExternalStorageDirectory()
                + File.separator + "test.jpg");
        try{
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            fo.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
