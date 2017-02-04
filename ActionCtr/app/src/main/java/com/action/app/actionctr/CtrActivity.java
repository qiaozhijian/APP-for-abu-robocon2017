package com.action.app.actionctr;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.action.app.actionctr.ble.bleDataProcess;
import com.action.app.actionctr.wifi.wifiService;

public class CtrActivity extends BasicActivity implements View.OnClickListener {

    private bleDataProcess bleCtr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ctr);

        findViewById(R.id.activity_ctr_button1).setOnClickListener(this);
        findViewById(R.id.activity_ctr_button2).setOnClickListener(this);
        findViewById(R.id.activity_ctr_button3).setOnClickListener(this);
        findViewById(R.id.activity_ctr_button4).setOnClickListener(this);
        findViewById(R.id.activity_ctr_button5).setOnClickListener(this);
        findViewById(R.id.activity_ctr_button6).setOnClickListener(this);
        findViewById(R.id.activity_ctr_button7).setOnClickListener(this);
        findViewById(R.id.activity_ctr_button8).setOnClickListener(this);

        findViewById(R.id.activity_ctr_cancel).setOnClickListener(this);
        bleCtr=new bleDataProcess(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.activity_ctr_button1:
                Log.d("ctrCmd","button1");
                bleCtr.sendCmd(1);
                break;
            case R.id.activity_ctr_button2:
                Log.d("ctrCmd","button2");
                bleCtr.sendCmd(2);
                break;
            case R.id.activity_ctr_button3:
                Log.d("ctrCmd","button3");
                bleCtr.sendCmd(3);
                break;
            case R.id.activity_ctr_button4:
                Log.d("ctrCmd","button4");
                bleCtr.sendCmd(4);
                break;
            case R.id.activity_ctr_button5:
                Log.d("ctrCmd","button5");
                bleCtr.sendCmd(5);
                break;
            case R.id.activity_ctr_button6:
                Log.d("ctrCmd","button6");
                bleCtr.sendCmd(6);
                break;
            case R.id.activity_ctr_button7:
                Log.d("ctrCmd","button7");
                bleCtr.sendCmd(7);
                break;
            case R.id.activity_ctr_button8:
                Log.d("ctrCmd","button8");
                bleCtr.sendCmd(8);
                break;
            case R.id.activity_ctr_cancel:
                Intent intent=new Intent(this,BeginActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        bleCtr.unbind();
    }

}
