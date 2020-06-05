package com.thundersoft.dameserverdemo.video.egl;

import javax.microedition.khronos.egl.EGL10;

/**
 * 指定使用上下文使用的属性列表.
 */
public class EGLContextAttrs {


    private int version = 2;

    private boolean isDefault;

    public EGLContextAttrs version(int v) {
        this.version = v;
        return this;
    }

    public EGLContextAttrs makeDefault(boolean def) {
        this.isDefault = def;
        return this;
    }

    public boolean isDefault() {
        return isDefault;
    }

    int[] build() {
        return new int[]{0x3098, version, EGL10.EGL_NONE};
    }


}
