// DataFeederInterface.aidl
package com.thundersoft.dameserverdemo;

// Declare any non-default types here with import statements
import com.thundersoft.dameserverdemo.IDataFeederListener;

interface DataFeederInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    ParcelFileDescriptor getPfd();
    void callbackNumber(IDataFeederListener Listener);
}
