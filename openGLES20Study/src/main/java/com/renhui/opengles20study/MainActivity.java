package com.renhui.opengles20study;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.renhui.opengles20study.camera.preview.PreviewCameraActivity;
import com.renhui.opengles20study.camera.takepic.TakePictureActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // setContentView(new TriangleGLSurfaceView(this)); // 绘制三角形

        // setContentView(new SquareGLSurfaceView(this));  // 绘制正方形

        // setContentView(new OvalGLSurfaceView(this)); // 绘制圆形

        // setContentView(new PaintPointGLSurfaceView(this)); // 手绘点

        // setContentView(new RotateTriangleGLSurfaceView(this)); // 旋转三角形

        // setContentView(new ImageGLSurfaceView(this)); // 加载图片

        // startActivity(new Intent(this, PreviewCameraActivity.class));  // OpenGL预览摄像头

        setContentView(R.layout.activity_main);
    }


    public void copy1(View view) {
        Intent intent = new Intent(this, ShowActivity.class);
        intent.putExtra("index", 1);
        startActivity(intent);
    }


    public void copy2(View view) {
        Intent intent = new Intent(this, ShowActivity.class);
        intent.putExtra("index", 2);
        startActivity(intent);
    }

    public void copy3(View view) {
        Intent intent = new Intent(this, ShowActivity.class);
        intent.putExtra("index", 3);
        startActivity(intent);
    }

    public void copy4(View view) {
        Intent intent = new Intent(this, ShowActivity.class);
        intent.putExtra("index", 4);
        startActivity(intent);
    }

    public void copy5(View view) {
        Intent intent = new Intent(this, ShowActivity.class);
        intent.putExtra("index", 5);
        startActivity(intent);
    }

    public void copy6(View view) {
        Intent intent = new Intent(this, ShowActivity.class);
        intent.putExtra("index", 6);
        startActivity(intent);
    }

    public void copy7(View view) {
        startActivity(new Intent(this, PreviewCameraActivity.class));
        // OpenGL预览摄像头
    }

    public void copy8(View view) {
        startActivity(new Intent(this, TakePictureActivity.class));
        // OpenGL 拍照

    }


}
