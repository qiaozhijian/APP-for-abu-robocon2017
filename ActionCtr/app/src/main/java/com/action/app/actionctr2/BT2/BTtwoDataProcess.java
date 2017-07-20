package com.action.app.actionctr2.BT2;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.action.app.actionctr2.ble.BleService;
import com.action.app.actionctr2.myTool;

/**
 * Created by Administrator on 2017/7/19/019.
 */

public class BTtwoDataProcess {
    private Context context;
    private BTtwoService.BT2Binder binder;
    private ServiceConnection connection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder=(BTtwoService.BT2Binder)iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    public BTtwoDataProcess(Context text){
        context=text;
        Intent bindIntent=new Intent(context,BTtwoService.class);
        context.bindService(bindIntent,connection,context.BIND_AUTO_CREATE);
    }

    public boolean sendParam(byte id1, byte id2,float value){
        myTool mytool=new myTool();
        byte[] sendData=new byte[BleService.bleDataLen];
        byte[] floatData=mytool.getByteArray(value);
        if(sendData.length>=10){
            sendData[0]='A';
            sendData[1]='C';
            sendData[2]='P';
            sendData[3]='C';
            sendData[4]=id1;
            sendData[5]=id2;
            sendData[6]=floatData[0];
            sendData[7]=floatData[1];
            sendData[8]=floatData[2];
            sendData[9]=floatData[3];
            String log_out = new String();
            for (int i = 0; i < 12; i++) {
                    log_out += String.valueOf((byte) sendData[i]) + '\t';
            }
            Log.d("sendpara", log_out);
            if(binder!=null)
                binder.sendbyBT2(sendData);
            return true;
        }
        Log.e("bletrack","dataSend length err");
        return false;
    }
    public boolean sendCmd(byte id){

        byte[] sendData=new byte[BleService.bleDataLen];

        if(sendData.length>=10){
            sendData[0]='A';
            sendData[1]='C';
            sendData[2]='C';
            sendData[3]='T';
            sendData[4]=id;
            if(binder!=null) {
                binder.sendbyBT2(sendData);
            }
            return true;
        }

        Log.e("bletrack","dataSend length err");
        return false;
    }
    public IBinder getBinder(){
        return binder;
    }

    public boolean isConnected()
    {
        return binder.isConnect();
    }

    public void unbind(){
        context.unbindService(connection);
    }
}
