package com.action.app.actionctr2.ble;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.action.app.actionctr2.myTool;

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
            Log.d("bleDataProcess","Service Connected");
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e("bleDataProcess","Service disconnected");
        }
    };
    public bleDataProcess(Context text){
        Intent bindIntent=new Intent(text,BleService.class);
        context=text;
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
            String log_out = new String();
            for (int i = 0; i < 12; i++) {
                    log_out += String.valueOf((int) sendData[i]) + '\t';
            }
            Log.d("bletrack", "write: " + log_out);
            if(state!=null)
                state.send(sendData);
            return true;
        }
        Log.e("bletrack","dataSend length err");
        return false;
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
            Log.d("datasend","sendParam ");
            if(state!=null)
                state.send(sendData);
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
            if(state!=null) {
                state.send(sendData);
            }
            return true;
        }
        Log.e("bletrack","dataSend length err");
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
            if(state!=null)
                state.send(sendData);
            return true;
        }
        Log.e("bletrack","dataSend length err");
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
    public byte[] getMCUinfo(){return state.getHeartBeats();}
    public int readRssi(){
        return state.readRssi();
    }
    public void unbind(){
        context.unbindService(connection);
    }
}
