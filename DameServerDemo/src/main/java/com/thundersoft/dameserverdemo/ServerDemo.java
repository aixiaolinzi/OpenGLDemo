package com.thundersoft.dameserverdemo;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.concurrent.CopyOnWriteArraySet;

public class ServerDemo extends Service {
    private ParcelFileDescriptor pfd;
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private FileDescriptor fd;
    private MemoryFile mf;
    private Method method;
    private  byte[] contentBytes;
    private CopyOnWriteArraySet<IDataFeederListener> mListenerList = new CopyOnWriteArraySet<>();
    private DataFeederInterface mDataFeederInterface = new DataFeederInterface.Stub() {

        @Override
        public ParcelFileDescriptor getPfd() throws RemoteException {
            return pfd;
        }

        @Override
        public void callbackNumber(IDataFeederListener Listener) throws RemoteException {
            mListenerList.add(Listener);
        }
    };
   private CameraController mCameraController;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(getClass().getSimpleName(), String.format("on bind,intent = %s", intent.toString()));
        return (IBinder) mDataFeederInterface;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mCameraController = CameraController.getInstance(getApplicationContext());
        mCameraController.initCamera(null);

        mHandlerThread = new HandlerThread(" service");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String str = "hehhehehehhehehehehhehehehehehhehehehehehsfsdfdsfsgdsgdsagdsfsdfsafsafefsafsaefsafdsvdsvfsvsavefefsdvdsvdsfddsssssfsafsfsafsfsfdsfdsfsdfdsfdsfsfdsfffdfddddddddddfd" +
                "dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd" +
                "sddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd" +
                "sddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd" +
                "sddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd" +
                "dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd" +
                "sddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd" +
                "sddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd" +
                "sddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd" +
                "sdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd" +
                "sdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd";
        contentBytes = str.getBytes();
        //创建匿名共享内存
        try {
            mf = new MemoryFile("memfile", 1280*720);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        //写入字符数据
                        mf.writeBytes(contentBytes, 0, 0, contentBytes.length);
                        method = MemoryFile.class.getDeclaredMethod("getFileDescriptor");
                        //通过反射获得文件句柄
                        fd = (FileDescriptor) method.invoke(mf);
                        pfd = ParcelFileDescriptor.dup(fd);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    for (IDataFeederListener listener: mListenerList){
                        try {
                            listener.onNewDataArrived(pfd);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                }

            }
        },1000);

//        number = 1;
    }

}
