package com.action.app.actionctr2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

/**
 * Created by 56390 on 2016/12/7.
 */
//后面的类都在继承这个类，appCompatActivity似乎更通用
public class BasicActivity extends AppCompatActivity{
//设置这个主题
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
    }
//屏蔽按键
    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_BACK)
            return false;
        return super.onKeyDown(keycode, event);
    }


}
