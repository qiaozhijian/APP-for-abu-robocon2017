package com.action.app.actionctr2.ndktool;

/**
 * Created by 56390 on 2016/12/13.
 */

public class JniShareUtils {
    public native byte[] floatToByte(float s);
    static {
        System.loadLibrary("native-lib");
    }
}
