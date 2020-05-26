package com.thundersoft.dameserverdemo;

import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;
import android.util.Log;
import android.view.Surface;

import java.util.ArrayList;
import java.util.List;


/**
 * @author weidong xiao
 */
public class DataFeeder {
    private final static String TAG = "crbtest";

    private ImageReader mImageReader;

    private List<OnFrameAvailableListener> mListenerList;
    
    public DataFeeder() {
        this.mListenerList = new ArrayList<>();
        mImageReader = ImageReader.newInstance(1280, 720, ImageFormat.YUV_420_888, 2);
        mImageReader.setOnImageAvailableListener(new ImageAvailableListener(), null);
    }
    
    public Surface getSurface(){
        return mImageReader.getSurface();
    }
    
    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireNextImage();
            if (image == null) {
                return;
            }
            Log.d(TAG, "onImageAvailable: do sth");
            image.close();
        }
    }

}
