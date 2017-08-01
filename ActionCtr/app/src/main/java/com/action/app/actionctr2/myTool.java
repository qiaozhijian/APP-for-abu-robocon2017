package com.action.app.actionctr2;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;

/**
 * Created by 56390 on 2017/5/29.
 */

public class myTool {
    static final public boolean isForTestPara=true;
    static public ArrayList<Button> getAllButton(ViewGroup group)
    {
        ArrayList<Button> arrayList=new ArrayList<>();
        for (int i=0;i<group.getChildCount();i++) {
            View v=group.getChildAt(i);
            if(v instanceof Button){
                arrayList.add((Button) v);
            }
            else if(v instanceof ViewGroup)
            {
                arrayList.addAll(getAllButton((ViewGroup) v));
            }
        }
        return  arrayList;
    }



    //    要看大端还是小端模式
    public static byte[] getByteArray(int i) {
        byte[] b = new byte[4];
        b[3] = (byte) ((i & 0xff000000) >> 24);
        b[2] = (byte) ((i & 0x00ff0000) >> 16);
        b[1] = (byte) ((i & 0x0000ff00) >> 8);
        b[0] = (byte)  (i & 0x000000ff);
        return b;
    }
    // 从byte数组的index处的连续4个字节获得一个int
    public static int getInt(byte[] arr, int index) {
        return  (0xff000000     & (arr[index+3] << 24))  |
                (0x00ff0000     & (arr[index+2] << 16))  |
                (0x0000ff00     & (arr[index+1] << 8))   |
                (0x000000ff     &  arr[index+0]);
    }
    // float转换为byte[4]数组
    public static byte[] getByteArray(float f) {
        int intbits = Float.floatToIntBits(f);//将float里面的二进制串解释为int整数
        return getByteArray(intbits);
    }
    // 从byte数组的index处的连续4个字节获得一个float
    public static float getFloat(byte[] arr, int index) {
        return Float.intBitsToFloat(getInt(arr, index));
    }

}




