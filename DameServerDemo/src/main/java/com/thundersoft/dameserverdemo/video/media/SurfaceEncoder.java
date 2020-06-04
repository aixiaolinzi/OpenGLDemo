package com.thundersoft.dameserverdemo.video.media;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.opengl.EGLSurface;


import com.thundersoft.dameserverdemo.video.CamLog;
import com.thundersoft.dameserverdemo.video.core.RenderBean;
import com.thundersoft.dameserverdemo.video.media.store.HardMediaData;
import com.thundersoft.dameserverdemo.video.media.store.IStore;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * SurfaceEncoder 从surface上进行硬编码，通过{@link #setStore(IHardStore)}来设置存储器进行存储
 */
public class SurfaceEncoder extends SurfaceShower {

    private final String TAG = "SurfaceEncoder";
    private MediaConfig mConfig = new MediaConfig();
    private MediaCodec mVideoEncoder;
    private boolean isEncodeStarted = false;
    private static final int TIME_OUT = 1000;

    private IStore mStore;
    private int mVideoTrack = -1;

    private long startTime = -1;
    private long index;

    public SurfaceEncoder() {
        super.setOnDrawEndListener(new OnDrawEndListener() {
            @Override
            public void onDrawEnd(EGLSurface surface, RenderBean bean) {
                CamLog.e(TAG, "onDrawEnd start-->" + index++);
                if (bean.timeStamp != -1) {
                    bean.egl.setPresentationTime(surface, bean.timeStamp * 1000);
                } else {
                    if (startTime == -1) {
                        startTime = bean.textureTime;
                    }
                    bean.egl.setPresentationTime(surface, bean.textureTime - startTime);
                }
                videoEncodeStep(false);
                CamLog.e(TAG, "onDrawEnd end-->");
            }
        });
    }

    @Override
    public void onCall(RenderBean rb) {
        if (rb.endFlag) {
            videoEncodeStep(true);
        }
        super.onCall(rb);
    }

    public void setConfig(MediaConfig config) {
        this.mConfig = config;
    }

    public void setStore(IStore store) {
        this.mStore = store;
    }

    @Override
    public void setOutputSize(int width, int height) {
        super.setOutputSize(width, height);
        mConfig.mVideo.width = width;
        mConfig.mVideo.height = height;
    }

    protected MediaFormat convertVideoConfigToFormat(MediaConfig.Video config) {
        MediaFormat format = MediaFormat.createVideoFormat(config.mime, config.width, config.height);
        format.setInteger(MediaFormat.KEY_BIT_RATE, config.bitrate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, config.frameRate);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, config.iframe);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        return format;
    }

    private void openVideoEncoder() {
        CamLog.d(TAG, "openVideoEncoder startTime-->");
        if (mVideoEncoder == null) {
            try {
                MediaFormat format = convertVideoConfigToFormat(mConfig.mVideo);
                mVideoEncoder = MediaCodec.createEncoderByType(mConfig.mVideo.mime);
                mVideoEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                super.setSurface(mVideoEncoder.createInputSurface());
                super.setOutputSize(mConfig.mVideo.width, mConfig.mVideo.height);
                mVideoEncoder.start();
                isEncodeStarted = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        CamLog.d(TAG, "openVideoEncoder endTime-->");
    }

    private void closeVideoEncoder() {
        CamLog.d(TAG, "closeEncoder");
        if (mVideoEncoder != null) {
            mVideoEncoder.stop();
            mVideoEncoder.release();
            mVideoEncoder = null;
        }
    }


    private synchronized boolean videoEncodeStep(boolean isEnd) {
        CamLog.d(TAG, "videoEncodeStep:" + isEncodeStarted + "/" + isEnd);
        if (isEncodeStarted) {
            if (isEnd) {
                mVideoEncoder.signalEndOfInputStream();
            }

            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            while (true) {
                int mOutputIndex = mVideoEncoder.dequeueOutputBuffer(info, TIME_OUT);
                CamLog.i(TAG, "VideoSurfaceProcessor:mOutputIndex=" + mOutputIndex);
                if (mOutputIndex >= 0) {
                    if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        info.size = 0;
                    }
                    ByteBuffer buffer = CodecUtil.getOutputBuffer(mVideoEncoder, mOutputIndex);
                    if (mStore != null) {
                        mStore.addData(mVideoTrack, new HardMediaData(buffer, info));
                    }
                    mVideoEncoder.releaseOutputBuffer(mOutputIndex, false);
                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        closeVideoEncoder();
                        isEncodeStarted = false;
                        CamLog.i(TAG, "videoEncodeStep: MediaCodec.BUFFER_FLAG_END_OF_STREAM ");
                        break;
                    }
                } else if (mOutputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat format = mVideoEncoder.getOutputFormat();
                    if (mStore != null) {
                        mVideoTrack = mStore.addTrack(format);
                    }
                } else if (mOutputIndex == MediaCodec.INFO_TRY_AGAIN_LATER && !isEnd) {
                    break;
                }
            }
        }
        return false;
    }


    @Override
    public void open() {
        openVideoEncoder();
        super.open();
    }

    @Override
    public void close() {
        super.close();
        super.closeSurface();
        videoEncodeStep(true);
        startTime = -1;
    }


    @Override
    public void setSurface(Object surface) {
    }

}