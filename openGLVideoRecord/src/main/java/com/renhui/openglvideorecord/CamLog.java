package com.renhui.openglvideorecord;

import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;

/**
 * Log helper class
 */
public class CamLog {
    private static final String GLOBAL_TAG = "CameraApp";

    /**
     * {@link CamLog#DEBUG} is true if running in 'userdebug' variant.
     * The following log is disabled in 'user' variant.
     *
     * <pre>{@code if (CamLog.DEBUG) CamLog.d(...);}</pre>
     */
    public static final boolean DEBUG;

    /**
     * logging flag for performance measurement
     */
    public static final boolean IS_TIME_DEBUG = false;

    /**
     * {@link CamLog#VERBOSE} is true if running in 'userdebug' variant and
     * log level is higher than VERBOSE.
     */
    public static final boolean VERBOSE;

    static {
        // TODO: 2020/3/3 user version open log
//        DEBUG = Build.TYPE.equals("userdebug");
        DEBUG = true;
        VERBOSE = DEBUG && android.util.Log.isLoggable(GLOBAL_TAG, android.util.Log.VERBOSE);
    }

    /**
     * Enabled if running in 'userdebug' variant and Log level is higher than VERBOSE.
     */
    public static void v(String... message) {
        if (VERBOSE) {
            android.util.Log.v(GLOBAL_TAG, makeLogStringWithLongInfo(message));
        }
    }

    /**
     * Enabled if running in 'userdebug' variant and Log level is higher than VERBOSE.
     */
    public static void v(String message, Throwable e) {
        if (VERBOSE) {
            android.util.Log.v(GLOBAL_TAG, makeLogStringWithLongInfo(message), e);
        }
    }

    /**
     * Enabled if running in 'userdebug' variant or Log level is higher than DEBUG.
     */
    public static void d(String... message) {
        if (DEBUG || android.util.Log.isLoggable(GLOBAL_TAG, android.util.Log.DEBUG)) {
            android.util.Log.d(GLOBAL_TAG, makeLogStringWithLongInfo(message));
        }
    }

    /**
     * Enabled if running in 'userdebug' variant or Log level is higher than DEBUG.
     */
    public static void d(String message, Throwable e) {
        if (DEBUG || android.util.Log.isLoggable(GLOBAL_TAG, android.util.Log.DEBUG)) {
            android.util.Log.d(GLOBAL_TAG, makeLogStringWithLongInfo(message), e);
        }
    }

    /**
     * Always enabled.
     */
    public static void i(String... message) {
        android.util.Log.i(GLOBAL_TAG, makeLogStringWithShortInfo(message));
    }

    /**
     * Always enabled.
     */
    public static void i(String message, Throwable e) {
        android.util.Log.i(GLOBAL_TAG, makeLogStringWithShortInfo(message), e);
    }

    /**
     * print captureRequest set.
     *
     * @param request request
     * @param keys    request list
     */
    public static void d(CaptureRequest request, CaptureRequest.Key... keys) {
        if (keys.length > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append("CaptureRequest : ");
            for (CaptureRequest.Key key : keys) {
                builder.append(", ").append(key.getName()).append(": ").append(request.get(key));
            }
            android.util.Log.i(GLOBAL_TAG, makeLogStringWithShortInfo(builder.toString()));
        }
    }

    /**
     * @param result
     * @param keys
     */
    public static void d(CaptureResult result, CaptureResult.Key... keys) {
        if (keys.length > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append("CaptureResult : ");
            for (CaptureResult.Key key : keys) {
                builder.append(" ").append(key.getName()).append(" : ").append(result.get(key));
            }
            builder.append(".");
            android.util.Log.i(GLOBAL_TAG, makeLogStringWithShortInfo(builder.toString()));
        }
    }

    /**
     * Always enabled.
     */
    public static void w(String... message) {
        android.util.Log.w(GLOBAL_TAG, makeLogStringWithShortInfo(message));
    }

    /**
     * Always enabled.
     */
    public static void w(String message, Throwable e) {
        android.util.Log.w(GLOBAL_TAG, makeLogStringWithShortInfo(message), e);
    }

    /**
     * Always enabled.
     */
    public static void e(String... message) {
        android.util.Log.e(GLOBAL_TAG, makeLogStringWithShortInfo(message));
    }

    /**
     * Always enabled.
     */
    public static void e(String message, Throwable e) {
        android.util.Log.e(GLOBAL_TAG, makeLogStringWithShortInfo(message), e);
    }

    private static String makeLogStringWithLongInfo(String... message) {
        StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[4];
        StringBuilder builder = new StringBuilder();
        appendTag(builder, stackTrace);
        appendTraceInfo(builder, stackTrace);
        for (String i : message) {
            builder.append(" ");
            builder.append(i);
        }
        return builder.toString();
    }

    private static String makeLogStringWithShortInfo(String... message) {
        StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[4];
        StringBuilder builder = new StringBuilder();
        appendTag(builder, stackTrace);
        for (String i : message) {
            builder.append(" ");
            builder.append(i);
        }
        return builder.toString();
    }

    private static void appendTag(StringBuilder builder, StackTraceElement stackTrace) {
        builder.append('[');
        builder.append(suppressFileExtension(stackTrace.getFileName()));
        builder.append("] ");
    }

    private static void appendTraceInfo(StringBuilder builder, StackTraceElement stackTrace) {
        builder.append(stackTrace.getMethodName());
        builder.append(":");
        builder.append(stackTrace.getLineNumber());
        builder.append(" ");
    }

    private static String suppressFileExtension(String filename) {
        int extensionPosition = filename.lastIndexOf('.');
        if (extensionPosition > 0 && extensionPosition < filename.length()) {
            return filename.substring(0, extensionPosition);
        } else {
            return filename;
        }
    }
}

