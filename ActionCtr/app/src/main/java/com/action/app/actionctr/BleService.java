package com.action.app.actionctr;

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
import android.os.IBinder;
import android.util.Log;
import java.util.ArrayList;
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

    public interface BleProfile{
         int BLE_CONNECTED=0;
         int BLE_DISCONNECTED=1;
         int BLE_SCANING=2;
         int BLE_IDLE=3;
    }
    private DataSend dataSend=new DataSend();
    public class DataSend extends Binder {

        private int ble_status=BleProfile.BLE_IDLE;
        public void send(byte[] data){
            Log.d("Ble","dataSend = "+data.toString());
            characteristic.setValue(data);
            mBluetoothGatt.writeCharacteristic(characteristic);
        }
        public void send(String s){
            Log.d("Ble","dataSend = "+s);
            characteristic.setValue(s);
            mBluetoothGatt.writeCharacteristic(characteristic);
        }
        public int  getBleStatus()
        {
            return ble_status;
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
                switch (newState)
                {
                    case BluetoothProfile.STATE_CONNECTED:
                        dataSend.ble_status=BleProfile.BLE_CONNECTED;
                        Log.d("Ble","ble connected");
                        mBluetoothGatt.discoverServices();
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        Log.d("Ble","ble disconnected");
                        break;
                }
            }
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt,int status){
                if(status==BluetoothGatt.GATT_SUCCESS)
                    Log.d("Ble","ble gatt success");
                else
                    Log.d("Ble","ble gatt fail");
                Log.d("Ble","onServiceDiscovered: " + status);
                gattService=mBluetoothGatt.getService(UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb"));
                characteristic=gattService.getCharacteristic(UUID.fromString("0000fff6-0000-1000-8000-00805f9b34fb"));
                characteristic.setValue(new String("AC_START_000"));
                mBluetoothGatt.writeCharacteristic(characteristic);
                mBluetoothGatt.setCharacteristicNotification(characteristic,true);
            }
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                Log.d("Ble","read value: "+characteristic.getStringValue(0));
            }
            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
                Log.d("Ble","notify: "+characteristic.getStringValue(0));
            }
            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){
                Log.d("Ble","write: "+characteristic.getStringValue(0));
            }
        };
        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                if (!devicesList.contains(device)) {
                    devicesList.add(device);
                    Log.d("Ble", "find device , name= " + device.getName());
                    bleAdapter.stopLeScan(mLeScanCallback);
                    mBluetoothGatt=device.connectGatt(BleService.this, false, mGattCallback);
                }
            }
        };
        bleAdapter.startLeScan(mLeScanCallback);
        dataSend.ble_status=BleProfile.BLE_SCANING;
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
