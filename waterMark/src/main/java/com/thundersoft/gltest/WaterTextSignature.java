package com.thundersoft.gltest;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;

public class WaterTextSignature {

    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "  gl_Position = uMVPMatrix * aPosition;\n" +
                    "  vTextureCoord = aTextureCoord.xy;\n" +
                    "}\n";

    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform sampler2D sTexture;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";

    public static final int SIZE_OF_FLOAT = 4;

    private static final float FULL_RECTANGLE_COORDS[] = {
            -1.0f, -1.0f,  // 0 bottom left
            1.0f, -1.0f,  // 1 bottom right
            -1.0f, 1.0f,  // 2 top left
            1.0f, 1.0f,  // 3 top right
    };
    private static final float FULL_RECTANGLE_TEX_COORDS[] = {
            0.0f, 1.0f,   //0 bottom left   //0.0f, 0.0f, // 0 bottom left
            1.0f, 1.0f,   //1 bottom right  //1.0f, 0.0f, // 1 bottom right
            0.0f, 0.0f,   //2 top left    //0.0f, 1.0f, // 2 top left
            1.0f, 0.0f,   //3 top right    //1.0f, 1.0f, // 3 top right
    };

    private FloatBuffer mVertexArray;
    private FloatBuffer mTexCoordArray;
    private int mCoordsPerVertex;
    private int mCoordsPerTexture;
    private int mVertexCount;
    private int mVertexStride;
    private int mTexCoordStride;
    private int hProgram;

    public float[] mProjectionMatrix = new float[16];// 投影矩阵
    public float[] mViewMatrix = new float[16]; // 摄像机位置朝向9参数矩阵
    public float[] mModelMatrix = new float[16];// 模型变换矩阵
    public float[] mMVPMatrix = new float[16];// 获取具体物体的总变换矩阵

    private float[] getFinalMatrix() {
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        return mMVPMatrix;
    }

    public WaterTextSignature() {
        mVertexArray = createFloatBuffer(FULL_RECTANGLE_COORDS);
        mTexCoordArray = createFloatBuffer(FULL_RECTANGLE_TEX_COORDS);
        mCoordsPerVertex = 2;
        mCoordsPerTexture = 2;
        mVertexCount = FULL_RECTANGLE_COORDS.length / mCoordsPerVertex; // 4
        mTexCoordStride = 2 * SIZE_OF_FLOAT;
        mVertexStride = 2 * SIZE_OF_FLOAT;

        Matrix.setIdentityM(mProjectionMatrix, 0);
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mMVPMatrix, 0);
        hProgram = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    private FloatBuffer createFloatBuffer(float[] coords) {
        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * SIZE_OF_FLOAT);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(coords);
        fb.position(0);
        return fb;
    }

    /**
     * 渲染部分，与GLRender中的 Camera图形渲染流程一致
     * 区别在于shader程序有差异，以及纹理使用的是普通Texture
     *
     * @param mTextureId
     */
    public void drawFrame(int mTextureId) {
        GLES20.glUseProgram(hProgram);
        int aPosition = glGetAttribLocation(hProgram, "aPosition");
        int aTextureCoord = glGetAttribLocation(hProgram, "aTextureCoord");
        int uMVPMatrix = glGetUniformLocation(hProgram, "uMVPMatrix");
        int sTexture = glGetUniformLocation(hProgram, "sTexture");

        // 设置纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
        GLES20.glUniform1i(sTexture, 0);
        GLES20.glUniformMatrix4fv(uMVPMatrix, 1, false, getFinalMatrix(), 0);
        // 使用简单的VAO 设置顶点坐标数据
        GLES20.glVertexAttribPointer(aPosition, mCoordsPerVertex, GLES20.GL_FLOAT, false, mVertexStride, mVertexArray);
        GLES20.glEnableVertexAttribArray(aPosition);
        // 使用简单的VAO 设置纹理坐标数据
        GLES20.glVertexAttribPointer(aTextureCoord, mCoordsPerTexture, GLES20.GL_FLOAT, false, mTexCoordStride, mTexCoordArray);
        GLES20.glEnableVertexAttribArray(aTextureCoord);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertexCount);
        //解绑
        GLES20.glDisableVertexAttribArray(aPosition);
        GLES20.glDisableVertexAttribArray(aTextureCoord);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);
    }

    /**
     * 加载创建shader
     *
     * @param shaderType
     * @param sourceCode
     * @return
     */
    private int loadShader(int shaderType, String sourceCode) {
        int shader = GLES20.glCreateShader(shaderType);

        if (shader != 0) {
            GLES20.glShaderSource(shader, sourceCode);
            GLES20.glCompileShader(shader);

            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e("ES20_ERROR", "Could not compile shader " + shaderType + ":");
                Log.e("ES20_ERROR", GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    /**
     * 创建shader程序
     *
     * @param vertexSource
     * @param fragmentSource
     * @return
     */
    private int createProgram(String vertexSource, String fragmentSource) {

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) return 0;

        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (fragmentShader == 0) return 0;

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            GLES20.glAttachShader(program, fragmentShader);

            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e("ES20_ERROR", "Could not link program: ");
                Log.e("ES20_ERROR", GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }

        return program;
    }
}
