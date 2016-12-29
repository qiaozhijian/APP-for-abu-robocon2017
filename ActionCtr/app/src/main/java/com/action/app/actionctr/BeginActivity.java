package com.action.app.actionctr;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class BeginActivity extends BasicActivity implements View.OnTouchListener,View.OnClickListener {

    private GestureDetector mGestureDetector;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_begin);
        mGestureDetector = new GestureDetector(this,new gestureListener());

        findViewById(R.id.go_to_data_activity).setOnClickListener(this);
        findViewById(R.id.go_to_ctr_activity).setOnClickListener(this);

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

    private String landPlace;
    private boolean recognizeGesture=false;
    private int buttonIdInt;
    //重写OnTouchListener的onTouch方法
    //此方法在触摸屏被触摸，即发生触摸事件的时候被调用。
    @Override
    public
    boolean onTouch(View v, MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        if(recognizeGesture) {
            switch (v.getId())
            {
                case R.id.column1:
                    Log.d("MyGesture","c1");
                    buttonIdInt=1;
                    break;
                case R.id.column2:
                    Log.d("MyGesture","c2");
                    buttonIdInt=2;
                    break;
                case R.id.column3:
                    Log.d("MyGesture","c3");
                    buttonIdInt=3;
                    break;
                case R.id.column4:
                    Log.d("MyGesture","c4");
                    buttonIdInt=4;
                    break;
                case R.id.column5:
                    Log.d("MyGesture","c5");
                    buttonIdInt=5;
                    break;
                case R.id.column6:
                    Log.d("MyGesture","c6");
                    buttonIdInt=6;
                    break;
                case R.id.column7:
                    Log.d("MyGesture","c7");
                    buttonIdInt=7;
                    break;
                default:
                    buttonIdInt=0;
                    break;
            }
            //实例化下一个活动
            Intent intent=new Intent(this,ParamChangeActivity.class);
            Log.d("MyGesture", "success");
            recognizeGesture =  false;
            intent.putExtra("button_id",buttonIdInt);
            intent.putExtra("gesture_ward",landPlace);
            startActivity(intent);
            finish();
        }
        return true;
    }
    private class gestureListener implements GestureDetector.OnGestureListener {
        //在按下动作时被调用
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }
        //在按住时被调用
        @Override
        public void onShowPress(MotionEvent e) {
        }
        //在抬起时被调用
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            landPlace="no ward";
            recognizeGesture =  true;
            return false;
        }
        //在长按时被调用
        @Override
        public void onLongPress(MotionEvent e) {
        }
        //在滚动时调用
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }
        //在抛掷动作时被调用
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (velocityY > 300 &&  Math.abs(velocityX) < 0.5 * Math.abs(velocityY)) {
                landPlace = "down";
                recognizeGesture =  true;
                Log.d("MyGesture", "down");
            } else if (velocityY < -300 &&  Math.abs(velocityX) < 0.5 * Math.abs(velocityY)) {
                Log.d("MyGesture", "up");
                landPlace = "up";
                recognizeGesture =  true;
            } else if (Math.abs(velocityY) < 0.5 * Math.abs(velocityX)  && velocityX < -300) {
                Log.d("MyGesture", "left");
                landPlace = "left";
                recognizeGesture =  true;
            } else if ( Math.abs(velocityY) < 0.5 * Math.abs(velocityX) && velocityX > 300) {
                Log.d("MyGesture", "right");
                landPlace = "right";
                recognizeGesture =  true;
            } else if (velocityY < -300 && velocityX < -300 && Math.abs(Math.abs(velocityY) - Math.abs(velocityX)) < 0.5 * Math.abs(velocityX)) {
                Log.d("MyGesture", "left-up");
                landPlace = "left-up";
                recognizeGesture =  true;
            } else if (velocityY > 300 && velocityX > 300 && Math.abs(Math.abs(velocityY) - Math.abs(velocityX)) < 0.5 * Math.abs(velocityX)) {
                Log.d("MyGesture", "right-down");
                landPlace = "right-down";
                recognizeGesture =  true;
            } else if (velocityY < -300 && velocityX > 300 && Math.abs(Math.abs(velocityY) - Math.abs(velocityX)) < 0.5 * Math.abs(velocityX)) {
                Log.d("MyGesture", "right-up");
                landPlace = "right-up";
                recognizeGesture =  true;
            } else if (velocityY > 300 &&  velocityX < -300 && Math.abs(Math.abs(velocityY) - Math.abs(velocityX)) < 0.5 * Math.abs(velocityX)) {
                Log.d("MyGesture", "left-down");
                landPlace = "left-down";
                recognizeGesture =  true;
            }
            return false;
        }
    }
    @Override
    public void onClick(View v){
        Intent intent;
        switch (v.getId()){
            case R.id.go_to_ctr_activity:
                intent=new Intent(BeginActivity.this,CtrActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.go_to_data_activity:
                intent=new Intent(BeginActivity.this,DataActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }
}
