package com.action.app.actionctr.ble;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
/**
 * Created by 56390 on 2016/12/18.
 */

public class bleDataProcess{
    private BleService.myBleBand state;
    private Context context;
    private ServiceConnection connection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            state=(BleService.myBleBand)iBinder;
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };
    public bleDataProcess(Context text){
        Intent bindIntent=new Intent(text,BleService.class);
        context=text;
        text.bindService(bindIntent,connection,text.BIND_AUTO_CREATE);
    }

    public int getBleStatus(){
        return state.getBleStatus();
    }
    public boolean sendParam(int id,int value){
        return true;
    }
    public boolean sendParam(int id,float value){
        return true;
    }
    public void unbind(){
        context.unbindService(connection);
    }
}
