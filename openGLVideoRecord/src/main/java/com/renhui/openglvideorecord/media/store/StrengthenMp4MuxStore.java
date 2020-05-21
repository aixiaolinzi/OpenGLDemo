package com.renhui.openglvideorecord.media.store;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.renhui.openglvideorecord.CamLog;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * StrengthenMp4MuxStore
 */
public class StrengthenMp4MuxStore implements IHardStore {

    private final String tag = getClass().getSimpleName();
    private MediaMuxer mMuxer1;
    private MediaMuxer mMuxer2;
    private boolean ifUseMuxer1;

    private final boolean av;
    private String path;

    private MediaFormat audioMediaFormat;
    private MediaFormat videoMediaFormat;
    private int audioTrack = -1;
    private int videoTrack = -1;

    private int index;
    private long frameIndex;


    private int audioTrack1 = -1;
    private int videoTrack1 = -1;

    private int audioTrack2 = -1;
    private int videoTrack2 = -1;

    private int writeSampleDataIndex = 0;
    private long samplePresentationTimeUs = 33333;


    private final Object Lock = new Object();
    private boolean muxStarted = false;
    private LinkedBlockingQueue<HardMediaData> cache;
    private Recycler<HardMediaData> recycler;
    private ExecutorService exec;
    private final Handler mHandler = new Handler();
    private final Runnable mStopRunnable = new Runnable() {
        @Override
        public void run() {
            mHandler.removeCallbacks(mStopRunnable);
            mHandler.removeCallbacks(mCreateMuxerRunnable);

            mHandler.postDelayed(mStopRunnable, 5000);
            mHandler.postDelayed(mCreateMuxerRunnable, 2000);

            if (ifUseMuxer1) {
                writeSampleDataIndex = 0;
                ifUseMuxer1 = false;
            } else {
                ifUseMuxer1 = true;
            }

        }

    };


