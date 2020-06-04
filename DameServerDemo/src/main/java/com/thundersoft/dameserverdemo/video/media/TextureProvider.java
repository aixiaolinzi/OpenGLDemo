package com.thundersoft.dameserverdemo.video.media;

import android.graphics.SurfaceTexture;

import com.thundersoft.dameserverdemo.video.CamLog;

import java.util.concurrent.Semaphore;

/**
 * 纹理提供者---其实就是相机控制逻辑
 */
public class TextureProvider {
    private Semaphore mFrameSem;

    public TextureProvider() {

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
    public void setOnFrameAvailableListener(final SurfaceTexture surface) {
        mFrameSem = new Semaphore(0);
        surface.setOnFrameAvailableListener(frameListener);
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
