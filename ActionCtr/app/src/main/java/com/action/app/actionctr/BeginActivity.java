package com.action.app.actionctr;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.View;

public class BeginActivity extends BasicActivity implements OnClickListener{

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_begin);
        findViewById(R.id.column1).setOnClickListener(this);
        findViewById(R.id.column2).setOnClickListener(this);
        findViewById(R.id.column3).setOnClickListener(this);
        findViewById(R.id.column4).setOnClickListener(this);
        findViewById(R.id.column5).setOnClickListener(this);
        findViewById(R.id.column6).setOnClickListener(this);
        findViewById(R.id.column7).setOnClickListener(this);

        findViewById(R.id.button_leftTop).setOnClickListener(this);
        findViewById(R.id.button_Top).setOnClickListener(this);
        findViewById(R.id.button_RightTop).setOnClickListener(this);
        findViewById(R.id.button_left).setOnClickListener(this);
        findViewById(R.id.buttun_center).setOnClickListener(this);
        findViewById(R.id.button_right).setOnClickListener(this);
        findViewById(R.id.button_leftBottom).setOnClickListener(this);
        findViewById(R.id.button_Bottom).setOnClickListener(this);
        findViewById(R.id.button_rightBottom).setOnClickListener(this);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();


    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.column1:
            case R.id.column2:
            case R.id.column3:
            case R.id.column4:
            case R.id.column5:
            case R.id.column6:
            case R.id.column7:
                findViewById(R.id.activity_button).setVisibility(View.INVISIBLE);
                findViewById(R.id.activity_choose).setVisibility(View.VISIBLE);
                break;
            case R.id.button_leftTop:
            case R.id.button_Top:
            case R.id.button_RightTop:
            case R.id.button_left:
            case R.id.buttun_center:
            case R.id.button_right:
            case R.id.button_leftBottom:
            case R.id.button_Bottom:
            case R.id.button_rightBottom:
                findViewById(R.id.activity_button).setVisibility(View.VISIBLE);
                findViewById(R.id.activity_choose).setVisibility(View.INVISIBLE);
                Intent intent=new Intent(this,ParamChangeActivity.class);
                intent.putExtra("button_id",v.getId());
                startActivity(intent);
                finish();
                break;
        }
    }
}
