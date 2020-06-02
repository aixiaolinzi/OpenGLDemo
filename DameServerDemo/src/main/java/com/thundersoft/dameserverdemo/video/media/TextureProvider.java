package com.thundersoft.dameserverdemo.video.media;

import android.content.Context;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;

import com.thundersoft.dameserverdemo.CameraController;
import com.thundersoft.dameserverdemo.video.CamLog;

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


        mSurfaceTexture.setDefaultBufferSize(2048, 1536);

        Surface surface1 = new Surface(mSurfaceTexture);

        mCameraController = CameraController.getInstance(mContext);
        mCameraController.initCamera(surface1);
        mCameraController.openCamera();

        surface.setOnFrameAvailableListener(frameListener);

        size.x = 1536;
        size.y = 2048;
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
