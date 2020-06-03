package com.thundersoft.dameserverdemo.videoprocessor;

import android.view.Surface;

public class VideoController {

    private static class VideoControllerHolder {
        private static final VideoController INSTANCE = new VideoController();
    }

    private VideoController() {



    }

    public static final VideoController getInstance() {
        return VideoControllerHolder.INSTANCE;
    }





    public int startVideoRecord() {
        return 0;
    }


    public int stopVideoRecord() {
        return 0;
    }


    public int setAudioEnable(boolean enable) {
        return 0;
    }

    public Surface getVideoSurface() {

        return null;
    }



}
