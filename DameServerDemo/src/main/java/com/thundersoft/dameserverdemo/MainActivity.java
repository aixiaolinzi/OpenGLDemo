package com.thundersoft.dameserverdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.thundersoft.dameserverdemo.video.CamLog;
import com.thundersoft.dameserverdemo.video.CameraRecorder;
import com.thundersoft.dameserverdemo.video.filter.GroupFilter;
import com.thundersoft.dameserverdemo.video.filter.WaterMarkFilter;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "crbtest";
    private CameraController mCameraController;
    private DataFeeder mDataFeeder;


    private CameraRecorder mCamera;

    private String tempPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test23.mp4";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCamera = new CameraRecorder(this);
        mCamera.setOutputPath(tempPath);

        GroupFilter filter = new GroupFilter(getResources());
        mCamera.setRenderer(filter);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.image);
        filter.addFilter(new WaterMarkFilter().
                setMarkPosition(125, 600, 227 * 2, 39 * 2).setMark(bitmap));
        mCamera.open();


//        mCameraController = CameraController.getInstance(this);
//        mCameraController.initCamera(null);
//        mDataFeeder = new DataFeeder();
//        mCameraController.setDataFeeder(mDataFeeder);
//        mCameraController.openCamera();
        //mCameraController.startBackgroudCamera();
    }

    public void openPreview(View view) {
        mCamera.startRecord1();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CamLog.d("SurfaceView+  surfaceDestroyed surfaceDestroyed ");
        mCamera.stopPreview();
        mCamera.close();
    }

    public void closePreview(View view) {
        mCamera.stopPreview();
        mCamera.close();
    }
}
