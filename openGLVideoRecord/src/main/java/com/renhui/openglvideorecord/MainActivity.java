package com.renhui.openglvideorecord;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.renhui.openglvideorecord.filter.GroupFilter;
import com.renhui.openglvideorecord.filter.WaterMarkFilter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SurfaceView mSurfaceView;
    private TextView mTvPreview, mTvRecord;
    private boolean isPreviewOpen = false;
    private boolean isRecordOpen = false;

    private CameraRecorder mCamera;

    private String tempPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.mp4";
    private int index;


    private Handler mHander = new Handler();
    private Runnable stop1 = new Runnable() {
        @Override
        public void run() {
            mCamera.stopRecord1();
        }
    };
    private Runnable stop2 = new Runnable() {
        @Override
        public void run() {
            mCamera.stopRecord2();
        }
    };


    private Runnable startNext = new Runnable() {
        @Override
        public void run() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    StartVideo();
                }
            }).start();

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSurfaceView = (SurfaceView) findViewById(R.id.mSurfaceView);
        mTvRecord = (TextView) findViewById(R.id.mTvRec);
        mTvPreview = (TextView) findViewById(R.id.mTvShow);

        mCamera = new CameraRecorder(this);
//        mCamera.setOutputPath(tempPath);

        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                CamLog.d("SurfaceView+  surfaceCreated create ");
                GroupFilter filter = new GroupFilter(getResources());
                mCamera.setRenderer(filter);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.image);
                filter.addFilter(new WaterMarkFilter().
                        setMarkPosition(125, 600, 280 * 2, 39 * 2).setMark(bitmap));

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                CamLog.d("SurfaceView+  surfaceChanged change width" + width + " height " + height);
                mCamera.open();
                mCamera.setSurface(holder.getSurface());
                mCamera.setPreviewSize(width, height);
                mCamera.startPreview();
                isPreviewOpen = true;
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                CamLog.d("SurfaceView+  surfaceDestroyed surfaceDestroyed ");
                mCamera.stopPreview();
                mCamera.close();
            }
        });

        setFullScreenConfigs();

        initPermission();
    }


    private void initPermission() {
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.CAMERA);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.RECORD_AUDIO);
        }
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), 1);
        } else {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHander.removeCallbacks(null);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mTvShow:
                isPreviewOpen = !isPreviewOpen;
                mTvPreview.setText(isPreviewOpen ? "关预览" : "开预览");
                if (isPreviewOpen) {
                    mCamera.startPreview();
                } else {
                    mCamera.stopPreview();
                }
                break;
            case R.id.mTvRec:
                mTvRecord.setEnabled(false);
                StartVideo();
                break;
            default:
                break;
        }
    }

    private void StartVideo() {
        mHander.postDelayed(startNext, 10000);
        isRecordOpen = !isRecordOpen;
        mTvRecord.setText(isRecordOpen ? "使用2录制" : "使用1录制");
        String tempPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test/test" + index++ + ".mp4";
        if (isRecordOpen) {
            mCamera.setOutputPath1(tempPath);
            mCamera.startRecord1();
            mHander.postDelayed(stop2, 0);
        } else {
            mCamera.setOutputPath2(tempPath);
            mCamera.startRecord2();
            mHander.postDelayed(stop1, 0);
//                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            Intent v = new Intent(Intent.ACTION_VIEW);
//                            v.setDataAndType(Uri.parse(tempPath), "video/mp4");
//                            if (v.resolveActivity(getPackageManager()) != null) {
//                                startActivity(v);
//                            } else {
//                                Toast.makeText(MainActivity.this, "无法找到默认媒体软件打开:" + tempPath, Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    }, 1000);
        }
    }


    private void setFullScreenConfigs() {
        int flag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(flag);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
    }

    public void onBack(View view) {

        finish();
    }
}
