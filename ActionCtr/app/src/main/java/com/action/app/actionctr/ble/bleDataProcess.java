package com.action.app.actionctr.ble;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.action.app.actionctr.ndktool.JniShareUtils;

/**
 * Created by 56390 on 2016/12/18.
 */

public class bleDataProcess{
    private JniShareUtils cpptool;
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
        cpptool=new JniShareUtils();
        text.bindService(bindIntent,connection,text.BIND_AUTO_CREATE);
    }

    public int getBleStatus(){
        return state.getBleStatus();
    }
    public boolean sendParam(byte id,int value){
        byte[] sendData=new byte[BleService.BleProfile.dataLen];

        if(BleService.BleProfile.dataLen>=9){
            sendData[0]='A';
            sendData[1]='C';
            sendData[2]='P';
            sendData[3]='C';
            sendData[4]=id;
            sendData[5]=(byte)(value&0xff);
            sendData[6]=(byte)((value>>8)&0xff);
            sendData[7]=(byte)((value>>16)&0xff);
            sendData[8]=(byte)((value>>24)&0xff);
            state.send(sendData);
            return true;
        }
        Log.e("Ble","dataSend length err");
        return false;
    }
    public boolean sendParam(byte id,float value){
        byte[] sendData=new byte[BleService.BleProfile.dataLen];
        byte[] floatData=cpptool.floatToByte(value);
        if(BleService.BleProfile.dataLen>=9){
            sendData[0]='A';
            sendData[1]='C';
            sendData[2]='P';
            sendData[3]='C';
            sendData[4]=id;
            sendData[5]=floatData[0];
            sendData[6]=floatData[1];
            sendData[7]=floatData[2];
            sendData[8]=floatData[3];
            state.send(sendData);
            return true;
        }
        Log.e("Ble","dataSend length err");
        return false;
    }
    public boolean sendCmd(int num){

        byte[] sendData=new byte[BleService.BleProfile.dataLen];

        if(BleService.BleProfile.dataLen>=9){
            sendData[0]='A';
            sendData[1]='C';
            sendData[2]='C';
            sendData[3]='T';
            switch (num){
                case 0:
                    sendData[4]='0';
                    sendData[5]='0';
                    sendData[6]='0';
                    sendData[7]='0';
                    sendData[8]='0';
                    break;
                case 1:
                    break;
            }
            state.send(sendData);
            return true;
        }
        Log.e("Ble","dataSend length err");
        return false;
    }
    public boolean checkSendOk(){
        return state.checkSendOk();
    }
    public Binder getBinder(){
        return state;
    }
    public void unbind(){
        context.unbindService(connection);
    }
}
