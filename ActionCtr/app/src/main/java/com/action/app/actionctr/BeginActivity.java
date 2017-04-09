package com.action.app.actionctr;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.action.app.actionctr.ble.bleDataProcess;
import com.action.app.actionctr.wifi.wifiService;

public class BeginActivity extends BasicActivity implements View.OnClickListener {
    private bleDataProcess state;
    private boolean isEnding=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_begin);
        isEnding=false;
        Intent intentWifiService=new Intent(this,wifiService.class);
        startService(intentWifiService);

        findViewById(R.id.go_to_data_activity).setOnClickListener(this);
        findViewById(R.id.go_to_human_activity).setOnClickListener(this);
        findViewById(R.id.go_to_debug_data_activity).setOnClickListener(this);

        findViewById(R.id.column1).setOnClickListener(this);
        findViewById(R.id.column2).setOnClickListener(this);
        findViewById(R.id.column3).setOnClickListener(this);
        findViewById(R.id.column4).setOnClickListener(this);
        findViewById(R.id.column5).setOnClickListener(this);
        findViewById(R.id.column6).setOnClickListener(this);
        findViewById(R.id.column7).setOnClickListener(this);

        final ProgressBar bar=(ProgressBar)findViewById(R.id.begin_progressbar_rssi);
        final TextView textView=(TextView)findViewById(R.id.begin_textview_rssi);
        bar.setProgress(0);
        state=new bleDataProcess(this);

        final Handler handler=new Handler();
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                if(!isEnding){
                    if(state.getBinder()!=null){
                        if(!state.isReadyForData()){
                            bar.setProgress(0);
                            textView.setText("蓝牙断开");
                            textView.setTextColor(Color.parseColor("#FF0000"));
                        }
                        else {
                            textView.setText("蓝牙强度");
                            textView.setTextColor(Color.parseColor("#000000"));
                            int rssi=150+state.readRssi();
                            if(rssi>100)
                                rssi=100;
                            if(rssi<0)
                                rssi=0;
                            bar.setProgress(rssi);
                            Log.d("ble","rssi: "+String.valueOf(rssi));
                        }
                    }
                    handler.postDelayed(this,500);
                }
            }
        };
        handler.post(runnable);
    }

    @Override
    public void onDestroy(){
        isEnding=true;
        super.onDestroy();
        state.unbind();
    }
    @Override
    public void onClick(View v){
        Intent intent;
        int countForColumn=0;
        switch (v.getId()){
            case R.id.go_to_human_activity:
                intent=new Intent(BeginActivity.this,humanSensorActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.go_to_data_activity:
                intent=new Intent(BeginActivity.this,DataActivity.class);
                startActivity(intent);
                //finish();
                break;
            case R.id.go_to_debug_data_activity:
                intent=new Intent(BeginActivity.this,DebugDataDisplayActivity.class);
                startActivity(intent);
//                finish();
                break;
            case R.id.column7:countForColumn++;
            case R.id.column6:countForColumn++;
            case R.id.column5:countForColumn++;
            case R.id.column4:countForColumn++;
            case R.id.column3:countForColumn++;
            case R.id.column2:countForColumn++;
            case R.id.column1:countForColumn++;
                intent=new Intent(this,ParamChangeActivity.class);
                intent.putExtra("button_id",countForColumn);
                startActivity(intent);
                finish();
                break;
        }
    }
}
