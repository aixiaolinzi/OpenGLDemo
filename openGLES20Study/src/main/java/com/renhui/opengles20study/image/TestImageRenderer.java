package com.renhui.opengles20study.image;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.renhui.opengles20study.base.BaseGLSL;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 图片展示渲染器
 */
public class TestImageRenderer extends BaseGLSL implements GLSurfaceView.Renderer  {

    private static final String vertexMatrixShaderCode =
            "attribute vec4 vPosition;\n" +
                    "attribute vec2 vCoordinate;\n" +
                    "uniform mat4 vMatrix;\n" +
                    "varying vec2 aCoordinate;\n" +
                    "void main(){\n" +
//                    "    gl_Position=vMatrix*vPosition;\n" +
                    "    gl_Position=vMatrix*vPosition;\n" +
                    "    aCoordinate=vCoordinate;\n" +
                    "}";


    private static final String fragmentShaderCode =
            "precision mediump float;\n" +
                    "uniform sampler2D vTexture_y;\n" +
                    "uniform sampler2D vTexture_u;\n" +
                    "uniform sampler2D vTexture_v;\n" +
                    "uniform sampler2D vTexture_uv;\n" +

                    "uniform int yuvType;" +

                    "varying vec2 aCoordinate;\n" +
                    "void main(){\n" +

                    "float y;\n" +
                    "float u;\n" +
                    "float v;\n" +

                    "y = texture2D(vTexture_y, aCoordinate).r;\n" +
                    "if (yuvType == 0){" +
                    "   u = texture2D(vTexture_u, aCoordinate).r;\n" +
                    "   v = texture2D(vTexture_v, aCoordinate).r;\n" +
                    "} else if (yuvType == 1){" +
                    "   u = texture2D(vTexture_uv, aCoordinate).r;\n" +
                    "   v = texture2D(vTexture_uv, aCoordinate).a;\n" +
                    "} " +

                    "y = 1.164*(y - 0.0625);\n" +
                    "u = u - 0.5;\n" +
                    "v = v - 0.5;\n" +

                    "float r = y + 1.596023559570 * v;\n" +
                    "float g = y - 0.3917694091796875 * u - 0.8129730224609375 * v;\n" +
                    "float b = y + 2.017227172851563 * u;\n" +

                    "    gl_FragColor = vec4( r, g, b, 1);\n" +
                    "}";

    private static final float[] sPos = {
            -1.0f, 1.0f,    //左上角
            -1.0f, -1.0f,   //左下角
            1.0f, 1.0f,     //右上角
            1.0f, -1.0f     //右下角
    };

    private static final float[] sCoord = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
    };

    private Context mContext;
    private int mProgram;
    private int glHPosition;
    private int glHTexture;
    private int glHCoordinate;
    private int glHMatrix;
    private int glYUVType;
