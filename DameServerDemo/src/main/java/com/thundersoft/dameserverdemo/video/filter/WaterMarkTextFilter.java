package com.thundersoft.dameserverdemo.video.filter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WaterMarkTextFilter extends LazyFilter {

    private int[] viewPort = new int[4];
    private int[] markPort = new int[4];
    private final LazyFilter mark = new LazyFilter() {

        @Override
        protected void onClear() {

        }
    };

    public WaterMarkTextFilter setMarkPosition(final int x, final int y, final int width, final int height) {
        markPort[0] = x;
        markPort[1] = y;
        markPort[2] = width;
        markPort[3] = height;
        runOnGLThread(new Runnable() {
            @Override
            public void run() {
                mark.sizeChanged(width, height);
            }
        });
        return this;
    }



    @Override
    protected void onCreate() {
        super.onCreate();
        mark.create();
    }

    @Override
    protected void onDraw() {
        super.onDraw();

        int markTextureId = loadTextureText();
        if (markTextureId != -1) {
            GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, viewPort, 0);
            GLES20.glViewport(markPort[0], mHeight - markPort[3] - markPort[1], markPort[2], markPort[3]);

            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            GLES20.glBlendEquation(GLES20.GL_FUNC_ADD);
            mark.draw(markTextureId);
            GLES20.glDisable(GLES20.GL_BLEND);

            GLES20.glViewport(viewPort[0], viewPort[1], viewPort[2], viewPort[3]);
        }
    }


    /**
     * 加载图片至OpenGL纹理
     *
     * @return
     */
    public static int loadTextureText() {

        String timeTitle = getStringDate();
        Paint p = new Paint();

        String familyName = "serif";

        Typeface font = Typeface.create(familyName, Typeface.BOLD);

        p.setColor(Color.RED);

        p.setTypeface(font);

        p.setTextSize(22);

        //获取高度
        Paint.FontMetricsInt metrics = p.getFontMetricsInt();
        int height = metrics.bottom - metrics.top;

        //获取宽度
        Rect rect = new Rect();
        p.getTextBounds(timeTitle, 0, timeTitle.length(), rect);
        int width = rect.width();//文本的宽度


        //图象大小要根据文字大小算下,以和文本长度对应 Canvas canvasTemp = new Canvas(bmp);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
//        canvas.drawColor(Color.WHITE);

        canvas.drawText(timeTitle, 0, -metrics.ascent, p);
        canvas.save();


        final int[] textureObjectIds = new int[1];
        GLES20.glGenTextures(1, textureObjectIds, 0);
        if (textureObjectIds[0] == 0) {
            Log.e("GLRender", "Could not generate a new OpenGL texture object!");
            return -1;
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureObjectIds[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return textureObjectIds[0];
    }


    /**
     * 获取现在时间
     *
     * @return返回字符串格式 yyyy-MM-dd HH:mm:ss
     */
    public static String getStringDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        return dateString;
    }
}
