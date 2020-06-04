package com.thundersoft.dameserverdemo.videoprocessor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Surface;

import com.thundersoft.dameserverdemo.R;
import com.thundersoft.dameserverdemo.video.CameraRecorder;
import com.thundersoft.dameserverdemo.video.filter.GroupFilter;
import com.thundersoft.dameserverdemo.video.filter.WaterMarkFilter;
import com.thundersoft.dameserverdemo.video.filter.WaterMarkTextFilter;

public class VideoController {
    private CameraRecorder mCameraRecorder;


    private static class VideoControllerHolder {
        private static final VideoController INSTANCE = new VideoController();
    }

    private VideoController() {
        mCameraRecorder = new CameraRecorder();
    }

    public static final VideoController getInstance() {
        return VideoControllerHolder.INSTANCE;
    }



    public int startVideoRecord() {
        GroupFilter filter = new GroupFilter();
        mCameraRecorder.setRenderer(filter);

        filter.addFilter(new WaterMarkFilter().
                setMarkPosition(125, 600, 227 * 2, 39 * 2));

        filter.addFilter(new WaterMarkTextFilter().setMarkPosition(500, 500, 400, 120));

        mCameraRecorder.startRecord();


        return 0;
    }


    public int stopVideoRecord() {
        mCameraRecorder.close();
        return 0;
    }


    public int setAudioEnable(boolean enable) {
        return 0;
    }

    public Surface getVideoSurface() {
        return mCameraRecorder.getVideoSurface();
    }


    public int setPicWatermarkEnable(boolean status) {
        return 0;
    }


    public int setPicWatermarkPath(String imagePath) {
        return 0;
    }

    public int setPicWatermarkLocation(int left, int top) {
        return 0;
    }

    public int setPicWatermarkSize(int width, int height) {
        return 0;
    }


    public int setTimeWatermarkEnable(boolean status) {

        return 0;
    }


    public int setTimeWatermarkLocation(int left, int top) {

        return 0;
    }


    public int setTimeWatermarkColor(int color) {

        return 0;
    }

    public int setTimeWatermarkTextSize(float textSize) {

        return 0;
    }


    public int setStorageResolution(int resolution) {

        return 0;
    }


    public int setVideoSegmentPeriod(int second) {
        return 0;
    }


}
