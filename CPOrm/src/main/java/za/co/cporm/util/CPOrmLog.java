package za.co.cporm.util;

import android.util.Log;

/**
 * Created by hennie.brink on 2015-05-16.
 */
public class CPOrmLog {

    private final static String TAG = "CPOrm";

    public static int println(int level, String msg) {
        return Log.println(level, TAG, msg);
    }

    public static int v(String msg) {
        return Log.v(TAG, msg);
    }

    public static int v(String msg, Throwable th) {
        return Log.v(TAG, msg, th);
    }

    public static int d(String msg) {
        return Log.d(TAG, msg);
    }

    public static int d(String msg, Throwable th) {
        return Log.d(TAG, msg, th);
    }

    public static int i(String msg) {
        return Log.i(TAG, msg);
    }

    public static int i(String msg, Throwable th) {
        return Log.i(TAG, msg, th);
    }

    public static int w(String msg) {
        return Log.w(TAG, msg);
    }

    public static int w(String msg, Throwable th) {
        return Log.w(TAG, msg, th);
    }

    public static int w(Throwable th) {
        return Log.w(TAG, th);
    }

    public static int e(String msg) {
        return Log.e(TAG, msg);
    }

    public static int e(String msg, Throwable th) {
        return Log.e(TAG, msg, th);
    }
}
