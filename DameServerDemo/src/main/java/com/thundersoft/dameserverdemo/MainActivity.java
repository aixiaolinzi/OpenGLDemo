package com.thundersoft.dameserverdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Camera;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "crbtest";
    private CameraController mCameraController;
    private DataFeeder mDataFeeder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCameraController = CameraController.getInstance(this);
        mCameraController.initCamera(null);
        mDataFeeder = new DataFeeder();
        mCameraController.setDataFeeder(mDataFeeder);
        mCameraController.openCamera();
        //mCameraController.startBackgroudCamera();
    }
}
