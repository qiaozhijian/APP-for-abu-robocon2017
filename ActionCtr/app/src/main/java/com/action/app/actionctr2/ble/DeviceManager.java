package com.action.app.actionctr2.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class DeviceManager {

    protected static final int STATE_DISCONNECTED = 0;
    
    protected boolean isConnectPermit = false;
    protected boolean isReadyForNextFor = false;
    protected byte[] dataReceive;                 //    接收数据缓存区
    protected byte[] dataHeartBeats;              //    心跳包的缓存区
    protected int HBcount = 0;                      //    心跳包的计数
    protected int RssiValue = 0;                    //    RSSI
    protected String aimAddress;
    protected int connectionState=STATE_DISCONNECTED;
    private final static UUID aimServiceUUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    private final static UUID aimChar6UUID = UUID.fromString("0000fff6-0000-1000-8000-00805f9b34fb");
    private final static UUID aimChar7UUID = UUID.fromString("0000fff7-0000-1000-8000-00805f9b34fb");
    private BluetoothGattService mGATTSevice;
    protected BluetoothGattCharacteristic characteristic6;
    protected BluetoothGattCharacteristic characteristic7;
    protected int connectTime=0;
    protected ArrayList<Runnable> runnables=new ArrayList<Runnable>();

    protected ArrayList<Runnable> serRunnables=new ArrayList<Runnable>();

    DeviceManager(String aimAddress) {
        connectionQueue.add(mblueToothGatt1);
        connectionQueue.add(mblueToothGatt2);
        this.aimAddress = aimAddress;
    }

    public DeviceManager()
    {
        super();
    }

    protected static ArrayList<BluetoothGatt> connectionQueue = new ArrayList<BluetoothGatt>();

    protected static BluetoothGatt mblueToothGatt1;

    protected static BluetoothGatt mblueToothGatt2;

    public void findService(BluetoothGatt gatt) {

        mGATTSevice = gatt.getService(aimServiceUUID);
        characteristic6 = mGATTSevice.getCharacteristic(aimChar6UUID);
        characteristic7 = mGATTSevice.getCharacteristic(aimChar7UUID);
        enableNotification(gatt, aimServiceUUID, aimChar6UUID);
        enableNotification(gatt, aimServiceUUID, aimChar7UUID);


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

    String getCharValue(BluetoothGattCharacteristic characteristic) {
        String log_out = new String();
        for (int i = 0; i < 12; i++) {
            if (i < 4)
                log_out += String.valueOf((char) characteristic.getValue()[i]) + '\t';
            else
                log_out += String.valueOf(characteristic.getValue()[i]) + '\t';
        }
        return log_out;
    }

    protected static boolean checkGATT(int order) {

        if (connectionQueue.size() > order) {
            if (connectionQueue.get(order) != null)
                return true;
            else {
                Log.d("checkGATT", String.valueOf(order) + " null");
                return false;
            }
        } else {
            Log.d("checkGATT", String.valueOf(order) + " size fail");
            return false;
        }
    }

    protected static boolean checkGATT(BluetoothGatt gatt, int order) {

        if (connectionQueue.size() > order) {
            if (gatt.equals(connectionQueue.get(order))) {
                Log.d("checkGATT", String.valueOf(order) + "  equal ");
                return true;
            } else {
                Log.d("checkGATT", String.valueOf(order) + "  equal null");
                return false;
            }
        } else {
            Log.d("checkGATT", String.valueOf(order) + "equal size fail");
            return false;
        }
    }


    protected static boolean checkGATT(BluetoothGatt bluetoothGatt) {
        if (!connectionQueue.isEmpty()) {
            for (BluetoothGatt btg : connectionQueue) {
                if (btg.equals(bluetoothGatt)) {
                    return false;
                }
            }
        }
        return true;
    }

    protected static boolean checkGATT(BluetoothGattCharacteristic blechar, BluetoothGattCharacteristic characteristic) {

        if (blechar != null && characteristic != null) {
            if (characteristic.equals(blechar))
                return true;
            else {
                Log.d("checkGATT", blechar.toString() + "blechar equal fail");
                return false;
            }
        } else {
            Log.d("checkGATT", blechar.toString() + "blechar null");
            return false;
        }
    }
    protected boolean checkConRun(Runnable runnable) {
        if (!runnables.isEmpty()) {
            for (Runnable run : runnables) {
                if (run.equals(runnable)) {
                    return false;
                }
            }
        }
        return true;
    }

    protected boolean checkSerRun(Runnable runnable) {
        if (!serRunnables.isEmpty()) {
            for (Runnable run : serRunnables) {
                if (run.equals(runnable)) {
                    return false;
                }
            }
        }
        return true;
    }

}










/**
 * 注册广播
 * <p>
 * 广播接收者
 * <p>
 * 广播接收者
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