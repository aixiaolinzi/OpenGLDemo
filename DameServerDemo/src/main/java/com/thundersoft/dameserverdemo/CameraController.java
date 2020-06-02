package com.thundersoft.dameserverdemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

import androidx.core.app.ActivityCompat;

/**
 * @author weidong xiao
 */
public class CameraController {

    private static final String TAG = "CameraController";
    private static final int IMAGE_WIDTH = 1280;
    private static final int IMAGE_HEIGHT = 720;
    private static boolean isPreviewStarted = false;
    private static CameraController sCameraController;
    private Context mContext;
//    private DataFeeder mDataFeeder = new DataFeeder();

    private String mCameraId;
    private Surface mSurface;
    private List<Surface> mSurfaceList;
    private Surface mGLSurface;
    /**
     * Camera Capture Session
     */
    private CameraCaptureSession mCameraCaptureSession;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private CaptureRequest mCaptureRequest;

    /**
     * background Thread
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private HandlerThread mCameraThread;
    private Handler mCameraHandler;

    /**
     * camera devices
     */
    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            startBackgroudCamera();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
        }
    };

    private CameraController(Context context) {
        mContext = context;
        startCameraThread();
    }

    public void setDataFeeder(DataFeeder dataFeeder) {
//        this.mDataFeeder = dataFeeder;

    }

    /**
     * get a cameraHelper instance
     *
     * @param context
     * @return CameraController instance
     */
    public static CameraController getInstance(Context context) {
        if (null == sCameraController) {
            return new CameraController(context);
        }
        return sCameraController;
    }

    public void setGLSurface(Surface surface) {
        mGLSurface = surface;
        mSurfaceList.add(surface);
    }

    /**
     * initialize the Camera
     *
     * @return results tell initialization states : 0 successful
     */
    public int initCamera(Surface surface) {

        mSurface = surface;
        mSurfaceList = new ArrayList<>();
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        if (null == mCameraManager) {
            Log.w(TAG, " CameraManager can not get System Services.");
            return -2;
        }
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCameraId);
            StreamConfigurationMap map = characteristics.
                    get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map == null) {
                Log.d(TAG, "Cannot get available preview/video sizes");
                return -1;
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
            return -2;
        }
        return 0;
    }

    /**
     * initial and start camera Thread
     */
    private void startCameraThread() {
        mCameraThread = new HandlerThread("CameraThread");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());
    }

    /**
     * check the system permission of the camera
     * open the Camera
     * called by startPreview
     *
     * @return open successful? true : false
     */
    public boolean openCamera() {
        if (null == mCameraManager) {
            return false;
        }
        try {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            mCameraManager.openCamera(mCameraId, mStateCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /**
     * close all the resources when close the camera
     */
    public void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCameraCaptureSession) {
                mCameraCaptureSession.close();
                mCameraCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mCameraOpenCloseLock.release();
            stopCameraThread();
        }
    }

    /**
     * stop the Camera Thread
     */
    private void stopCameraThread() {
        mCameraThread.quitSafely();
        try {
            mCameraThread.join();
            mCameraThread = null;
            mCameraHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public int startBackgroudCamera() {

        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//            mCaptureRequestBuilder.addTarget(mDataFeeder.getSurface());
            if (null != mGLSurface) {
                mCaptureRequestBuilder.addTarget(mGLSurface);
            }
            mCaptureRequestBuilder.addTarget(mSurface);
//            mSurfaceList.add(mDataFeeder.getSurface());
            if (null != mSurface) {
                mSurfaceList.add(mSurface);
            }


            mCameraDevice.createCaptureSession(Arrays.asList(mSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            try {

                                mCaptureRequest = mCaptureRequestBuilder.build();
                                mCameraCaptureSession = session;
                                mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, null, mCameraHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                            Log.d(TAG, "CameraCaptureSession comfiguration failed.");
                        }
                    }, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return -2;
        }
        return 0;
    }

    /**
     * start preview after target surface is prepared
     */
    public int startPreview() {
        if (null == mSurface) {
            return -2;
        }
        if (!isPreviewStarted) {
            try {
                if (null == mCameraDevice) {
                    return -1;
                }
                mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                mCaptureRequestBuilder.addTarget(mSurface);
//                mCaptureRequestBuilder.addTarget(mDataFeeder.getSurface());
                mCaptureRequest = mCaptureRequestBuilder.build();
                mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, null, mCameraHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
                return -2;
            }
            isPreviewStarted = true;
            return 0;
        }
        return -1;
    }

    /**
     * StopPreview
     *
     * @return
     */
    public int stopPreview() {
        if (isPreviewStarted) {
            try {
                if (null == mCameraDevice) {
                    return -1;
                }
                mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//                mCaptureRequestBuilder.addTarget(mDataFeeder.getSurface());
                mCaptureRequest = mCaptureRequestBuilder.build();
                mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, null, mCameraHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
                return -2;
            }
            isPreviewStarted = false;
            return 0;
        }
        return -1;
    }




    /**
     * convert YUV_420_88 to NV21
     *
     * @param image
     * @return byte[] data
     */
    private static byte[] yuv_420_888toNV21(Image image) {
        byte[] nv21;
        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        nv21 = new byte[ySize + uSize + vSize];

        //U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        return nv21;
    }
}
