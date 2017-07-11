package com.action.app.actionctr2.ble;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.action.app.actionctr2.BeginActivity;
import com.action.app.actionctr2.wifi.wifiService;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class BleService extends Service {

    static {
        System.loadLibrary("native-lib");
    }

    private boolean isSending = false;

    private BluetoothManager bleManager;
    private BluetoothAdapter bleAdapter;

    private BluetoothLeScanner scanner;
    private ScanCallback scanCallback;
    private BluetoothGattCallback mGattCallback;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic characteristic;
    private BluetoothGattCharacteristic characteristicHB;
    private BluetoothGattService gattService;
    private BluetoothDevice device;
    private boolean isReadyForNext = false;
    private boolean isScanning = false;
    private int RssiValue = 0;

    public static final int bleDataLen = 12;
    private final String address = "F4:5E:AB:B9:58:80";//1号 黄色平板
    //  private final String address="50:65:83:86:C6:33";// //白色平板
    //  private final String address="F4:5E:AB:B9:59:77";//4号
    //  private final String address="F4:5E:AB:B9:5A:03";// //3号

    private byte[] dataReceive;
    private byte[] dataTrans;
    private byte[] dataHeartBeats;

    private int HBcount = 0;

    private void wifiSend(byte[] data) {
        OutputStream out = wifiService.getOutputStream();
        if (out != null) {
            try {
                out.write(data);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("wifi", "ble Exception: " + Log.getStackTraceString(e));
            }
        }
    }

    private myBleBand dataSend = new myBleBand();

    public class myBleBand extends Binder {
        byte count = 0;
        boolean isBusy = false;
        Handler handler = new Handler();

        public void send(byte[] data) {
            isSending = true;
            if (data.length != bleDataLen) {
                Log.e("version err", "length of senddata is not equal to require");
            }
            dataTrans = data;
            dataTrans[bleDataLen - 1] = count;
            if (count < 100)
                count++;
            else
                count = 0;
            if (characteristic != null && mBluetoothGatt != null) {
                characteristic.setValue(dataTrans);
                mBluetoothGatt.writeCharacteristic(characteristic);
            }
            wifiSend(dataTrans);

            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (!checkSendOk()) {
                        isBusy = true;
                        if (characteristic != null && mBluetoothGatt != null) {
                            characteristic.setValue(dataTrans);
                            mBluetoothGatt.writeCharacteristic(characteristic);
                        }
                        handler.postDelayed(this, 100);
                    } else {
                        isBusy = false;
                    }
                }
            };
            if (!isBusy)
                handler.postDelayed(runnable, 100);
            isSending = false;
        }

        public byte[] getHeartBeats() {
            return dataHeartBeats;
        }

        public boolean checkSendOk() {
            if (!isReadyForNext) {
                return true;
            }
            if (Arrays.equals(dataReceive, dataTrans)) {
                return true;
            }
            if (dataReceive != null && dataTrans != null) {
                int i;
                for (i = 0; i < 10; i++) {
                    if (dataReceive[i] != dataTrans[i])
                        break;
                }
                if (i == 10) {
                    Log.e("ble", "communicate unstable");
                    return true;
                }
            }
            return false;
        }

        public boolean isReady() {
            return isReadyForNext;
        }

        public int readRssi() {
            if (isReadyForNext) {
                mBluetoothGatt.readRemoteRssi();
                return RssiValue;
            } else {
                return 0;
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return dataSend;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(android.R.drawable.btn_dialog);
        builder.setContentTitle("ActionCtrBle");
        builder.setContentText("为了保证Ble的长期不被系统干掉");
        Intent intent = new Intent(this, BeginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        startForeground(1, notification);

        Log.d("Ble", "Ble Service onCreate");

        mGattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        if (gatt.getDevice().getAddress().equals(address)) {
                            Log.d("Ble", "ble connected");
                            isReadyForNext = false;
                            mBluetoothGatt.discoverServices();
                        } else {
                            Log.d("Ble", "ble devicce err");
                            gatt.disconnect();
                        }
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            gatt.close();
                            scanner.startScan(scanCallback);
                            isScanning = true;
                            Log.d("Ble", "reconnect");
                        } else if (status == BluetoothGatt.GATT_FAILURE || status == 0x85) {
                            gatt.close();
                            Log.d("Ble", "gatt close reconnect"+ String.valueOf(status));
                            mBluetoothGatt = device.connectGatt(BleService.this, false, mGattCallback);
                        } else {
//                            8
                            Log.e("Ble", "unkown disconnected: " + String.valueOf(status));
                            gatt.close();
                            mBluetoothGatt = device.connectGatt(BleService.this, false, mGattCallback);
                            //scanner.startScan(scanCallback);
                            //isScanning = true;
                        }
                        isReadyForNext = false;
                        Log.d("Ble", "ble disconnected");
                        break;
                    default:
                        Log.e("Ble", "Unknown state");
                        isReadyForNext = false;
                        break;
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("Ble", "ble gatt service success");
                    isReadyForNext = true;
                    gattService = mBluetoothGatt.getService(UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb"));
                    characteristic = gattService.getCharacteristic(UUID.fromString("0000fff6-0000-1000-8000-00805f9b34fb"));
                    characteristicHB = gattService.getCharacteristic(UUID.fromString("0000fff7-0000-1000-8000-00805f9b34fb"));

//                    mBluetoothGatt.setCharacteristicNotification(characteristic, true);
//                    mBluetoothGatt.setCharacteristicNotification(characteristicHB, true);
                   // setCharacteristicNotification(characteristicHB,true);
                    enableNotification(mBluetoothGatt,
                            UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb"),
                            UUID.fromString("0000fff7-0000-1000-8000-00805f9b34fb"));
                } else {
                    isReadyForNext = false;
                    Log.d("Ble", "ble gatt service fail");
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                String log_out = new String();
                for (int i = 0; i < bleDataLen; i++) {
                    log_out += String.valueOf((int) characteristic.getValue()[i]) + '\t';
                }
                Log.d("Ble", "read value: " + log_out);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                byte[] temp;
                String log_out = new String();
                for (int i = 0; i < 12; i++) {
                    log_out += String.valueOf((int) dataReceive[i]) + '\t';
                }
                temp = characteristic.getValue();
                if (temp[0] == 'A' && temp[1] == 'C' && temp[2] == 'H' && temp[3] == 'B') {
                    dataHeartBeats = temp;
                    HBcount++;
                 //   Log.d("Ble", "heartBeats Receive");
                } else {
                    dataReceive = temp;

                    if (dataReceive.length != bleDataLen) {
                        Log.e("version err", "length of receivedata is not equal to require");
                    }
                    //Log.d("Ble", "notify: " + log_out);
                }
                Log.d("Ble", "notify: " + log_out);
            }
            public void setCharacteristicNotification(
                    BluetoothGattCharacteristic characteristic, boolean enabled) {
                if (bleAdapter == null || mBluetoothGatt == null) {
                    Log.w(TAG, "BluetoothAdapter not initialized");
                    return;
                }
                mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

                BluetoothGattDescriptor clientConfig = characteristic
                        .getDescriptor(UUID
                                .fromString("0000fff7-0000-1000-8000-00805f9b34fb"));

                if (enabled) {
                    clientConfig
                            .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                } else {
                    clientConfig
                            .setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                }
                mBluetoothGatt.writeDescriptor(clientConfig);
            }

            public boolean enableNotification(BluetoothGatt gatt, UUID serviceUUID, UUID characteristicUUID) {
                boolean success = false;
                BluetoothGattService service = gatt.getService(serviceUUID);
                if (service != null) {
                    BluetoothGattCharacteristic characteristic = findNotifyCharacteristic(service, characteristicUUID);
                    if (characteristic != null) {
                        success = gatt.setCharacteristicNotification(characteristic, true);
                        if (success) {
                            for(BluetoothGattDescriptor dp: characteristic.getDescriptors()){
                                if (dp != null) {
                                    if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                                        dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                    } else if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                                        dp.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                                    }
                                    gatt.writeDescriptor(dp);
                                }
                            }
                        }
                    }
                }
                return success;
            }

            private BluetoothGattCharacteristic findNotifyCharacteristic(BluetoothGattService service, UUID characteristicUUID) {
                BluetoothGattCharacteristic characteristic = null;
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                for (BluetoothGattCharacteristic c : characteristics) {
                    if ((c.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0
                            && characteristicUUID.equals(c.getUuid())) {
                        characteristic = c;
                        break;
                    }
                }
                if (characteristic != null)
                    return characteristic;
                for (BluetoothGattCharacteristic c : characteristics) {
                    if ((c.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0
                            && characteristicUUID.equals(c.getUuid())) {
                        characteristic = c;
                        break;
                    }
                }
                return characteristic;
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                String log_out = new String();
                for (int i = 0; i < 12; i++) {
                    log_out += String.valueOf((int) characteristic.getValue()[i]) + '\t';
                }
                if (characteristic.getValue()[0] == 'A' && characteristic.getValue()[1] == 'C' && characteristic.getValue()[2] == 'H' && characteristic.getValue()[3] == 'B') {

                }
                else
                    Log.d("Ble", "write: " + log_out);
            }

            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                RssiValue = rssi;
            }
        };


        //mBluetoothGatt=bleAdapter.getRemoteDevice(address).connectGatt(BleService.this, true, mGattCallback);
        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                BluetoothDevice deviceScan = result.getDevice();
                Log.d("Ble", "find device: " + deviceScan.getName());
                Log.d("Ble", "device address: " + deviceScan.getAddress());
                if (deviceScan.getAddress().equals(address) && isScanning) {
                    Log.d("Ble", "start connecting");
                    device = deviceScan;
                    mBluetoothGatt = deviceScan.connectGatt(BleService.this, false, mGattCallback);
                    scanner.stopScan(scanCallback);
                    isScanning = false;
                }
            }
        };
        try {
            bleManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            bleAdapter = bleManager.getAdapter();
            scanner = bleAdapter.getBluetoothLeScanner();
            scanner.startScan(scanCallback);
            isScanning = true;
            //下面的代码用于发送心跳包
            final Handler handlerHeartBeat = new Handler();
            Runnable runnable = new Runnable() {
                private int errCount = 0;
                private int lastHBcount = 0;
                private boolean lastIsReadyForNext = false;
                private int heartBeatDelayCount = 0;

                @Override
                public void run() {
                    byte[] heartBeat = new byte[bleDataLen];
                    heartBeat[0] = 'A';
                    heartBeat[1] = 'C';
                    heartBeat[2] = 'H';
                    heartBeat[3] = 'B';
                    if (!isSending) {
                        wifiSend(heartBeat);
                    }
                    if (isReadyForNext || lastIsReadyForNext) {
                        heartBeatDelayCount++;
                    } else {
                        heartBeatDelayCount = 0;
                    }
                    if (isReadyForNext && lastIsReadyForNext && heartBeatDelayCount > 7) {
                        if (characteristicHB != null) {
                            characteristicHB.setValue(heartBeat);
                            mBluetoothGatt.writeCharacteristic(characteristicHB);
                        }
                        if (HBcount == lastHBcount)
                            errCount++;
                        else
                            errCount = 0;
                        lastHBcount = HBcount;
                        if (errCount >= 15) {
                            Log.e("Ble", "HeartBeats disconnect");
                            isReadyForNext = false;
                            mBluetoothGatt.disconnect();
                            errCount = 0;
                        }
                    }
                    lastIsReadyForNext = isReadyForNext;
                    handlerHeartBeat.postDelayed(this, 300);
                }
            };
            handlerHeartBeat.postDelayed(runnable, 500);
        } catch (Exception e) {
            Log.e("Ble", e.getMessage());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Ble", "Ble Service onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d("Ble", "Ble Service onDestroy");
        super.onDestroy();
        if (mBluetoothGatt != null)
            mBluetoothGatt.close();
    }
}
