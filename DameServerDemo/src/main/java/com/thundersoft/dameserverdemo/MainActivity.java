package com.thundersoft.dameserverdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.View;

import com.thundersoft.dameserverdemo.video.CamLog;
import com.thundersoft.dameserverdemo.video.CameraRecorder;
import com.thundersoft.dameserverdemo.video.filter.GroupFilter;
import com.thundersoft.dameserverdemo.video.filter.WaterMarkFilter;
import com.thundersoft.dameserverdemo.video.filter.WaterMarkTextFilter;
import com.thundersoft.dameserverdemo.videoprocessor.VideoController;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "crbtest";
    private CameraController mCameraController;

    private VideoController mVideoController;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mVideoController = VideoController.getInstance();
        mVideoController.init(this);




    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        CamLog.d("SurfaceView+  surfaceDestroyed surfaceDestroyed ");
        mVideoController.stopVideoRecord();
    }


    public void openCamera(View view) {
        Surface videoSurface = mVideoController.getVideoSurface();
        mCameraController = CameraController.getInstance(this);
        mCameraController.initCamera(videoSurface);
        mCameraController.openCamera();
    }

    public void openPreview(View view) {
        mVideoController.startVideoRecord();
    }

    public void closePreview(View view) {
        mVideoController.stopVideoRecord();
    }



}
