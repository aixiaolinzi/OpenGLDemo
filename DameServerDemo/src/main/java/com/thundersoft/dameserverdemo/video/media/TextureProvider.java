package com.thundersoft.dameserverdemo.video.media;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.thundersoft.dameserverdemo.CameraController;
import com.thundersoft.dameserverdemo.video.CamLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * 纹理提供者---其实就是相机控制逻辑
 */
public class TextureProvider {

    private Context mContext;

    private Semaphore mFrameSem;

    private CameraController mCameraController;

    private SurfaceTexture mSurfaceTexture;



    public TextureProvider(Context mContext) {
        this.mContext = mContext;
    }

    // 视频帧监听器
    private SurfaceTexture.OnFrameAvailableListener frameListener = new SurfaceTexture.OnFrameAvailableListener() {

        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            CamLog.d("timestamp TextureProvider", "onFrameAvailable");
            mFrameSem.drainPermits();
            mFrameSem.release();
        }

    };

    /**
     * 打开视频流数据源（摄像头）
     *
     * @param surface 数据流输出到此
     * @return 视频流的宽高
     */
    public Point open(final SurfaceTexture surface) {
        this.mSurfaceTexture = surface;
        final Point size = new Point();

        mFrameSem = new Semaphore(0);

        Surface surface1 = new Surface(mSurfaceTexture);

        mCameraController = CameraController.getInstance(mContext);
        mCameraController.initCamera(surface1);
        mCameraController.openCamera();

        surface.setOnFrameAvailableListener(frameListener);

        size.x = 720;
        size.y = 1920;
        Log.i("TextureProvider", "Camera Opened");

        return size;
    }




    /**
     * 关闭视频流数据源
     */
    public void close() {
        mFrameSem.drainPermits();
        mFrameSem.release();

    }

    /**
     * 获取一帧数据
     *
     * @return 是否最后一帧
     */
    public boolean frame() {
        try {
            mFrameSem.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取当前帧时间戳
     *
     * @return 时间戳
     */
    public long getTimeStamp() {
        return -1;
    }

    /**
     * 视频流是否是横向的
     *
     * @return true or false
     */
    public boolean isLandscape() {
        return true;
    }



}
