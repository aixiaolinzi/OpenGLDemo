package com.thundersoft.gltest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_TEXTURE;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glVertexAttribPointer;

public class GLRender implements GLSurfaceView.Renderer {

    private Context m_context;
    private GLSurfaceView m_glSurfaceView;
    private CameraV2 m_camera;
    private FloatBuffer m_vectorBuffer;
    private WaterSignature waterSignature;
    private WaterTextSignature waterTextSignature;

    private int program = -1;
    private int vPosition = -1;
    private int aTextureCoordinate = -1;
    private int uTextureMatrix = -1;
    private int uTextureSampler = -1;
    private int uTextureWatermark = -1;
    private float[] transformMatrix = new float[16];
    private int mOESTextureId = -1;
    private SurfaceTexture mSurfaceTexture;
    private int mMarkerTextureId = -1;
    private static int bitmapWidth;
    private static int bitmapHeight;

    private int mTextureId = -1;

    public void init(GLSurfaceView surfaceView, CameraV2 camera, Context context) {
        m_glSurfaceView = surfaceView;
        m_context = context;
        m_camera = camera;
    }


    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        //顶点Shader
        String verticesShader = readShaderFromResource(m_context, R.raw.base_vertex_shader);
        //片元Shader
        String fragmentShader = readShaderFromResource(m_context, R.raw.base_fragment_shader);
        //创建Shader
        program = creatProgram(verticesShader, fragmentShader);
        m_vectorBuffer = getVertices();
        mOESTextureId = createOESTextureObject();

        //获取水印图片纹理
        mMarkerTextureId = loadTexture(m_context, R.mipmap.image);
        mTextureId = loadTextureText(m_context, R.mipmap.image);

        //水印渲染模块
        if (waterSignature == null) {
            waterSignature = new WaterSignature();
        }

