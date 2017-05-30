package com.action.app.actionctr;

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
}
