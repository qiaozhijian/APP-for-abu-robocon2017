package com.action.app.actionctr.ble;

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
import android.util.Log;

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
    private BluetoothGattService        gattService;
    private boolean isReadyForNext=false;
    private int RssiValue=0;

    public static final int bleDataLen=12;
    private final String address="90:59:AF:0E:62:A4";
    Handler handler;

    private byte[] dataReceive;
    private byte[] dataTrans;


    private myBleBand dataSend=new myBleBand();
    public class myBleBand extends Binder {
        public void send(byte[] data){
            if(data.length!=bleDataLen){
                Log.e("version err","length of senddata is not equal to require");
            }
            dataTrans=data;
            characteristic.setValue(dataTrans);
            mBluetoothGatt.writeCharacteristic(characteristic);

            final Runnable runnable=new Runnable() {
                @Override
                public void run() {
                    if(!checkSendOk()) {
                        characteristic.setValue(dataTrans);
                        mBluetoothGatt.writeCharacteristic(characteristic);
                        Log.w("Ble","communication with mcu may not be stable");
                        handler.postDelayed(this,30);
                    }
                }
            };
            handler.postDelayed(runnable,30);
        }
        public boolean checkSendOk(){
            if(Arrays.equals(dataReceive,dataTrans)) {
                return true;
            }
            return false;
        }
        public boolean isReady(){
            return isReadyForNext;
        }
        public int readRssi(){
            mBluetoothGatt.readRemoteRssi();
            return RssiValue;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return dataSend;
    }
    @Override
    public void onCreate(){
        super.onCreate();
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
                }
                else {
                    isReadyForNext=false;
                    Log.d("Ble","ble gatt service fail");
                }

                gattService=mBluetoothGatt.getService(UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb"));
                characteristic=gattService.getCharacteristic(UUID.fromString("0000fff6-0000-1000-8000-00805f9b34fb"));
                mBluetoothGatt.setCharacteristicNotification(characteristic,true);
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
                dataReceive=characteristic.getValue();

                String log_out=new String();
                for (int i=0;i<12;i++){
                    log_out+=String.valueOf((int)dataReceive[i])+'\t';
                }

                if(dataReceive.length!=bleDataLen){
                    Log.e("version err","length of receivedata is not equal to require");
                }
                Log.d("Ble","notify: "+log_out);
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
        bleAdapter.startLeScan(mLeScanCallback);
        handler=new Handler();
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
    }
}