        if (waterTextSignature == null) {
            waterTextSignature = new WaterTextSignature();
        }

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        GLES20.glViewport(0, 0, i, i1);
    }

    boolean previewStarted = false;

    @Override
    public void onDrawFrame(GL10 gl10) {
        // 清屏
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT);
        //启用透明
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        //刷新Camera图片
        if (mSurfaceTexture != null) {
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(transformMatrix);
        }

        if (!previewStarted) {
            previewStarted = initSurfaceTexture();
            previewStarted = true;
            return;
        }

        //绘制Camera图像
        //渲染窗口大小位置
        GLES20.glViewport(0, 0, m_glSurfaceView.getWidth(), m_glSurfaceView.getHeight());
        //使用Shader
        GLES20.glUseProgram(program);
        //Shader中个字段
        vPosition = glGetAttribLocation(program, "vPosition");
        aTextureCoordinate = glGetAttribLocation(program, "aTextureCoordinate");
        uTextureMatrix = glGetUniformLocation(program, "uTextureMatrix");
        uTextureSampler = glGetUniformLocation(program, "uTextureSampler");
        uTextureWatermark = glGetUniformLocation(program, "uTextureWatermark");
        //激活纹理单元
        GLES20.glActiveTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
        //绑定OES纹理到纹理单元
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mOESTextureId);
        //纹理传至Shader中uTextureSampler字段
        GLES20.glUniform1i(uTextureSampler, 0);
        //uTextureMatrix字段赋值
        GLES20.glUniformMatrix4fv(uTextureMatrix, 1, false, transformMatrix, 0);
        //设置顶点Shader中的顶点参数和纹理坐标参数
        m_vectorBuffer.position(0);
        //每次去两个float值，为顶点数据，隔4 * 4 = 16 再取下一行的顶点数据
        GLES20.glVertexAttribPointer(vPosition, 2, GL_FLOAT, false, 16, m_vectorBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);
        m_vectorBuffer.position(2);
        GLES20.glEnableVertexAttribArray(aTextureCoordinate);
        GLES20.glVertexAttribPointer(aTextureCoordinate, 2, GL_FLOAT, false, 16, m_vectorBuffer);
        // 绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
        GLES20.glDisableVertexAttribArray(vPosition);
        GLES20.glDisableVertexAttribArray(aTextureCoordinate);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES20.glUseProgram(0);
        //Camera绘制完成

        //水印窗口大小位置
        GLES20.glViewport(0, 0, bitmapWidth, bitmapHeight);
        waterSignature.drawFrame(mMarkerTextureId);

        //水印窗口大小位置
        GLES20.glViewport(500, 100, bitmapWidth * 2, bitmapHeight * 2);
        waterTextSignature.drawFrame(mTextureId);
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
    private int creatProgram(String vertexSource, String fragmentSource) {

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

    /**
     * 初始化SurfaceTexture，与OpenGL的OES纹理绑定
     *
     * @return
     */
    public boolean initSurfaceTexture() {
        if (m_camera == null || m_glSurfaceView == null) {
            return false;
        }
        mSurfaceTexture = new SurfaceTexture(mOESTextureId);
        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                m_glSurfaceView.requestRender();
            }
        });
        m_camera.setPreviewTexture(mSurfaceTexture);
        m_camera.startPreview();
        return true;
    }

    /**
     * 顶点数据 Buffer储存
     *
     * @return
     */
    private FloatBuffer getVertices() {
        //每行前两个值为顶点坐标，后两个为纹理坐标
        //三个点画一个三角面，两个面组成一个平面
        float vertices[] = {
                1f, 1f, 1f, 1f,
                -1f, 1f, 0f, 1f,
                -1f, -1f, 0f, 0f,
                1f, 1f, 1f, 1f,
                -1f, -1f, 0f, 0f,
                1f, -1f, 1f, 0f
        };

        FloatBuffer buffer = ByteBuffer.allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        buffer.put(vertices, 0, vertices.length).position(0);
        return buffer;
    }

    /**
     * 创建OpenGL OES纹理
     *
     * @return
     */
    public static int createOESTextureObject() {
        int[] tex = new int[1];
        GLES20.glGenTextures(1, tex, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        return tex[0];
    }

    public static String readShaderFromResource(Context context, int resourceId) {
        StringBuilder builder = new StringBuilder();
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            is = context.getResources().openRawResource(resourceId);
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                builder.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                    is = null;
                }
                if (isr != null) {
                    isr.close();
                    isr = null;
                }
                if (br != null) {
                    br.close();
                    br = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return builder.toString();
    }

    /**
     * 加载图片至OpenGL纹理
     *
     * @param context
     * @param resourceId
     * @return
     */
    public static int loadTexture(Context context, int resourceId) {


        final int[] textureObjectIds = new int[1];
        GLES20.glGenTextures(1, textureObjectIds, 0);
        if (textureObjectIds[0] == 0) {
            Log.e("GLRender", "Could not generate a new OpenGL texture object!");
            return 0;
        }
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        if (bitmap == null) {
            Log.e("GLRender", "Resource ID " + resourceId + "could not be decode");
            GLES20.glDeleteTextures(1, textureObjectIds, 0);
            return 0;
        }
        bitmapWidth = bitmap.getWidth();
        bitmapHeight = bitmap.getHeight();
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
     * 加载图片至OpenGL纹理
     *
     * @param context
     * @param resourceId
     * @return
     */
    public static int loadTextureText(Context context, int resourceId) {

        String mstrTitle = "文字渲染到Bitmap!";
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
        p.getTextBounds(mstrTitle, 0, mstrTitle.length(), rect);
        int width = rect.width();//文本的宽度


        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); //图象大小要根据文字大小算下,以和文本长度对应 Canvas canvasTemp = new Canvas(bmp);

        Canvas canvas = new Canvas(bitmap);
//        canvas.drawColor(Color.WHITE);

        canvas.drawText(mstrTitle, 0, -metrics.ascent, p);
        canvas.save();


        final int[] textureObjectIds = new int[1];
        GLES20.glGenTextures(1, textureObjectIds, 0);
        if (textureObjectIds[0] == 0) {
            Log.e("GLRender", "Could not generate a new OpenGL texture object!");
            return 0;
        }
        if (bitmap == null) {
            Log.e("GLRender", "Resource ID " + resourceId + "could not be decode");
            GLES20.glDeleteTextures(1, textureObjectIds, 0);
            return 0;
        }
        bitmapWidth = bitmap.getWidth();
        bitmapHeight = bitmap.getHeight();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureObjectIds[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return textureObjectIds[0];
    }
}
