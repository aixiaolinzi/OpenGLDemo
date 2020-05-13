package com.thundersoft.gltest;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private GLSurfaceView m_glSurfaceView;
    private CameraV2 m_camera;
    private GLRender glRender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //开启Camera
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        m_camera = new CameraV2(this);
        m_camera.setupCamera(dm.widthPixels, dm.heightPixels);
        if (!m_camera.openCamera()) {
            Log.d("MainActivity", "Open Camera fail");
            return;
        }
        Log.d("MainActivity", "Open Camera sucess");

        //开启OpneGL画布
        m_glSurfaceView = new GLSurfaceView(this);
        m_glSurfaceView.setEGLContextClientVersion(2);
        //渲染管线
        glRender = new GLRender();
        glRender.init(m_glSurfaceView, m_camera, MainActivity.this);
        m_glSurfaceView.setRenderer(glRender);
        m_glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        setContentView(m_glSurfaceView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            glRender.setCameraHeight(640);
            glRender.setCameraWidth(480);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            glRender.setCameraHeight(1920);
            glRender.setCameraWidth(1080);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}