    private final Runnable mCreateMuxerRunnable = new Runnable() {
        @Override
        public void run() {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (mMuxer1 == null) {
                        createMuxer1();
                    }
                    if (mMuxer2 == null) {
                        createMuxer2();
                    }
                }
            }).start();


        }
    };
    private long presentationTimeUsMuxer1;
    private long presentationTimeUsMuxer2;


    private void stopcreateMuxer1() {
        if (mMuxer1 != null) {
            try {
                mMuxer1.stop();
                Log.d(tag, "muxer stoped success");
                mMuxer1.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mMuxer1 = null;

        }

    }


    private void stopcreateMuxer2() {
        if (mMuxer2 != null) {
            try {
                mMuxer2.stop();
                Log.d(tag, "muxer stoped success");
                mMuxer2.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mMuxer2 = null;
        }

    }

    private void createMuxer1() {
        synchronized (Lock) {
            try {
                index++;
                String tempPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test" + index + ".mp4";
                mMuxer1 = new MediaMuxer(tempPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            } catch (IOException e) {
                e.printStackTrace();
            }
            audioTrack1 = mMuxer1.addTrack(audioMediaFormat);
            videoTrack1 = mMuxer1.addTrack(videoMediaFormat);
            mMuxer1.start();
        }
    }


    private void createMuxer2() {
        synchronized (Lock) {
            try {
                index++;
                String tempPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test" + index + ".mp4";
                mMuxer2 = new MediaMuxer(tempPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            } catch (IOException e) {
                e.printStackTrace();
            }
            audioTrack2 = mMuxer2.addTrack(audioMediaFormat);
            videoTrack2 = mMuxer2.addTrack(videoMediaFormat);
            mMuxer2.start();
        }
    }


    public StrengthenMp4MuxStore(boolean av) {
        this.av = av;
        cache = new LinkedBlockingQueue<>(30);
        recycler = new Recycler<>();
        exec = new ThreadPoolExecutor(1, 1, 1, TimeUnit.MINUTES,
                new LinkedBlockingQueue<Runnable>(16), Executors.defaultThreadFactory());


        try {
            index++;
            String tempPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test" + index + ".mp4";
            mMuxer1 = new MediaMuxer(tempPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            ifUseMuxer1 = true;


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void close() {
        synchronized (Lock) {
            mHandler.removeCallbacks(mStopRunnable);
            mHandler.removeCallbacks(mCreateMuxerRunnable);
            if (muxStarted) {
                audioTrack = -1;
                videoTrack = -1;
                muxStarted = false;
            }
        }
    }


    private void muxRun() {
        Log.d(tag, "enter mux loop");
        while (muxStarted) {
            try {
                HardMediaData data = cache.poll(1, TimeUnit.SECONDS);
                synchronized (Lock) {
                    Log.d(tag, "data is null?" + (data == null));
                    if (muxStarted && data != null) {

                        if (ifUseMuxer1) {
                            if (data.index == audioTrack) {
                                mMuxer1.writeSampleData(audioTrack1, data.data, data.info);
                            } else if (data.index == videoTrack) {
                                CamLog.e("cache " + cache.size() + " frameIndex" + frameIndex++
                                        + " mMediaCodecBuf.presentationTimeUs " + data.info.presentationTimeUs
                                        + " info.offset " + data.info.offset
                                        + " info.flags " + data.info.flags
                                        + " info.size " + data.info.size);

                                ByteBuffer encodedData = data.data;
                                MediaCodec.BufferInfo mMediaCodecBuf = data.info;

                                long presentationTimeUs = writeSampleDataIndex * samplePresentationTimeUs;
                                // adjust the ByteBuffer values to match BufferInfo (not needed?)
                                encodedData.position(mMediaCodecBuf.offset);
                                encodedData.limit(mMediaCodecBuf.offset + mMediaCodecBuf.size);
                                mMediaCodecBuf.presentationTimeUs = presentationTimeUs;
                                writeSampleDataIndex++;

                                mMuxer1.writeSampleData(videoTrack1, encodedData, mMediaCodecBuf);
                            }
                        } else {
                            if (data.index == audioTrack) {
                                mMuxer1.writeSampleData(audioTrack1, data.data, data.info);
                            } else if (data.index == videoTrack) {
                                mMuxer1.writeSampleData(videoTrack1, data.data, data.info);
                            }
                            if (data.index == audioTrack) {
                                mMuxer2.writeSampleData(audioTrack2, data.data, data.info);
                            } else if (data.index == videoTrack) {


                                CamLog.e("mMuxer2cache" + cache.size() + " frameIndex" + frameIndex++
                                        + " mMediaCodecBuf.presentationTimeUs " + data.info.presentationTimeUs
                                        + " info.offset " + data.info.offset
                                        + " info.flags " + data.info.flags
                                        + " info.size " + data.info.size);


                                final ByteBuffer encodedData = data.data;
                                final MediaCodec.BufferInfo mMediaCodecBuf = data.info;

                                long presentationTimeUs = writeSampleDataIndex * samplePresentationTimeUs;
                                // adjust the ByteBuffer values to match BufferInfo (not needed?)
                                encodedData.position(mMediaCodecBuf.offset);
                                encodedData.limit(mMediaCodecBuf.offset + mMediaCodecBuf.size);
                                mMediaCodecBuf.presentationTimeUs = presentationTimeUs;
                                writeSampleDataIndex++;

                                mMuxer2.writeSampleData(videoTrack2, encodedData, mMediaCodecBuf);

                            }


                        }
                        recycler.put(data.index, data);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (mMuxer1 != null) {
            try {
                mMuxer1.stop();
                Log.d(tag, "muxer stoped success");
                mMuxer1.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mMuxer1 = null;

        }

        if (mMuxer2 != null) {
            try {
                mMuxer2.stop();
                Log.d(tag, "muxer stoped success");
                mMuxer2.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mMuxer2 = null;
        }


        cache.clear();
        recycler.clear();
    }


    public byte[] decodeValue(ByteBuffer bytes) {
        int len = bytes.limit() - bytes.position();
        byte[] bytes1 = new byte[len];
        bytes.get(bytes1);
        return bytes1;
    }


    @Override
    public int addTrack(MediaFormat mediaFormat) {
        int ret = -1;
        synchronized (Lock) {
            if (!muxStarted) {
                if (audioTrack == -1 && videoTrack == -1) {
                    mHandler.postDelayed(mStopRunnable, 5000);
                    mHandler.postDelayed(mCreateMuxerRunnable, 2000);
                }
                String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("audio")) {
                    audioTrack = mMuxer1.addTrack(mediaFormat);
                    audioMediaFormat = mediaFormat;
                    audioTrack1 = ret = audioTrack;
                } else if (mime.startsWith("video")) {
                    videoTrack = mMuxer1.addTrack(mediaFormat);
                    videoMediaFormat = mediaFormat;
                    videoTrack1 = ret = videoTrack;
                }

                startMux();
            }
        }
        return ret;
    }

    private void startMux() {
        boolean canMux = !av || (audioTrack != -1 && videoTrack != -1);
        if (canMux) {
            mMuxer1.start();
            muxStarted = true;
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    muxRun();
                }
            });
        }
    }

    @Override
    public int addData(int track, HardMediaData hardMediaData) {
        if (track >= 0) {
            Log.d(tag, "addData->" + track + "/" + audioTrack + "/" + videoTrack);
            hardMediaData.index = track;
            if (track == audioTrack || track == videoTrack) {
                HardMediaData d = recycler.poll(track);
//                if (d == null) {
                Log.d(tag, "hardMediaData.copy");
                d = hardMediaData.copy();
//                } else {
//                    Log.d(tag, "copyTo");
//                    hardMediaData.copyTo(d);
//                }
                while (!cache.offer(d)) {
                    Log.d(tag, "put data to the cache : poll");
                    HardMediaData c = cache.poll();
                    recycler.put(c.index, c);
                }
            }
        }
        return 0;
    }

    @Override
    public void setOutputPath(String path) {
        this.path = path;
    }
}
