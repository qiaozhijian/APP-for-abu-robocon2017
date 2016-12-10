package com.action.app.actionctr;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * Created by 56390 on 2016/12/9.
 */

public class BleConnectActivity extends BasicActivity implements View.OnClickListener {

    private  BluetoothManager bleManager;
    private BluetoothAdapter bleAdapter;
    private final int REQUEST_ENABLE_BT=2;
    private ArrayList<BluetoothDevice> devicesList;

    private Handler mHandler;
    private final int SCAN_PERIOD=1000;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;

    private ProgressBar progressView;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_ble_connect);
        Log.d("ble", "App running");
        bleManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bleAdapter= bleManager.getAdapter();
        devicesList=new ArrayList<>();
        progressView=(ProgressBar)findViewById(R.id.ble_connect_progress);
        progressView.setProgress(0);
        progressView.setMax(100);

        if (bleAdapter == null) {
            Toast.makeText(this, "device do not support bluetooth", Toast.LENGTH_SHORT).show();
            Log.d("ble", "device do not support bluetooth");
            finish();
        }
        if (!bleAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
        }
        Log.d("ble", "ble enable");
        progressView.setProgress(10);
        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, int rssi,
                                 byte[] scanRecord) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(!devicesList.contains(device)) {
                            devicesList.add(device);
                            Log.d("ble","find device , name="+device.getName());
                            progressView.setProgress(50);
                        }
                    }
                });
             }
        };
        mHandler=new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bleAdapter.stopLeScan(mLeScanCallback);
            }
        }, SCAN_PERIOD);
        bleAdapter.startLeScan(mLeScanCallback);
        Log.d("ble", "ble scanning");
        progressView.setProgress(20);
    }

    @Override
    public void onClick(View v) {

    }
}
