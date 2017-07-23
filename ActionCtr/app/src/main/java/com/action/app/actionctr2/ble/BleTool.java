package com.action.app.actionctr2.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import java.util.List;
import java.util.UUID;

/**
 * Created by Administrator on 2017/7/23/023.
 */

public class BleTool {

    private final static UUID aimServiceUUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    private final static UUID aimChar6UUID = UUID.fromString("0000fff6-0000-1000-8000-00805f9b34fb");
    private final static UUID aimChar7UUID = UUID.fromString("0000fff7-0000-1000-8000-00805f9b34fb");

    private BluetoothGattService mGATTFirst;
    protected BluetoothGattCharacteristic charFirst6;
    protected BluetoothGattCharacteristic charFirst7;

    private BluetoothGattService mGATTSecond;
    protected BluetoothGattCharacteristic charSecond6;
    protected BluetoothGattCharacteristic charSecond7;
    
    BleTool() {
    }

    public void findService(int i, BluetoothGatt gatt) {
        switch (i) {
            case 1:
                mGATTFirst = gatt.getService(aimServiceUUID);
                charFirst6 = mGATTFirst.getCharacteristic(aimChar6UUID);
                charFirst7 = mGATTFirst.getCharacteristic(aimChar7UUID);
                enableNotification(gatt, aimServiceUUID, aimChar6UUID);
                enableNotification(gatt, aimServiceUUID, aimChar7UUID);
                break;
            case 2:
                mGATTSecond = gatt.getService(aimServiceUUID);
                charSecond6 = mGATTSecond.getCharacteristic(aimChar6UUID);
                charSecond7 = mGATTSecond.getCharacteristic(aimChar7UUID);
                enableNotification(gatt, aimServiceUUID, aimChar6UUID);
                enableNotification(gatt, aimServiceUUID, aimChar7UUID);
                break;
            case 3:
                break;
        }
    }


    public boolean enableNotification(BluetoothGatt gatt, UUID serviceUUID, UUID characteristicUUID) {
        boolean success = false;
        BluetoothGattService service = gatt.getService(serviceUUID);
        if (service != null) {
            BluetoothGattCharacteristic characteristic = findNotifyCharacteristic(service, characteristicUUID);
            if (characteristic != null) {
                success = gatt.setCharacteristicNotification(characteristic, true);
                if (success) {
                    for (BluetoothGattDescriptor dp : characteristic.getDescriptors()) {
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
//            判断特征值是否具备通知属性，并且UUID是否相符
            if ((c.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0
                    && characteristicUUID.equals(c.getUuid())) {
                characteristic = c;
                break;
            }
        }
        if (characteristic != null)
            return characteristic;
//            判断特征值是否具备indicate属性，并且UUID是否相符
        for (BluetoothGattCharacteristic c : characteristics) {
            if ((c.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0
                    && characteristicUUID.equals(c.getUuid())) {
                characteristic = c;
                break;
            }
        }
        return characteristic;
    }
}  




/**
 * 注册广播
 */
//    public void registerBTReceiver() {
//        // 设置广播信息过滤
//        IntentFilter filter = new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED");
//        filter.addAction("android.bluetooth.device.action.PAIRING_REQUEST");
//        filter.setPriority(Integer.MAX_VALUE);
//        // 注册广播接收器，接收并处理搜索结果
//        this.registerReceiver(BTReceive, filter);
//    }

/**
 * 广播接收者
 */
//    private BroadcastReceiver BTReceive = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            boolean isSuccess = false;
//            Log.d("bletrack",intent.getAction());
//            if (intent.getAction().equals("android.bluetooth.device.action.PAIRING_REQUEST")) {
//                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                if (!device.getAddress().equals(AIMADDRESS1))
//                    return;
//
//                try {
//                    int mType = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR);
//                    // 蓝牙有两种配对类型一种是pin，一种是密钥 先搞清楚你的设备是哪一种在做，不然瞎忙活
//                    switch (mType) {
//                        case 0:
//                            // 反射 不会自己收ClsUtils
//                            isSuccess = ClsUtils.setPin(device.getClass(), device, "123456");
//                            Log.d("bletrack","setPin");
//                            break;
//                        case 1:
//                            // 这个方法是我自己加的 ,不会的可以照着setPin写一个setPasskey boolean
//                            // Bluetooth.setPasskey(int)
//                            Log.d("bletrack","setpasscode");
//                    }
//
//                    //配对不成功就弹出来
//                    if (isSuccess) {
//                        // 重点 先前已经设置我的广播接收者的优先者为最高 收到广播后 停止广播向下传递
//                        // 因为系统的配对框的消失与弹出是通过广播做到的 我看源码得知 ，我们要将它
//                        // 扼杀在摇篮中
//                        abortBroadcast();
//                        // 怕没拦截成功 补上一刀 发送一个关闭对话框的广播
//                        context.sendBroadcast(new Intent("android.bluetooth.device.action.PAIRING_CANCEL"));
//                    }
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }else {
//                Log.d("bletrack",intent.getAction());
//            }
//
//        }
//    };//      5.0及之前的版本
//    /**
//     * 蓝牙扫描回调函数 实现扫描蓝牙设备，回调蓝牙BluetoothDevice，可以获取name MAC等信息
//     **/
//    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
//
//        @Override
//        public void onLeScan(final BluetoothDevice device, final int rssi,
//                             byte[] scanRecord) {
//            Log.d("bletrack", "find device: " + device.getName());
//            Log.d("bletrack", "device address: " + device.getAddress());
//            if (device.getAddress().equals(AIMADDRESS1) && mScanning) {
//                // TODO Auto-generated method stub
//                Log.d("bletrack", "RIGHT DEVICE");
//                mScanning = false;
//                mBluetoothDevice = device;
//                mRssi = rssi;
//                scanLeDevice(false);
//                connect(AIMADDRESS1);
//            }
//        }
//    };

//                    case BluetoothProfile.STATE_DISCONNECTED:               //断开连接
//                            mConnectionState = STATE_DISCONNECTED;
////                    加上会变快
//                            Log.d("bletrack", "ble disconnected");
//                            //秒连速度之快已经让车上蓝牙检测不到断开了
//                            if (isConnectPermit) {
//                            if (gatt.connect())//连接蓝牙，其实就是调用BluetoothGatt的连接方法
//                            {
//                            mConnectionState = STATE_CONNECTING;
//                            } else {
//                            }
//                            if (!connectionQueue.isEmpty()) {
//                            for (BluetoothGatt btg : connectionQueue) {
//                            if (btg.equals(gatt)) {
//                            gatt.close();
//                            connectionQueue.remove(btg);
//                            if (!connect(AIMADDRESS1)) {
//                            connect(AIMADDRESS1);
//                            Log.d("bletrack", "reconnect(AIMADDRESS1)");
//                            }
//                            }
//                            }
//                            }
//
//                            if (gatt.equals(connectionQueue.get(0))) {
//                            if (gatt != null)
//                            GattClose(1);
//                            if (!connect(AIMADDRESS1)) {
//                            connect(AIMADDRESS1);
//                            Log.d("bletrack", "reconnect(AIMADDRESS1)");
//                            }
//                            } else if (gatt.equals(connectionQueue.get(1))) {
//                            if (gatt != null)
//                            GattClose(2);
//                            if (!connect(AIMADDRESS2)) {
//                            connect(AIMADDRESS2);
//                            Log.d("bletrack", "reconnect(AIMADDRESS2)");
//                            }
//                            }
//                            }
//
//
//                            //scanLeDevice(true);
//                            isReadyForNext = false;