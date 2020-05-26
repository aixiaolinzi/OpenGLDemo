package com.thundersoft.dameserverdemo.video;

import android.content.Context;

import com.thundersoft.dameserverdemo.video.core.Renderer;
import com.thundersoft.dameserverdemo.video.media.SoundRecorder;
import com.thundersoft.dameserverdemo.video.media.SurfaceEncoder;
import com.thundersoft.dameserverdemo.video.media.SurfaceShower;
import com.thundersoft.dameserverdemo.video.media.TextureProvider;
import com.thundersoft.dameserverdemo.video.media.VideoSurfaceProcessor;
import com.thundersoft.dameserverdemo.video.media.store.IHardStore;
import com.thundersoft.dameserverdemo.video.media.store.StrengthenMp4MuxStore;


public class CameraRecorder {
    private Context mContext;

    private VideoSurfaceProcessor mTextureProcessor;
    private TextureProvider mTextureProvider;
    private SurfaceShower mShower;
    private IHardStore mMuxer1;
    private SurfaceEncoder mSurfaceStore1;
    private SoundRecorder mSoundRecord1;



    public CameraRecorder(Context context) {
        mContext = context;
        //用于视频混流和存储
        mMuxer1 = new StrengthenMp4MuxStore(true);

        //用于编码图像
        mSurfaceStore1 = new SurfaceEncoder();
        mSurfaceStore1.setStore(mMuxer1);

        //用于音频
        mSoundRecord1 = new SoundRecorder(mMuxer1);




        //用于预览图像
        mShower = new SurfaceShower();
        mShower.setOutputSize(720, 1280);
        mTextureProvider = new TextureProvider(mContext);

        //用于处理视频图像
        mTextureProcessor = new VideoSurfaceProcessor();
        mTextureProcessor.setTextureProvider(mTextureProvider);
        mTextureProcessor.addObserver(mShower);
        mTextureProcessor.addObserver(mSurfaceStore1);



    }

    public void setRenderer(Renderer renderer) {
        mTextureProcessor.setRenderer(renderer);
    }

    /**
     * 设置预览对象，必须是{@link android.view.Surface}、{@link android.graphics.SurfaceTexture}或者
     * {@link android.view.TextureView}
     *
     * @param surface 预览对象
     */
    public void setSurface(Object surface) {
        mShower.setSurface(surface);
    }

    /**
     * 设置录制的输出路径
     *
     * @param path 输出路径
     */
    public void setOutputPath(String path) {
        mMuxer1.setOutputPath(path);
    }


    /**
     * 设置录制的输出路径
     *
     * @param path 输出路径
     */
    public void setOutputPath1(String path) {
        mMuxer1.setOutputPath(path);
    }





    /**
     * 设置预览大小
     *
     * @param width  预览区域宽度
     * @param height 预览区域高度
     */
    public void setPreviewSize(int width, int height) {
        mShower.setOutputSize(width, height);
    }

    /**
     * 打开数据源
     */
    public void open() {
        mTextureProcessor.start();
    }

    /**
     * 关闭数据源
     */
    public void close() {
        mTextureProcessor.stop();
        stopRecord();
    }

    /**
     * 打开预览
     */
    public void startPreview() {
        mShower.open();
    }

    /**
     * 关闭预览
     */
    public void stopPreview() {
        mShower.close();
    }


    /**
     * 开始录制
     */
    public void startRecord1() {
        mSurfaceStore1.open();
        mSoundRecord1.start();
    }

    /**
     * 关闭录制
     */
    public void stopRecord1() {
        mSoundRecord1.stop();
        mSurfaceStore1.close();
        mMuxer1.close();
    }




    /**
     * 开始录制
     */
    public void startRecord() {
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
