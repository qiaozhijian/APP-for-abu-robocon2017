package com.action.app.actionctr2;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.action.app.actionctr2.ble.BleService;
import com.action.app.actionctr2.ble.bleDataProcess;


/**
 * Created by 56390 on 2016/12/9.
 */

public class BleConnectActivity extends BasicActivity implements View.OnClickListener {
    private bleDataProcess state;
    private boolean isEnding=false;
    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
//        加载界面
        setContentView(R.layout.activity_ble_connect);
//        获取sharedpreference.editor对象
        SharedPreferences.Editor dataSt=getSharedPreferences("data",MODE_PRIVATE).edit();
//        清除sharedpreference 文件中的数据
        dataSt.clear();
//        提交数据，完成保存工作
        dataSt.commit();

        isEnding=false;

        final TextView text=(TextView)findViewById(R.id.ble_connect_display);
        text.setText("蓝牙连接中");
        Button button=(Button)findViewById(R.id.ble_connect_skip);
        button.setOnClickListener(this);

//        启动服务
        Intent intentBleService=new Intent(this,BleService.class);
        startService(intentBleService);

//        获取蓝牙数据处理对象和状态返回对象
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
//                最多20个点
                count%=20;
                boolean check=false;
                if(state.getBinder()!=null){
//                    check 蓝牙是否已经发现服务
                    check=state.isReadyForData();
                    if(check){
                        Log.d("ble","ble is ready for sendData");
                        Intent intent=new Intent(BleConnectActivity.this,BeginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
//                如果没有，500ms后再这行一次
                if(!check&&!isEnding){
                    handler.postDelayed(this,500);
                }
            }
        };
        handler.post(runnable);
    }

//    点击就跳转
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
