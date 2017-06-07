package com.action.app.actionctr2.wifi;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.ArrayList;

/**
 * Created by 56390 on 2017/1/17.
 */

public class wifiDataProcess {
    private Context context;
    private wifiService.wifiServiceBinder binder;
    private ServiceConnection connection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder=(wifiService.wifiServiceBinder)iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    public wifiDataProcess(Context text){
        context=text;
        Intent bindIntent=new Intent(context,wifiService.class);
        context.bindService(bindIntent,connection,context.BIND_AUTO_CREATE);
    }
    public ArrayList<String> getWifiStringDataList(){
        return  binder.getWifiStringDataList();
    }

    public IBinder getBinder(){
        return binder;
    }

    public void unbind(){
        context.unbindService(connection);
    }
}
