package com.action.app.actionctr.ble;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.action.app.actionctr.BeginActivity;
import com.action.app.actionctr.wifi.wifiService;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BleService extends Service {

    static {
        System.loadLibrary("native-lib");
    }

    private BluetoothManager bleManager;
    private BluetoothAdapter bleAdapter;
    private ArrayList<BluetoothDevice> devicesList;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;
    private BluetoothGattCallback mGattCallback;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic characteristic;
    private BluetoothGattCharacteristic characteristicHB;
    private BluetoothGattService        gattService;
    private boolean isReadyForNext=false;
    private int RssiValue=0;

    public static final int bleDataLen=12;
    private final String address="F4:5E:AB:B9:59:77";//这个参数是车上用的平板 1号
//    private final String address="F4:5E:AB:B9:58:80";//2号 白色平板
//    private final String address="F4:5E:AB:B9:5A:03";// //3号
    private Handler handler;

    private byte[] dataReceive;
    private byte[] dataTrans;
    private byte[] dataHeartBeats;

    private int HBcount=0;

    private myBleBand dataSend=new myBleBand();
    public class myBleBand extends Binder {
        byte count=0;
        boolean isBusy=false;
        Handler handler=new Handler();
        public void send(byte[] data){
            if(data.length!=bleDataLen){
                Log.e("version err","length of senddata is not equal to require");
            }
            dataTrans=data;
            dataTrans[bleDataLen-1]=count;
            if(count<100)
                count++;
            else
                count=0;
            if(characteristic!=null&&mBluetoothGatt!=null) {
                characteristic.setValue(dataTrans);
                mBluetoothGatt.writeCharacteristic(characteristic);
            }
            OutputStream out=wifiService.getOutputStream();
            if(out!=null) {
                try {
                    out.write(dataTrans);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("wifi", "ble Exception: " + Log.getStackTraceString(e));
                }
            }

            final Runnable runnable=new Runnable() {
                @Override
                public void run() {
                    if(!checkSendOk()) {
                        isBusy=true;
                        if(characteristic!=null&&mBluetoothGatt!=null) {
                            characteristic.setValue(dataTrans);
                            mBluetoothGatt.writeCharacteristic(characteristic);
                        }
                        handler.postDelayed(this,100);
                    }
                    else{
                        isBusy=false;
                    }
                }
            };
            if(!isBusy)
                handler.postDelayed(runnable,100);
        }
        public byte[] getHeartBeats() {
            return dataHeartBeats;
        }
        public boolean checkSendOk(){
            if(!isReadyForNext) {
                return true;
            }
            if(Arrays.equals(dataReceive,dataTrans)) {
                return true;
            }
            if(dataReceive!=null&&dataTrans!=null) {
                int i;
                for(i=0;i<10;i++) {
                   if(dataReceive[i]!=dataTrans[i])
                       break;
                }
                if(i==10) {
                    Log.e("ble","communicate unstable");
                    return true;
                }
            }
            return false;
        }
        public boolean isReady(){
            return isReadyForNext;
        }
        public int readRssi(){
            if(isReadyForNext) {
                mBluetoothGatt.readRemoteRssi();
                return RssiValue;
            }
            else{
                return 0;
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return dataSend;
    }
    @Override
    public void onCreate(){
        super.onCreate();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(android.R.drawable.btn_dialog);
        builder.setContentTitle("ActionCtrBle");
        builder.setContentText("为了保证Ble的长期不被系统干掉");
        Intent intent = new Intent(this, BeginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        startForeground(1, notification);


        Log.d("Ble","Ble Service onCreate");

        bleManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bleAdapter= bleManager.getAdapter();
        devicesList=new ArrayList<>();
        mGattCallback=new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        if(gatt.getDevice().getAddress().equals(address)){
                            Log.d("Ble","ble connected");
                            isReadyForNext=false;
                            mBluetoothGatt.discoverServices();
                        }
                        else {
                            Log.d("Ble","ble devicce err");
                            gatt.disconnect();
                        }
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        gatt.close();
                        bleAdapter.startLeScan(mLeScanCallback);
                        isReadyForNext=false;
                        Log.d("Ble","ble disconnected");
                        break;
                    default:
                        isReadyForNext=false;
                        break;
                }
            }
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt,int status){
                if(status==BluetoothGatt.GATT_SUCCESS){
                    Log.d("Ble","ble gatt service success");
                    isReadyForNext=true;
                    gattService=mBluetoothGatt.getService(UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb"));
                    characteristic=gattService.getCharacteristic(UUID.fromString("0000fff6-0000-1000-8000-00805f9b34fb"));
                    characteristicHB=gattService.getCharacteristic(UUID.fromString("0000fff7-0000-1000-8000-00805f9b34fb"));

                    mBluetoothGatt.setCharacteristicNotification(characteristic,true);
                    mBluetoothGatt.setCharacteristicNotification(characteristicHB,true);
                }
                else {
                    isReadyForNext=false;
                    Log.d("Ble","ble gatt service fail");
                }
            }
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                String log_out=new String();
                for (int i=0;i<bleDataLen;i++){
                    log_out+=String.valueOf((int)characteristic.getValue()[i])+'\t';
                }
                Log.d("Ble","read value: "+log_out);
            }
            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
                byte[] temp;
                temp=characteristic.getValue();
                if(temp[0]=='A'&&temp[1]=='C'&&temp[2]=='H'&&temp[3]=='B')
                {
                    dataHeartBeats=temp;
                    HBcount++;
                }
                else{
                    dataReceive=temp;
                    String log_out=new String();
                    for (int i=0;i<12;i++){
                        log_out+=String.valueOf((int)dataReceive[i])+'\t';
                    }

                    if(dataReceive.length!=bleDataLen){
                        Log.e("version err","length of receivedata is not equal to require");
                    }
                    Log.d("Ble","notify: "+log_out);
                }
            }
            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){
                String log_out=new String();
                for (int i=0;i<12;i++){
                    log_out+=String.valueOf((int)characteristic.getValue()[i])+'\t';
                }
                Log.d("Ble","write: "+log_out);
            }
            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                RssiValue=rssi;
            }
        };
        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                if (!devicesList.contains(device)) {
                    devicesList.add(device);
                    Log.d("Ble", "find device , name= " + device.getName());
                    Log.d("Ble", "device address="+device.getAddress());

                    if(device.getAddress().equals(address)){
                        devicesList.clear();
                        bleAdapter.stopLeScan(mLeScanCallback);
                        mBluetoothGatt=device.connectGatt(BleService.this, false, mGattCallback);
                    }
                }
            }
        };
        List<BluetoothDevice> list=bleManager.getConnectedDevices(BluetoothProfile.GATT);
        if(list.size()==0){
            bleAdapter.startLeScan(mLeScanCallback);
        }
        else {
            mBluetoothGatt=null;
            BluetoothGatt temp;
            for (BluetoothDevice device:list) {
                temp=device.connectGatt(BleService.this, false, mGattCallback);
                if(device.getAddress().equals(address)){
                    temp.discoverServices();
                    mBluetoothGatt=temp;
                }
                else{
                    //连续执行两条的目的是使上面不会进入断开连接的回调
                    temp.disconnect();
                    temp.close();
                }
                if(mBluetoothGatt==null) {
                    bleAdapter.startLeScan(mLeScanCallback);
                }
            }
        }
        handler=new Handler();

        //下面的代码用于发送心跳包
        final Handler handlerHeartBeat=new Handler();
        Runnable runnable=new Runnable() {
            private int errCount=0;
            private int lastHBcount=0;
            @Override
            public void run() {
                if(isReadyForNext){
                    byte[] heartBeat=new byte[bleDataLen];
                    heartBeat[0]='A';
                    heartBeat[1]='C';
                    heartBeat[2]='H';
                    heartBeat[3]='B';
                    if(characteristicHB!=null) {
                        characteristicHB.setValue(heartBeat);
                        mBluetoothGatt.writeCharacteristic(characteristicHB);
                    }
                    if(HBcount==lastHBcount)
                        errCount++;
                    else
                        errCount=0;
                    lastHBcount=HBcount;
                    if(errCount>=15) {
                        Log.e("Ble","HeartBeats disconnect");
                        isReadyForNext=false;
                        mBluetoothGatt.disconnect();
                        errCount=0;
                    }
                }
                handlerHeartBeat.postDelayed(this,300);
            }
        };
        handlerHeartBeat.postDelayed(runnable,3000);
    }
    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        Log.d("Ble","Ble Service onStartCommand");
        return super.onStartCommand(intent,flags,startId);
    }
    @Override
    public void onDestroy(){
        Log.d("Ble","Ble Service onDestroy");
        super.onDestroy();
        if(mBluetoothGatt!=null)
            mBluetoothGatt.close();
    }
}
