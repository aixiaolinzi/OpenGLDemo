package com.thundersoft.dameserverdemo;

import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.Looper;
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


    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;
    
    public DataFeeder() {
        Looper.prepare();
        Looper mainLooper = Looper.getMainLooper();
        mBackgroundHandler=new Handler(mainLooper);
        this.mListenerList = new ArrayList<>();
        mImageReader = ImageReader.newInstance(1280, 720, ImageFormat.YUV_420_888, 2);
        mImageReader.setOnImageAvailableListener(new ImageAvailableListener(), mBackgroundHandler);
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
