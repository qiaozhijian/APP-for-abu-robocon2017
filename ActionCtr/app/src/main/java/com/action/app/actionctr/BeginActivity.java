package com.action.app.actionctr;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class BeginActivity extends BasicActivity implements View.OnTouchListener {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
    private GestureDetector mGestureDetector;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_begin);
        mGestureDetector = new GestureDetector(this,new gestureListener());

        Button column1 =(Button)findViewById(R.id.column1);
        initColumn(column1);
        Button column2 = (Button)findViewById(R.id.column2);
        initColumn(column2);
        Button column3 = (Button)findViewById(R.id.column3);
        initColumn(column3);
        Button column4 = (Button)findViewById(R.id.column4);
        initColumn(column4);
        Button column5 = (Button)findViewById(R.id.column5);
        initColumn(column5);
        Button column6 = (Button)findViewById(R.id.column6);
        initColumn(column6);
        Button column7 = (Button)findViewById(R.id.column7);
        initColumn(column7);
    }
    private void initColumn(Button bt)
    {
        bt.setOnTouchListener(this);
        bt.setFocusable(true);
        bt.setClickable(true);
        bt.setLongClickable(true);
    }

    //飞盘落点
    private String landPlace;
    //手势是否已经识别
    private boolean recognizeGesture=false;

    //重写OnTouchListener的onTouch方法
    //此方法在触摸屏被触摸，即发生触摸事件（接触和抚摸两个事件，挺形象）的时候被调用。
    @Override
    public
    boolean onTouch(View v, MotionEvent event) {
        mGestureDetector.onTouchEvent(event);

        if(recognizeGesture ) {
            //实例化下一个活动
            Intent intent=new Intent(this,ParamChangeActivity.class);
            Log.i("MyGesture", "success");
            recognizeGesture =  false;
            intent.putExtra("button_id",landPlace);
            startActivity(intent);
            finish();
        }
        return true;
    }
    private class gestureListener implements GestureDetector.OnGestureListener {
        //在按下动作时被调用
        @Override
        public boolean onDown(MotionEvent e) {
            return
                    false;
        }
        //在按住时被调用
        @Override
        public void onShowPress(MotionEvent e) {
        }
        //在抬起时被调用
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return
                    false;
        }
        //在长按时被调用
        @Override
        public void onLongPress(MotionEvent e) {
        }
        //在滚动时调用
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return
                    false;
        }
        //在抛掷动作时被调用
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (velocityY > 300 &&  Math.abs(velocityX) < 0.5 * Math.abs(velocityY)) {
                landPlace = "up";
                recognizeGesture =  true;
                Toast.makeText(BeginActivity.this, "up", Toast.LENGTH_SHORT).show();
                Log.i("MyGesture", "up");
            } else if (velocityY < -300 &&  Math.abs(velocityX) < 0.5 * Math.abs(velocityY)) {
                Toast.makeText(BeginActivity.this, "down", Toast.LENGTH_SHORT).show();
                Log.i("MyGesture", "down");
                landPlace = "down";
                recognizeGesture =  true;
            } else if (Math.abs(velocityY) < 0.5 * Math.abs(velocityX)  && velocityX < -300) {
                Toast.makeText(BeginActivity.this, "right", Toast.LENGTH_SHORT).show();
                Log.i("MyGesture", "right");
                landPlace = "right";
                recognizeGesture =  true;
            } else if ( Math.abs(velocityY) < 0.5 * Math.abs(velocityX) && velocityX > 300) {
                Toast.makeText(BeginActivity.this, "left", Toast.LENGTH_SHORT).show();
                Log.i("MyGesture", "left");
                landPlace = "left";
                recognizeGesture =  true;
            } else if (velocityY < -300 && velocityX < -300 && Math.abs(Math.abs(velocityY) - Math.abs(velocityX)) < 0.5 * Math.abs(velocityX)) {
                Toast.makeText(BeginActivity.this, "right-down", Toast.LENGTH_SHORT).show();
                Log.i("MyGesture", "right-down");
                landPlace = "right-down";
                recognizeGesture =  true;
            } else if (velocityY > 300 && velocityX > 300 && Math.abs(Math.abs(velocityY) - Math.abs(velocityX)) < 0.5 * Math.abs(velocityX)) {
                Toast.makeText(BeginActivity.this, "left-up", Toast.LENGTH_SHORT).show();
                Log.i("MyGesture", "left-up");
                landPlace = "left-up";
                recognizeGesture =  true;
            } else if (velocityY < -300 && velocityX > 300 && Math.abs(Math.abs(velocityY) - Math.abs(velocityX)) < 0.5 * Math.abs(velocityX)) {
                Toast.makeText(BeginActivity.this, "left-down", Toast.LENGTH_SHORT).show();
                Log.i("MyGesture", "left-down");
                landPlace = "left-down";
                recognizeGesture =  true;
            } else if (velocityY > 300 &&  velocityX < -300 && Math.abs(Math.abs(velocityY) - Math.abs(velocityX)) < 0.5 * Math.abs(velocityX)) {
                Toast.makeText(BeginActivity.this, "right-up", Toast.LENGTH_SHORT).show();
                Log.i("MyGesture", "right-up");
                landPlace = "right-up";
                recognizeGesture =  true;
            }
            return
                    false;
        }
    }
}
