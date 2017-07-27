package com.action.app.actionctr2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.action.app.actionctr2.ble.BleService;
import com.action.app.actionctr2.ble.bleDataProcess;
import com.action.app.actionctr2.sqlite.SharedPreferencesHelper;
import com.action.app.actionctr2.wifi.wifiService;

public class BeginActivity extends BasicActivity implements View.OnClickListener {
    private bleDataProcess state;
    private boolean isEnding = false;
    private boolean lastIsReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_begin);
        isEnding = false;

        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(this, "data");

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

        final ProgressBar bar = (ProgressBar) findViewById(R.id.begin_progressbar_rssi);
        final TextView textView = (TextView) findViewById(R.id.begin_textview_rssi);
        final TextView textView1 = (TextView) findViewById(R.id.thenum);

        findViewById(R.id.thenum).setOnClickListener(this);

        textView1.setText(sharedPreferencesHelper.getString("returnState"));
        if (sharedPreferencesHelper.getString("returnState") != null)
            Log.d("state2.0", sharedPreferencesHelper.getString("returnState"));
        Intent intentWifiService = new Intent(this, wifiService.class);
        startService(intentWifiService);

        bar.setProgress(0);
        state = new bleDataProcess(this);

        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (!isEnding) {
                    if (state.getBinder() != null) {
                        if (!state.isReadyForDataFirst() && !state.isReadyForDataSecond()) {
                            bar.setProgress(0);
                            textView.setText("蓝牙断开");
                            textView.setTextColor(Color.parseColor("#FF0000"));
                            textView1.setText(String.valueOf(0));
                        } else {
                            textView.setText("蓝牙强度");
                            textView1.setText(String.valueOf(state.readNum()));
                            textView.setTextColor(Color.parseColor("#000000"));
                            int rssi = 0;
                            if (state.isReadyForDataFirst()) {
                                rssi = 150 + state.readRssiFirst();
                                Log.d("ACHB", "1 rssi: " + String.valueOf(state.readRssiFirst()));
                            }
                            else if(state.isReadyForDataSecond()) {
                                rssi = 150 + state.readRssiSecond();
                                Log.d("ACHB", "2 rssi: " + String.valueOf(state.readRssiSecond()));
                            }
                            if (rssi > 100)
                                rssi = 100;
                            if (rssi < 0)
                                rssi = 0;
                            bar.setProgress(rssi);

                        }
                    }
                    handler.postDelayed(this, 500);
                }
            }
        };
        handler.post(runnable);


        //actionStart(this, "scan");
    }

    public void actionStart(Context context, String data) {
        Intent intent = new Intent(context, BleService.class);
        intent.putExtra("data", data);
        context.startService(intent);
    }

    @Override
    public void onDestroy() {
        isEnding = true;
        super.onDestroy();
        state.unbind();
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        int countForColumn = 0;
        switch (v.getId()) {
            case R.id.go_to_human_activity:
                intent = new Intent(BeginActivity.this, humanSensorActivity.class);
                startActivity(intent);

                //清楚数据
                SharedPreferences.Editor dataSt = getSharedPreferences("data", MODE_PRIVATE).edit();
                dataSt.putBoolean("gun_mode_left", false);
                dataSt.putBoolean("gun_mode_right", false);
                dataSt.putBoolean("gun_mode_top", false);
                dataSt.commit();
                //发送蓝牙变回自动模式
                state.sendCmd((byte) (-1));

                finish();
                break;
            case R.id.go_to_data_activity:
                intent = new Intent(BeginActivity.this, DataActivity.class);
                startActivity(intent);
                //finish();
                break;
            case R.id.go_to_debug_data_activity:
                intent = new Intent(BeginActivity.this, DebugDataDisplayActivity.class);
                startActivity(intent);
//                finish();
                break;
            case R.id.column7:
                countForColumn++;
            case R.id.column6:
                countForColumn++;
            case R.id.column5:
                countForColumn++;
            case R.id.column4:
                countForColumn++;
            case R.id.column3:
                countForColumn++;
            case R.id.column2:
                countForColumn++;
            case R.id.column1:
                countForColumn++;
                intent = new Intent(this, ParamChangeActivity.class);
                intent.putExtra("button_id", countForColumn);
                startActivity(intent);
                finish();
                break;
            case R.id.thenum:
                Toast.makeText(this,"断了就重启啊！！",Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
