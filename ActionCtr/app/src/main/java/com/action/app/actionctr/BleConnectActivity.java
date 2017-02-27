package com.action.app.actionctr;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.action.app.actionctr.ble.BleService;
import com.action.app.actionctr.ble.bleDataProcess;


/**
 * Created by 56390 on 2016/12/9.
 */

public class BleConnectActivity extends BasicActivity implements View.OnClickListener {
    private bleDataProcess state;
    private boolean isEnding=false;
    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_ble_connect);
        isEnding=false;
        final TextView text=(TextView)findViewById(R.id.ble_connect_display);
        text.setText("蓝牙连接中");
        Button button=(Button)findViewById(R.id.ble_connect_skip);
        button.setOnClickListener(this);

        Intent intentBleService=new Intent(this,BleService.class);
        startService(intentBleService);

        state=new bleDataProcess(this);
        final Handler handler=new Handler();
        Runnable runnable=new Runnable() {
            private int count=0;
            @Override
            public void run() {
                String string="蓝牙连接中";
                count++;
                for(int i=0;i<count;i++){
                    string+='.';
                }
                text.setText(string);
                count%=20;
                boolean check=false;
                if(state.getBinder()!=null){
                    check=state.isReadyForData();
                    if(check){
                        Log.d("ble","ble is ready for sendData");
                        Intent intent=new Intent(BleConnectActivity.this,BeginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
                if(!check&&!isEnding){
                    handler.postDelayed(this,500);
                }
            }
        };
        handler.post(runnable);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ble_connect_skip:
                Intent intent=new Intent(this,BeginActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }
    @Override
    public void onDestroy() {
        isEnding=true;
        super.onDestroy();
        state.unbind();
    }
}
