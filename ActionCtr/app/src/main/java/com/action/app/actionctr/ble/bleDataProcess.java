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
    public boolean sendParam(byte id1, byte id2,int value){
        byte[] sendData=new byte[BleService.bleDataLen];

        if(sendData.length>=10){
            sendData[0]='A';
            sendData[1]='C';
            sendData[2]='P';
            sendData[3]='C';
            sendData[4]=id1;
            sendData[5]=id2;
            sendData[6]=(byte)(value&0xff);
            sendData[7]=(byte)((value>>8)&0xff);
            sendData[8]=(byte)((value>>16)&0xff);
            sendData[9]=(byte)((value>>24)&0xff);
            state.send(sendData);
            return true;
        }
        Log.e("Ble","dataSend length err");
        return false;
    }
    public boolean sendParam(byte id1, byte id2,float value){
        byte[] sendData=new byte[BleService.bleDataLen];
        byte[] floatData=cpptool.floatToByte(value);
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
            state.send(sendData);
            return true;
        }
        Log.e("Ble","dataSend length err");
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
               state.send(sendData);
            return true;
        }
        Log.e("Ble","dataSend length err");
        return false;
    }
    public boolean sendState(byte state1,byte state2){

        byte[] sendData=new byte[BleService.bleDataLen];

        if(sendData.length>=10){
            sendData[0]='A';
            sendData[1]='C';
            sendData[2]='S';
            sendData[3]='T';
            sendData[4]=state1;
            sendData[5]=state2;
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
    public boolean isReadyForData(){
        return state.isReady();
    }
    public int readRssi(){
        return state.readRssi();
    }
    public void unbind(){
        context.unbindService(connection);
    }
}