//    private Bitmap mBitmap;


    private byte[] mYuvDate;
    private int mWidth = 1440;
    private int mHeight = 1080;

    private int[] textureId;

    private FloatBuffer bPos;
    private FloatBuffer bCoord;

    private float[] mViewMatrix = new float[16];
    private float[] mProjectMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];


    public TestImageRenderer(Context context) throws IOException {
        mContext = context;
//        mBitmap = BitmapFactory.decodeStream(mContext.getResources().getAssets().open("texture/fengj.png"));
        InputStream ins = mContext.getResources().getAssets().open("texture/yuvFile.YUV420NV12");
        BufferedInputStream bufferedIns = new BufferedInputStream(ins);
        byte[] bytes = new byte[mWidth * mHeight * 3 / 2];
        bufferedIns.read(bytes);
        mYuvDate = bytes;

        ByteBuffer bb = ByteBuffer.allocateDirect(sPos.length * 4);
        bb.order(ByteOrder.nativeOrder());
        bPos = bb.asFloatBuffer();
        bPos.put(sPos);
        bPos.position(0);


        ByteBuffer cc = ByteBuffer.allocateDirect(sCoord.length * 4);
        cc.order(ByteOrder.nativeOrder());
        bCoord = cc.asFloatBuffer();
        bCoord.put(sCoord);
        bCoord.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);

        mProgram = createOpenGLProgram(vertexMatrixShaderCode, fragmentShaderCode);

        glHPosition = GLES20.glGetAttribLocation(mProgram, "vPosition");
        glHCoordinate = GLES20.glGetAttribLocation(mProgram, "vCoordinate");
        glHTexture = GLES20.glGetUniformLocation(mProgram, "vTexture");
        glHMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        glYUVType = GLES20.glGetUniformLocation(mProgram, "yuvType");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        int w = mWidth;
        int h = mHeight;
        float sWH = w / (float) h;
        float sWidthHeight = width / (float) height;
        if (width > height) {
            if (sWH > sWidthHeight) {
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight * sWH, sWidthHeight * sWH, -1, 1, 3, 7);
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight / sWH, sWidthHeight / sWH, -1, 1, 3, 7);
            }
        } else {
            if (sWH > sWidthHeight) {
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -1 / sWidthHeight * sWH, 1 / sWidthHeight * sWH, 3, 7);
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -sWH / sWidthHeight, sWH / sWidthHeight, 3, 7);
            }
        }
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(mProgram);
        GLES20.glUniformMatrix4fv(glHMatrix, 1, false, mMVPMatrix, 0);
        GLES20.glEnableVertexAttribArray(glHPosition);
        GLES20.glEnableVertexAttribArray(glHCoordinate);
        glYUVType = GLES20.glGetUniformLocation(mProgram, "yuvType");
        textureId = createTextureNV12();
        //传入顶点坐标
        GLES20.glVertexAttribPointer(glHPosition, 2, GLES20.GL_FLOAT, false, 0, bPos);
        //传入纹理坐标
        GLES20.glVertexAttribPointer(glHCoordinate, 2, GLES20.GL_FLOAT, false, 0, bCoord);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    ByteBuffer buffer_y = ByteBuffer.allocate(mWidth * mHeight);
    ByteBuffer buffer_u = ByteBuffer.allocate(mWidth * mHeight / 4);
    ByteBuffer buffer_v = ByteBuffer.allocate(mWidth * mHeight / 4);
    ByteBuffer buffer_uv = ByteBuffer.allocate(mWidth * mHeight / 2);



    private int[] createTextureNV12() {
        GLES20.glUniform1i(glYUVType, 1);

        buffer_y.put(mYuvDate, 0, mWidth * mHeight);
        buffer_uv.put(mYuvDate, mWidth * mHeight, mWidth * mHeight / 2);

        buffer_y.position(0);
        buffer_uv.position(0);
        // TODO !!! 保持与 shader 内 sampler2D 代码名称一致 ！！！！
        int[] sampleHandle = new int[] {
                GLES20.glGetUniformLocation(mProgram, "vTexture_y"),
                GLES20.glGetUniformLocation(mProgram, "vTexture_uv"),
        };

        int[] texture = new int[sampleHandle.length];
        GLES20.glGenTextures(sampleHandle.length, texture, 0);

        //TODO Activity Texture
        for (int i = 0; i < sampleHandle.length; i++ ) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[i]);
            GLES20.glUniform1i(sampleHandle[i], i);
        }



        tex2DYUV(buffer_y, mWidth, mHeight, texture[0]);
        texture2DNV12(buffer_uv, mWidth / 2, mHeight / 2, texture[1]);

        return texture;
    }

    private void tex2DYUV(ByteBuffer buffer, int width, int height, int glTexture ) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, glTexture);
        //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D, 0,
                GLES20.GL_LUMINANCE, width, height, 0,
                GLES20.GL_LUMINANCE,
                GLES20.GL_UNSIGNED_BYTE, buffer);
    }

    private void texture2DNV12(ByteBuffer buffer, int width, int height, int glTexture) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, glTexture);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE_ALPHA, width, height, 0,
                GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, buffer
        );
    }
}
