package com.action.app.actionctr2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.action.app.actionctr2.ble.BleService;

/**
 * Created by Administrator on 2017/7/27/027.
 */

public class ModeSelect extends BasicActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.mode_select);
        findViewById(R.id.tryField).setOnClickListener(this);
        findViewById(R.id.compete).setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tryField:
                Intent serviceIntent1=new Intent(this, BleService.class);
                serviceIntent1.putExtra("mode","tryField");
                startService(serviceIntent1);
                Intent intent =new Intent(this,BleConnectActivity.class);
                startActivity(intent);
                break;
            case R.id.compete:
                Intent serviceIntent2=new Intent(this, BleService.class);
                serviceIntent2.putExtra("mode","compete");
                startService(serviceIntent2);
                Intent intent1=new Intent(this,BleConnectActivity.class);
                startActivity(intent1);
                break;
        }
    }
}
