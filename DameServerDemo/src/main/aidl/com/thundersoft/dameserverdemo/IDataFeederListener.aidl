// IDataFeederListener.aidl
package com.thundersoft.dameserverdemo;

// Declare any non-default types here with import statements
import android.os.ParcelFileDescriptor;
interface IDataFeederListener {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onNewDataArrived(in ParcelFileDescriptor pfd);
}
