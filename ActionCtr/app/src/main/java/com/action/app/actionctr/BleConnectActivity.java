package com.action.app.actionctr;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.action.app.actionctr.ble.BleService;
import com.action.app.actionctr.ble.bleDataProcess;


/**
 * Created by 56390 on 2016/12/9.
 */

public class BleConnectActivity extends BasicActivity implements View.OnClickListener {
    private ProgressBar progressView;
    private BluetoothAdapter bleAdapter;
    private BluetoothManager bleManager;
    private bleDataProcess state;
    Handler handler;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_ble_connect);
        Log.d("ble", "App running");

        progressView=(ProgressBar)findViewById(R.id.ble_connect_progress);
        progressView.setProgress(0);
        progressView.setMax(100);

        bleManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bleAdapter= bleManager.getAdapter();

        if (bleAdapter == null) {
            Toast.makeText(this, "device do not support bluetooth", Toast.LENGTH_SHORT).show();
            Log.d("Ble", "device do not support bluetooth");
            finish();
        }
        if (!bleAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent,2);
        }
        Log.d("Ble", "ble enable");
        progressView.setProgress(10);
        Intent intentService=new Intent(this,BleService.class);
        startService(intentService);
        state=new bleDataProcess(this);

        final Runnable runnable=new Runnable() {
            @Override
            public void run() {
                Message msg=new Message();
                if(state.getBinder()!=null){
                    msg.what=state.getBleStatus();
                }
                else {
                    msg.what=BleService.BleProfile.BLE_IDLE;
                }
                handler.sendMessage(msg);
                Log.d("Ble","handler running");
            }
        };

        handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case BleService.BleProfile.BLE_IDLE:
                        progressView.setProgress(20);
                        break;
                    case BleService.BleProfile.BLE_SCANING:
                        progressView.setProgress(50);
                        break;
                    case BleService.BleProfile.BLE_CONNECTED:
                        progressView.setProgress(100);
                        Intent intent=new Intent(BleConnectActivity.this,BeginActivity.class);
                        startActivity(intent);
                        break;
                }
                if(msg.what!=BleService.BleProfile.BLE_CONNECTED)
                    handler.postDelayed(runnable,1000);
                else
                    finish();
            }
        };
        handler.postDelayed(runnable,1000);
    }

    @Override
    public void onClick(View v) {

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        state.unbind();
    }
}
