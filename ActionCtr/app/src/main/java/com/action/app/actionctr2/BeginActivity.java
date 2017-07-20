package com.action.app.actionctr2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.action.app.actionctr2.BT2.BTtwoDataProcess;
import com.action.app.actionctr2.wifi.wifiService;

public class BeginActivity extends BasicActivity implements View.OnClickListener {
    private BTtwoDataProcess state2;
    private boolean isEnding = false;
    private boolean lastIsReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_begin);
        isEnding = false;
        Intent intentWifiService = new Intent(this, wifiService.class);
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

        final ProgressBar bar = (ProgressBar) findViewById(R.id.begin_progressbar_rssi);
        final TextView textView = (TextView) findViewById(R.id.begin_textview_rssi);
        bar.setProgress(0);
            state2 = new BTtwoDataProcess(this);
    }

    @Override
    public void onDestroy() {
        isEnding = true;
        super.onDestroy();
        state2.unbind();
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
                    state2.sendCmd((byte) (-1));

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
        }
    }
}
