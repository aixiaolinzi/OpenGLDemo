package com.thundersoft.dameserverdemo.video.egl;

import android.annotation.TargetApi;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.os.Build;

import com.thundersoft.dameserverdemo.video.CamLog;


/**
 * EGLHelper
 */
public class EglHelper {

    private boolean isDebug = true;
    private EGLDisplay mEGLDisplay;
    private EGLConfig mEGLConfig;
    private EGLContext mEGLContext;
    private EGLSurface mEGLSurface;

    public EglHelper(int display) {
        changeDisplay(display);
    }

    public EglHelper() {
        this(EGL14.EGL_DEFAULT_DISPLAY);
    }

    public void changeDisplay(int key) {
        //创建和初始化与本地EGL显示的连接
        mEGLDisplay = EGL14.eglGetDisplay(key);
        //获取版本号，[0]为版本号，[1]为子版本号
        int[] versions = new int[2];
        //初始化EGL内部数据结构，返回EGL实现的主版本号和次版本号
        EGL14.eglInitialize(mEGLDisplay, versions, 0, versions, 1);
        log(EGL14.eglQueryString(mEGLDisplay, EGL14.EGL_VENDOR));
        log(EGL14.eglQueryString(mEGLDisplay, EGL14.EGL_VERSION));
        log(EGL14.eglQueryString(mEGLDisplay, EGL14.EGL_EXTENSIONS));
    }

    public EGLConfig getConfig(EGLConfigAttrs attrs) {
        EGLConfig[] configs = new EGLConfig[1];
        int[] configNum = new int[1];
        EGL14.eglChooseConfig(mEGLDisplay, attrs.build(), 0, configs, 0, 1, configNum, 0);
        //选择的过程可能出现多个，也可能一个都没有，这里只用一个
        if (configNum[0] > 0) {
            if (attrs.isDefault()) {
                mEGLConfig = configs[0];
            }
            return configs[0];
        }
        return null;
    }

    public EGLConfig getDefaultConfig() {
        return mEGLConfig;
    }

    public EGLSurface getDefaultSurface() {
        return mEGLSurface;
    }

    public EGLContext getDefaultContext() {
        return mEGLContext;
    }

    public EGLContext createContext(EGLConfig config, EGLContext share, EGLContextAttrs attrs) {
        EGLContext context = EGL14.eglCreateContext(mEGLDisplay, config, share, attrs.build(), 0);
        if (attrs.isDefault()) {
            mEGLContext = context;
        }
        return context;
    }



    public EGLSurface createWindowSurface(Object surface) {
        mEGLSurface = EGL14.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, surface, new int[]{EGL14.EGL_NONE}, 0);
        return mEGLSurface;
    }


    public boolean createGLESWithSurface(EGLConfigAttrs attrs, EGLContextAttrs ctxAttrs, Object surface) {
        EGLConfig config = getConfig(attrs.surfaceType(EGL14.EGL_WINDOW_BIT).makeDefault(true));
        if (config == null) {
            log("getConfig failed : " + EGL14.eglGetError());
            return false;
        }
        mEGLContext = createContext(config, EGL14.EGL_NO_CONTEXT, ctxAttrs.makeDefault(true));
        if (mEGLContext == EGL14.EGL_NO_CONTEXT) {
            log("createContext failed : " + EGL14.eglGetError());
            return false;
        }
        mEGLSurface = createWindowSurface(surface);
        if (mEGLSurface == EGL14.EGL_NO_SURFACE) {
            log("createWindowSurface failed : " + EGL14.eglGetError());
            return false;
        }
        if (!EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)) {
            log("eglMakeCurrent failed : " + EGL14.eglGetError());
            return false;
        }
        return true;
    }

    public boolean makeCurrent(EGLSurface draw, EGLSurface read, EGLContext context) {
        if (!EGL14.eglMakeCurrent(mEGLDisplay, draw, read, context)) {
            log("eglMakeCurrent failed : " + EGL14.eglGetError());
        }
        return true;
    }

    public boolean makeCurrent(EGLSurface surface, EGLContext context) {
        return makeCurrent(surface, surface, context);
    }

    public boolean makeCurrent(EGLSurface surface) {
        return makeCurrent(surface, mEGLContext);
    }


    /**
     * 由于MediaCodec的设计是面向实时视频画面流编码的使用场景，所以MediaCodec会根据用户向其输入画面的速度来对编码的速度进行调节。
     * 如果我们不通过`eglPresentationTimeANDROID`来在编码之前对画面的时间戳进行设置，
     * 那么MediaCodec往往会将我们向其输入画面的速度默认为实时速度，来对编码速度进行调节。
     * 这种调节会造成码率降低，视频画面清晰度降低。
     *
     * 我们在运行完OpenGL相关绘制命令，在调用`swapbuffer`之前需要调用`eglPresentationTimeANDROID`接口来设置当前帧的时间戳。
     * @param surface
     * @param time
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void setPresentationTime(EGLSurface surface, long time) {
        EGLExt.eglPresentationTimeANDROID(mEGLDisplay, surface, time);
    }


    /**
     * 对于SwapBuffers，SwapBuffer命令只是把前台和后台的缓冲区指针交换一下而已也就是把前台的内容变成后台缓冲的内容，
     * 把后台的缓冲内容换到了前台这个函数它本身并不对换过来的成为了后台的buffer做清理工作，所以每帧都glClear一次，
     * 然后再绘制，而后再SwapBuffers。
     * @param surface
     */
    public void swapBuffers(EGLSurface surface) {
        EGL14.eglSwapBuffers(mEGLDisplay, surface);
    }

    public void destroySurface(EGLSurface surface) {
        EGL14.eglDestroySurface(mEGLDisplay, surface);
    }

    public EGLDisplay getDisplay() {
        return mEGLDisplay;
    }

    //创建视频数据流的OES TEXTURE


    private void log(String log) {
        if (isDebug) {
            CamLog.e("EGLHelper", log);
        }
    }
}
