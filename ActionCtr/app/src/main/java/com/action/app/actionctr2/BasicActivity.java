package com.action.app.actionctr2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;

/**
 * Created by 56390 on 2016/12/7.
 */

public class BasicActivity extends AppCompatActivity{
    public final String TAG = "activitytrack";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        Log.d(TAG, getClass().getSimpleName() + "  onCreate");
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_BACK)
            return false;
        return super.onKeyDown(keycode, event);
    }
    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, getClass().getSimpleName() + "  onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, getClass().getSimpleName() + "  onResume");
    }

    @Override
    public void onRestart() {
        super.onRestart();
        Log.d(TAG, getClass().getSimpleName() + "  onRestart");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, getClass().getSimpleName() + "  onStop");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, getClass().getSimpleName() + "  onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, getClass().getSimpleName() + "  onDestroy");
    }

    @Override
    public void finish() {
        super.finish();
        // moveTaskToBack(true);
        Log.d(TAG, getClass().getSimpleName() + "  finish");
    }

}
