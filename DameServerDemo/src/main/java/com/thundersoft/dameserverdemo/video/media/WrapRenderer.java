package com.thundersoft.dameserverdemo.video.media;

import com.thundersoft.dameserverdemo.video.core.Renderer;
import com.thundersoft.dameserverdemo.video.filter.OesFilter;

/**
 * WrapRenderer 用于包装其他Filter渲染OES纹理
 */
public class WrapRenderer implements Renderer {

    private Renderer mRenderer;
    private OesFilter mOesFilter;

    public static final int TYPE_MOVE = 0;
    public static final int TYPE_CAMERA = 1;

    public WrapRenderer(Renderer renderer) {
        this.mRenderer = renderer;
        mOesFilter = new OesFilter();
        setFlag(TYPE_MOVE);
    }

    public void setFlag(int flag) {
//        if (flag == TYPE_MOVE) {
        mOesFilter.setVertexCo(new float[]{
                    -1.0f, 1.0f,
                    -1.0f, -1.0f,
                    1.0f, 1.0f,
                    1.0f, -1.0f,
            });
//        } else if (flag == TYPE_CAMERA) {
//            mFilter.setVertexCo(new float[]{
//                    -1.0f, -1.0f,
//                    1.0f, -1.0f,
//                    -1.0f, 1.0f,
//                    1.0f, 1.0f,
//            });
//        }
    }

    public float[] getTextureMatrix() {
        return mOesFilter.getTextureMatrix();
    }

    @Override
    public void create() {
        mOesFilter.create();
        if (mRenderer != null) {
            mRenderer.create();
        }
    }

    @Override
    public void sizeChanged(int width, int height) {
        mOesFilter.sizeChanged(width, height);
        if (mRenderer != null) {
            mRenderer.sizeChanged(width, height);
        }
    }

    @Override
    public void draw(int texture) {
        if (mRenderer != null) {
            mRenderer.draw(mOesFilter.drawToTexture(texture));
        } else {
            mOesFilter.draw(texture);
        }
    }

    @Override
    public void destroy() {
        if (mRenderer != null) {
            mRenderer.destroy();
        }
        mOesFilter.destroy();
    }
}