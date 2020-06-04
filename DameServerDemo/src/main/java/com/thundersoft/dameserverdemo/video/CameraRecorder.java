package com.thundersoft.dameserverdemo.video;

import android.content.Context;
import android.view.Surface;

import com.thundersoft.dameserverdemo.video.core.Renderer;
import com.thundersoft.dameserverdemo.video.media.SoundRecorder;
import com.thundersoft.dameserverdemo.video.media.SurfaceEncoder;
import com.thundersoft.dameserverdemo.video.media.VideoSurfaceProcessor;
import com.thundersoft.dameserverdemo.video.media.store.IStore;
import com.thundersoft.dameserverdemo.video.media.store.StrengthenMp4MuxStore;


public class CameraRecorder {
    private VideoSurfaceProcessor mTextureProcessor;

    private IStore mMuxer1;
    private SurfaceEncoder mSurfaceStore1;
    private SoundRecorder mSoundRecord1;


    public CameraRecorder() {

    }

    public CameraRecorder(Context context) {
        //用于视频混流和存储
        mMuxer1 = new StrengthenMp4MuxStore(true);

        //用于编码图像
        mSurfaceStore1 = new SurfaceEncoder();
        mSurfaceStore1.setStore(mMuxer1);

        //用于音频
        mSoundRecord1 = new SoundRecorder(mMuxer1);

        //用于处理视频图像
        mTextureProcessor = new VideoSurfaceProcessor(context);
        mTextureProcessor.addObserver(mSurfaceStore1);
    }

    public Surface getVideoSurface() {
        Surface surface = mTextureProcessor.getVideoSurface();
        return surface;
    }


    public void setRenderer(Renderer renderer) {
        mTextureProcessor.setRenderer(renderer);
    }





    /**
     * 关闭数据源
     */
    public void close() {
        mTextureProcessor.stop();
        stopRecord();
    }

    /**
     * 开始录制
     */
    public void startRecord() {
        mTextureProcessor.start();
        mSurfaceStore1.open();
        mSoundRecord1.start();
    }

    /**
     * 关闭录制
     */
    public void stopRecord() {
        mSoundRecord1.stop();
        mSurfaceStore1.close();
        mMuxer1.close();
    }


}
