package com.renhui.opengles20study;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.renhui.opengles20study.glsv.ImageGLSurfaceView;
import com.renhui.opengles20study.glsv.OvalGLSurfaceView;
import com.renhui.opengles20study.glsv.PaintPointGLSurfaceView;
import com.renhui.opengles20study.glsv.RotateTriangleGLSurfaceView;
import com.renhui.opengles20study.glsv.SquareGLSurfaceView;
import com.renhui.opengles20study.glsv.TriangleGLSurfaceView;

import java.io.IOException;

public class ShowActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int index = getIntent().getIntExtra("index", 0);
        switch (index) {

            case 1:
                setContentView(new TriangleGLSurfaceView(this)); // 绘制三角形
                break;
            case 2:
                setContentView(new SquareGLSurfaceView(this));  // 绘制正方形
                break;
            case 3:
                setContentView(new OvalGLSurfaceView(this)); // 绘制圆形
                break;
            case 4:
                 setContentView(new PaintPointGLSurfaceView(this)); // 手绘点
                break;
            case 5:
                 setContentView(new RotateTriangleGLSurfaceView(this)); // 旋转三角形
                break;
            case 6:
                try {
                    setContentView(new ImageGLSurfaceView(this)); // 加载图片
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }






    }
}
