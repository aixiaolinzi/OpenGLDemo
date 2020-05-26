package com.thundersoft.dameserverdemo;

abstract class OnFrameAvailableListener {
    protected int imageFormat;
    protected void setImageFormat(int imageFormat) {
        this.imageFormat = imageFormat;
    }
    abstract void onFrameDataAvaliable(byte[] bytes);

}