package com.action.app.actionctr2.ble;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
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
import android.widget.Toast;

import com.action.app.actionctr2.BeginActivity;
import com.action.app.actionctr2.wifi.wifiService;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

//连接重连，断开重连，发现服务过长
public class BleService extends Service {

    private final IBinder mBinder = new myBleBand();

    private final String AIMADDRESS1 = "C8:FD:19:59:10:4B";//手机 1
    private final String AIMADDRESS2 = "C8:FD:19:59:10:37";//手机 1
    private final String AIMADDRESS3 = "C8:FD:19:59:10:29";//手机 1
    //有一个默认第一设备，第二设备，如果扫描就是哪个先进哪个是，如果直接连接就用默认的
//    private final String AIMADDRESS1 = "F4:5E:AB:B9:58:80";//1号平板  1  一号试场
//    private final String AIMADDRESS2 = "98:7B:F3:60:C7:1C";//1号平板  2  手机试场
//    private final String AIMADDRESS3 = "F4:5E:AB:B9:5A:03";// 2号平板 1  2号试场
//    private final String AIMADDRESS1 = "98:7B:F3:60:C7:01";//手机 1
//    private final String AIMADDRESS2 = "90:59:AF:0E:60:1F";//手机 2
//    private final String AIMADDRESS3 = "F4:5E:AB:B9:59:77";//手机 3
    //private final String AIMADDRESS1="50:65:83:86:C6:33";//已坏的新模块

    private DeviceManager deviceFirst = new DeviceManager(AIMADDRESS1);
    private DeviceManager deviceSecond = new DeviceManager(AIMADDRESS2);
    private DeviceManager deviceThird = new DeviceManager(AIMADDRESS3);
    //蓝牙相关类
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner scaner;  // android5.0把扫描方法单独弄成一个对象了

    private ArrayList<BluetoothDevice> mDeviceContainer = new ArrayList<BluetoothDevice>();

    public static final int bleDataLen = 12;      //    特征值的长度
    private byte[] dataTrans;                   //    发送数据缓存区

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private boolean isHBSending = false;                //是否在发送
    private boolean isDataSending = false;

    private boolean isNeedForScan = false;

    private static boolean occupyState = false;
    static int sendOrder = 0;

    private final Handler handler = new Handler();

    private int tryField = 0;
    private final int tryPara = 2;

    // 描述扫描蓝牙的状态.
    private boolean mScanning;

    /**
     * @param enable (扫描使能，true:扫描开始,false:扫描停止)
     *///不能循环扫描，会卡死
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            /* 开始扫描蓝牙设备，带mLeScanCallback 回调函数 */
            Log.i("bletrack", "begin scanning");
            mScanning = true;
            //在扫描前，最好先调用一次停止扫描
            //scaner.stopScan(mScanCallbac  k);   // 这时会引用空对象
            scaner.startScan(mScanCallback);  // 开始扫描
            //  mBluetoothAdapter.startLeScan(aimUUID, mLeScanCallback);5.0及之前的版本
        } else {
            Log.i("bletrack", "stoping scanning");
            mScanning = false;
            //mBluetoothAdapter.stopLeScan(mLeScanCallback);5.0及之前的版本
            scaner.stopScan(mScanCallback);   // 停止扫描
        }
    }


    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice deviceScan = result.getDevice();
            if (isNeedForScan) {
                if (deviceScan.getAddress() != null || deviceScan.getName() != null) {
                    //sharedPreferencesHelper.putString("returnState", returnState);
                    Log.d("bletrack", "STATE_CONNECTION_FAIL_SCAN ");
                }
            }

            if (deviceScan.getAddress().equals(AIMADDRESS1) ||
                    deviceScan.getAddress().equals(AIMADDRESS2)) {
                if (!mDeviceContainer.isEmpty()) {
                    if (deviceFirst.aimAddress.equals(AIMADDRESS1)) {
                        Log.d("bletrack", "deviceFirst.aimAddress.equals(AIMADDRESS1) ");
                    } else if (deviceFirst.aimAddress.equals(AIMADDRESS2)) {
                        Log.d("bletrack", "deviceFirst.aimAddress.equals(AIMADDRESS2) ");
                        deviceSecond.aimAddress = AIMADDRESS1;
                    }
                    if (!isEquals(deviceScan)) {
                        Log.d("bletrack", "add new device");
                        Log.d("bletrack", "find device: " + deviceScan.getName());
                        Log.d("bletrack", "device address: " + deviceScan.getAddress());
                        connectBle(deviceScan);
                    }
                } else {
                    deviceFirst.aimAddress = deviceScan.getAddress();
                    Log.d("bletrack", "mDeviceContainer is empty ");
                    Log.d("bletrack", "add new device");
                    Log.d("bletrack", "find device: " + deviceScan.getName());
                    Log.d("bletrack", "device address: " + deviceScan.getAddress());
                    connectBle(deviceScan);
                }
                if (isNeedForScan) {
                    //sharedPreferencesHelper.putString("returnState", returnState);
                    Log.d("bletrack", "STATE_CONNECTION_FAIL_SCAN_RIGHT ");
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            // 扫描失败，并且失败原因
            Log.d("bletrack", "扫描失败" + String.valueOf(errorCode));
        }
    };

    private boolean isEquals(BluetoothDevice device) {
        for (BluetoothDevice mDdevice : mDeviceContainer) {
            if (mDdevice.equals(device)) {
                return true;
            }
        }
        return false;
    }


    private void connectBle(BluetoothDevice device) {
        mDeviceContainer.add(device);
        if (mDeviceContainer.size() == 2)
            scanLeDevice(false);
        connectSafe(device.getAddress());
    }

    //final Handler conHandler = new Handler();

    //Android十秒连一次
    public void connectSafe(final String address) {

        if (address.equals(deviceFirst.aimAddress) && deviceFirst.connectionState != STATE_CONNECTED) {
            //创建DeviceManager.connectionQueue.get(0)时会进入mGattCallback
            deviceFirst.isConnectPermit = false;
            Log.d("bletrack", "1 connectsafe");
            connectFirst(address);
        } else if (address.equals(deviceSecond.aimAddress) && deviceSecond.connectionState != STATE_CONNECTED) {
            connectSecond(address);
            deviceSecond.isConnectPermit = false;
            Log.d("bletrack", "2 connectsafe");
        } else if (address.equals(deviceThird.aimAddress) && deviceThird.connectionState != STATE_CONNECTED) {
            connectThird(address);
            deviceThird.isConnectPermit = false;
            Log.d("bletrack", "3 connectsafe");
        }
    }

    // 连接远程蓝牙
    public boolean connectFirst(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w("bletrack",
                    "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        isNeedForScan = false;

        /* 获取远端的蓝牙设备 */
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        if (device == null) {
            Log.w("bletrack", "Device not found.  Unable to connect.");
            return false;
        }
        deviceFirst.connectionState = STATE_CONNECTING;
        /* 调用device中的connectGatt连接到远程设备 */

        BluetoothGatt bluetoothGatt;
        bluetoothGatt = device.connectGatt(this, false, mGattCallback);
        //如果没有
        DeviceManager.connectionQueue.set(0, bluetoothGatt);
        Log.d("bletrack", "1 start connecting");
        //防内存泄漏？
        bluetoothGatt = null;

        return true;
    }

    // 连接远程蓝牙
    public boolean connectSecond(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w("bletrack",
                    "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        isNeedForScan = false;

        /* 获取远端的蓝牙设备 */
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w("bletrack", "Device not found.  Unable to connect.");
            return false;
        }
        deviceSecond.connectionState = STATE_CONNECTING;
        /* 调用device中的connectGatt连接到远程设备 */

        BluetoothGatt bluetoothGatt;
        bluetoothGatt = device.connectGatt(this, false, mGattCallback);
        //如果没有
        DeviceManager.connectionQueue.set(1, bluetoothGatt);
        //防内存泄漏？
        bluetoothGatt = null;
        Log.d("bletrack", "2 start connecting");
        return true;
    }

    // 连接远程蓝牙
    public boolean connectThird(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w("bletrack",
                    "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        isNeedForScan = false;

        /* 获取远端的蓝牙设备 */
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w("bletrack", "Device not found.  Unable to connect.");
            return false;
        }
        deviceThird.connectionState = STATE_CONNECTING;
        /* 调用device中的connectGatt连接到远程设备 */
        BluetoothGatt bluetoothGatt;
        bluetoothGatt = device.connectGatt(this, false, mGattCallback);
        //如果没有
        DeviceManager.connectionQueue.set(2, bluetoothGatt);
        //防内存泄漏？
        bluetoothGatt = null;
        Log.d("bletrack", "3 start connecting");
        return true;
    }
//    final Handler serHandler = new Handler();

    //Android出现找服务慢的情况
    public void discoverSafe(final BluetoothGatt gatt) {
        if (gatt.getDevice().getAddress().equals(deviceFirst.aimAddress)
                && !(deviceFirst.isReadyForNextFor)) {
            gatt.discoverServices();
            Log.d("bletrack", "1 refind service");
        } else if (gatt.getDevice().getAddress().equals(deviceSecond.aimAddress)
                && !deviceSecond.isReadyForNextFor) {
            gatt.discoverServices();
            Log.d("bletrack", "2 refind service");
        } else if (gatt.getDevice().getAddress().equals(deviceThird.aimAddress)
                && !deviceThird.isReadyForNextFor) {
            gatt.discoverServices();
            Log.d("bletrack", "3 refind service");
        }
    }

    /* 连接远程设备的回调函数 */
    private BluetoothGattCallback mGattCallback;
    private void gattCallBackInit(){
        mGattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                int newState) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    switch (newState) {
//                    如果连接状态正常
                        case BluetoothProfile.STATE_CONNECTED:
                            if (gatt.getDevice().getAddress().equals(deviceFirst.aimAddress)
                                    && deviceFirst.connectionState == STATE_CONNECTING) {
                                deviceFirst.isReadyForNextFor = false;           //得到特征值之后才能准备好
                                deviceFirst.connectionState = STATE_CONNECTED;
                                deviceFirst.isConnectPermit = true;
                                occupyState = false;
                                countForCircle=0;
                                Log.d("bletrack", "GATT 1 connected");
                            } else if (gatt.getDevice().getAddress().equals(deviceSecond.aimAddress)
                                    && deviceSecond.connectionState == STATE_CONNECTING) {
                                deviceSecond.isReadyForNextFor = false;           //得到特征值之后才能准备好
                                deviceSecond.connectionState = STATE_CONNECTED;
                                deviceSecond.isConnectPermit = true;
                                occupyState = false;
                                countForCircle=0;
                                Log.d("bletrack", "GATT 2 connected");
                            } else if (gatt.getDevice().getAddress().equals(deviceThird.aimAddress)
                                    && deviceThird.connectionState == STATE_CONNECTING) {
                                deviceThird.isReadyForNextFor = false;           //得到特征值之后才能准备好
                                deviceThird.connectionState = STATE_CONNECTED;
                                deviceThird.isConnectPermit = true;
                                occupyState = false;
                                countForCircle=0;
                                Log.d("bletrack", "GATT 3 connected");
                            }
                            break;
                        default:
                            Log.e("bletrack", "unkown newstate: " + String.valueOf(newState));
                        case BluetoothProfile.STATE_DISCONNECTED:               //断开连接
                            if (DeviceManager.checkGATT(gatt, 0)) {
                                deviceFirst.isReadyForNextFor = false;
                                occupyState = false;
                                deviceFirst.connectionState = STATE_DISCONNECTED;
                                Log.d("bletrack", "GATT 1 disconnected");
                                gatt.close();
                            } else if (DeviceManager.checkGATT(gatt, 1)) {
                                deviceSecond.connectionState = STATE_DISCONNECTED;
                                deviceSecond.isReadyForNextFor = false;
                                occupyState = false;
                                Log.d("bletrack", "GATT 2 disconnected");
                                gatt.close();
                            } else if (DeviceManager.checkGATT(gatt, 2)) {
                                deviceThird.connectionState = STATE_DISCONNECTED;
                                deviceThird.isReadyForNextFor = false;
                                occupyState = false;
                                Log.d("bletrack", "GATT 3 disconnected");
                                gatt.close();
                            }
                            break;
                    }
                } else {
                    if (DeviceManager.checkGATT(gatt, 0)) {
                        Log.e("bletrack", "1 unkown disconnected: " + String.valueOf(status));
                        deviceFirst.isReadyForNextFor = false;
                        occupyState = false;
                        deviceFirst.connectionState = STATE_DISCONNECTED;
                        gatt.disconnect();
                        gatt.close();
                    } else if (DeviceManager.checkGATT(gatt, 1)) {
                        Log.e("bletrack", "2 unkown disconnected: " + String.valueOf(status));
                        deviceSecond.connectionState = STATE_DISCONNECTED;
                        deviceSecond.isReadyForNextFor = false;
                        occupyState = false;
                        gatt.disconnect();
                        gatt.close();
                    } else if (DeviceManager.checkGATT(gatt, 2)) {
                        Log.e("bletrack", "3 unkown disconnected: " + String.valueOf(status));
                        deviceThird.connectionState = STATE_DISCONNECTED;
                        deviceThird.isReadyForNextFor = false;
                        occupyState = false;
                        gatt.disconnect();
                        gatt.close();
                    }
                }
            }

            //            当新服务被发现，进这个回调
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//                如果特征值和描述被更新
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (DeviceManager.checkGATT(gatt, 0)) {
                        if (deviceFirst.findService(DeviceManager.connectionQueue.get(0))) {
                            deviceFirst.isReadyForNextFor = true;
                            occupyState = false;
                            Log.d("bletrack", " gatt 1 service success");
                        } else {
                            Log.d("bletrack", "gatt 1 service fail");
                            deviceFirst.isReadyForNextFor = false;
                            occupyState = false;
                            deviceFirst.connectionState = STATE_DISCONNECTED;
                            gatt.disconnect();
                            gatt.close();
                        }
                    } else if (DeviceManager.checkGATT(gatt, 1)) {
                        if (deviceSecond.findService(DeviceManager.connectionQueue.get(1))) {
                            deviceSecond.isReadyForNextFor = true;
                            occupyState = false;
                            Log.d("bletrack", " gatt 2 service success");
                        } else {
                            Log.d("bletrack", "gatt 2 service fail");
                            deviceSecond.connectionState = STATE_DISCONNECTED;
                            deviceSecond.isReadyForNextFor = false;
                            gatt.disconnect();
                            occupyState = false;
                            gatt.close();
                        }
                    } else if (DeviceManager.checkGATT(gatt, 2)) {
                        if (deviceThird.findService(DeviceManager.connectionQueue.get(2))) {
                            deviceThird.isReadyForNextFor = true;
                            occupyState = false;
                            Log.d("bletrack", " gatt 3 service success");
                        } else {
                            Log.d("bletrack", "gatt 3 service fail");
                            deviceThird.connectionState = STATE_DISCONNECTED;
                            deviceThird.isReadyForNextFor = false;
                            gatt.disconnect();
                            occupyState = false;
                            gatt.close();
                        }
                    }
                } else {
                    if (DeviceManager.checkGATT(gatt, 0)) {
                        deviceFirst.isReadyForNextFor = false;
                        deviceFirst.connectionState = STATE_DISCONNECTED;
                        Log.d("bletrack", "ble gatt 1 service fail");
                    } else if (DeviceManager.checkGATT(gatt, 1)) {
                        deviceSecond.isReadyForNextFor = false;
                        deviceSecond.connectionState = STATE_DISCONNECTED;
                        Log.d("bletrack", "ble gatt 2 service fail");
                    } else if (DeviceManager.checkGATT(gatt, 2)) {
                        deviceThird.isReadyForNextFor = false;
                        deviceThird.connectionState = STATE_DISCONNECTED;
                        Log.d("bletrack", "ble gatt 3 service fail");
                    }
                }
            }

            /*
             * 特征值的读写
             *
             */
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt,
                                             BluetoothGattCharacteristic characteristic, int status) {
                Log.d("bletrack", "gatt read");
            }

            //是不是从机设置有问题，一是多少错误就断开，二是多长广播还是说，速度重连的要求
        /* *特征值的改* */
            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt,
                                                BluetoothGattCharacteristic characteristic) {
                String log_out;
                if (deviceFirst.characteristic6 != null)
                    if (DeviceManager.checkGATT(deviceFirst.characteristic6, characteristic)) {
                        deviceFirst.dataReceive = characteristic.getValue();
                        log_out = deviceFirst.getCharValue(characteristic);
                        Log.d("bletrack", "first notify: " + log_out);
                    }
                if (deviceFirst.characteristic7 != null)
                    if (DeviceManager.checkGATT(deviceFirst.characteristic7, characteristic)) {
                        deviceFirst.dataHeartBeats = characteristic.getValue();
                        deviceFirst.HBcount++;
                        log_out = deviceFirst.getCharValue(characteristic);
                        Log.d("ACHB", "first notify: " + log_out);
                        if (deviceFirst.dataReceive != null)
                            if (deviceFirst.dataReceive.length != bleDataLen) {
                                Log.e("bletrack", "notify first length is not equal to require");
                            }
                    }
                if (deviceSecond.characteristic6 != null)
                    if (DeviceManager.checkGATT(deviceSecond.characteristic6, characteristic)) {
                        deviceSecond.dataReceive = characteristic.getValue();
                        log_out = deviceSecond.getCharValue(characteristic);
                        Log.d("bletrack", "second notify: " + log_out);
                    }
                if (deviceSecond.characteristic7 != null)
                    if (DeviceManager.checkGATT(deviceSecond.characteristic7, characteristic)) {
                        deviceSecond.dataHeartBeats = characteristic.getValue();
                        deviceSecond.HBcount++;
                        log_out = deviceSecond.getCharValue(characteristic);
                        Log.d("ACHB", "second notify: " + log_out);
                        if (deviceSecond.dataReceive != null)
                            if (deviceSecond.dataReceive.length != bleDataLen) {
                                Log.e("bletrack", "notify second length is not equal to require");
                            }
                    }
                if (deviceThird.characteristic6 != null)
                    if (DeviceManager.checkGATT(deviceThird.characteristic6, characteristic)) {
                        deviceThird.dataReceive = characteristic.getValue();
                        log_out = deviceThird.getCharValue(characteristic);
                        Log.d("bletrack", "third notify: " + log_out);
                    }
                if (deviceThird.characteristic7 != null)
                    if (DeviceManager.checkGATT(deviceThird.characteristic7, characteristic)) {
                        deviceThird.dataHeartBeats = characteristic.getValue();
                        deviceThird.HBcount++;
                        log_out = deviceThird.getCharValue(characteristic);
                        Log.d("ACHB", "third notify: " + log_out);
                        if (deviceThird.dataReceive != null)
                            if (deviceThird.dataReceive.length != bleDataLen) {
                                Log.e("bletrack", "notify third length is not equal to require");
                            }
                    }
            }

            /*
             * 特征值的写
             * */
            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt,
                                              BluetoothGattCharacteristic characteristic,
                                              int status) {
                String log_out;
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (deviceFirst.characteristic6 != null)
                        if (DeviceManager.checkGATT(deviceFirst.characteristic6, characteristic)) {
                            log_out = deviceFirst.getCharValue(characteristic);
                            Log.d("bletrack", "first write: " + log_out);
                        }
                    if (deviceFirst.characteristic7 != null)
                        if (DeviceManager.checkGATT(deviceFirst.characteristic7, characteristic)) {
                            log_out = deviceFirst.getCharValue(characteristic);
                            Log.d("ACHB", "first write: " + log_out);
                            if (deviceFirst.dataReceive != null)
                                if (deviceFirst.dataReceive.length != bleDataLen) {
                                    Log.e("bletrack", "write first length is not equal to require");
                                }
                        }
                    if (deviceSecond.characteristic6 != null)
                        if (DeviceManager.checkGATT(deviceSecond.characteristic6, characteristic)) {
                            log_out = deviceSecond.getCharValue(characteristic);
                            Log.d("bletrack", "second write: " + log_out);
                        }
                    if (deviceSecond.characteristic7 != null)
                        if (DeviceManager.checkGATT(deviceSecond.characteristic7, characteristic)) {
                            log_out = deviceSecond.getCharValue(characteristic);
                            Log.d("ACHB", "second write: " + log_out);
                            if (deviceSecond.dataReceive != null)
                                if (deviceSecond.dataReceive.length != bleDataLen) {
                                    Log.e("bletrack", "write second length is not equal to require");
                                }
                        }
                    if (deviceThird.characteristic6 != null)
                        if (DeviceManager.checkGATT(deviceThird.characteristic6, characteristic)) {
                            log_out = deviceThird.getCharValue(characteristic);
                            Log.d("bletrack", "third write: " + log_out);
                        }
                    if (deviceThird.characteristic7 != null)
                        if (DeviceManager.checkGATT(deviceThird.characteristic7, characteristic)) {
                            log_out = deviceThird.getCharValue(characteristic);
                            Log.d("ACHB", "third write: " + log_out);
                            if (deviceThird.dataReceive != null)
                                if (deviceThird.dataReceive.length != bleDataLen) {
                                    Log.e("bletrack", "write third length is not equal to require");
                                }
                        }

                } else {
                    if (DeviceManager.checkGATT(gatt, 0)) {
                        Log.d("bletrack", "gatt first char write fail");
                    }
                    if (DeviceManager.checkGATT(gatt, 1)) {
                        Log.d("bletrack", "gatt second char write fail");
                    }
                    if (DeviceManager.checkGATT(gatt, 2)) {
                        Log.d("bletrack", "gatt third char write fail");
                    }
                    gatt.disconnect();

                }
            }

            /*读写蓝牙信号值 */
            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (DeviceManager.checkGATT(gatt, 0)) {
                        deviceFirst.RssiValue = rssi;
                    } else if (DeviceManager.checkGATT(gatt, 1)) {
                        deviceSecond.RssiValue = rssi;
                    } else if (DeviceManager.checkGATT(gatt, 2)) {
                        deviceThird.RssiValue = rssi;
                    }
                } else {
                    if (DeviceManager.checkGATT(gatt, 0)) {
                        Log.d("bletrack", "gatt first rssi fail");
                    } else if (DeviceManager.checkGATT(gatt, 1)) {
                        Log.d("bletrack", "gatt second rssi fail");
                    } else if (DeviceManager.checkGATT(gatt, 2)) {
                        Log.d("bletrack", "gatt third rssi fail");
                    }
                    gatt.disconnect();
                }
            }
        };
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.d("servicetrack", getClass().getSimpleName() + "onbind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        GattClose();
        Log.d("servicetrack", getClass().getSimpleName() + "onUnbind");
        return super.onUnbind(intent);
    }

    public void GattClose() {
        if (DeviceManager.checkGATT(0)) {
            DeviceManager.connectionQueue.get(0).close();
            DeviceManager.connectionQueue.remove(0);
            Log.w("bletrack", "gatt 1 close()");
        }

        if (DeviceManager.checkGATT(1)) {
            DeviceManager.connectionQueue.get(1).close();
            DeviceManager.connectionQueue.remove(1);
            Log.w("bletrack", "gatt 2 close()");
        }

        if (DeviceManager.checkGATT(2)) {
            DeviceManager.connectionQueue.get(2).close();
            DeviceManager.connectionQueue.remove(2);
            Log.w("bletrack", "gatt 3 close()");
        }
    }

    public void disconnect(int i) {
        if (mBluetoothAdapter == null || DeviceManager.connectionQueue.size() == 0) {
            Log.w("bletrack", "BluetoothAdapter not initialized");
            return;
        } else {
            switch (i) {
                case 0:
                    if (DeviceManager.checkGATT(0)) {
                        DeviceManager.connectionQueue.get(0).disconnect();
                        deviceFirst.isConnectPermit = true;
                        Log.w("bletrack", "gatt 1 discount()");
                    }

                    if (DeviceManager.checkGATT(1)) {
                        DeviceManager.connectionQueue.get(1).disconnect();
                        deviceSecond.isConnectPermit = true;
                        Log.w("bletrack", "gatt 2 discount()");
                    }
                    break;
                case 1:
                    if (DeviceManager.checkGATT(0)) {
                        DeviceManager.connectionQueue.get(0).disconnect();
                        deviceFirst.isConnectPermit = true;
                        Log.w("bletrack", "gatt 1 discount()");
                    }
                    break;
                case 2:
                    if (DeviceManager.checkGATT(1)) {
                        DeviceManager.connectionQueue.get(1).disconnect();
                        deviceSecond.isConnectPermit = true;
                        Log.w("bletrack", "gatt 2 discount()");
                    }
                    break;
                case 3:
                    if (DeviceManager.checkGATT(2)) {
                        DeviceManager.connectionQueue.get(2).disconnect();
                        deviceThird.isConnectPermit = true;
                        Log.w("bletrack", "gatt 3 discount()");
                    }
                    break;
            }
        }
    }

    private void notification() {
        //        通知窗口

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(android.R.drawable.btn_dialog);
        builder.setContentTitle("ActionCtrBle");
        builder.setContentText("为了保证Ble的长期不被系统干掉");
        Intent intent = new Intent(this, BeginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        startForeground(1, notification);
    }

    private void ble_init() {
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        gattCallBackInit();
        //scaner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    //    通过WiFi发数
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

    private int countForCircle = 1;
    private int occupyOrder = tryPara;
    private boolean wifsend = false;
    private int reSend = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        notification();
        try {
            ble_init();
            final Handler handler1 = new Handler();
            handler1.post(new Runnable() {
                @Override
                public void run() {
                    countForCircle++;
//                    执行循环连接和发现服务
//                    执行超时断开连接和发现服务，不能同时连接或者发现服务

                    if (tryField != 0)
                        occupyOrder = tryField;

                    if (countForCircle == 300) {
                        countForCircle = 0;
                        switch (occupyOrder) {
                            case 1:
                                if ((deviceFirst.connectionState == STATE_CONNECTING)
                                        || (deviceFirst.connectionState == STATE_CONNECTED
                                        && !deviceFirst.isReadyForNextFor)) {
                                    if (tryField != 0)
                                        occupyOrder = tryField;
                                    else
                                        occupyOrder = 2;
                                    occupyState = false;
//                                    第一个三秒如果连不上就会进else
                                    if (DeviceManager.checkGATT(0)) {
                                        DeviceManager.connectionQueue.get(0).disconnect();
                                        DeviceManager.connectionQueue.get(0).close();
                                        deviceFirst.connectionState = STATE_DISCONNECTED;
                                        Log.d("bletrack", "occupyOrder 1 remove");
                                    } else {
                                        Log.d("bletrack", "monitor 1 exception");
                                    }
                                }
                                break;
                            case 2:
                                if ((deviceSecond.connectionState == STATE_CONNECTING)
                                        || (deviceSecond.connectionState == STATE_CONNECTED
                                        && !deviceSecond.isReadyForNextFor)) {
                                    if (tryField != 0)
                                        occupyOrder = tryField;
                                    else
                                        occupyOrder = 3;
                                    occupyState = false;
                                    if (DeviceManager.checkGATT(1)) {
                                        DeviceManager.connectionQueue.get(1).disconnect();
                                        DeviceManager.connectionQueue.get(1).close();
                                        deviceSecond.connectionState = STATE_DISCONNECTED;
                                        Log.d("bletrack", "occupyOrder 2 remove");
                                    } else
                                        Log.d("bletrack", "monitor 2 exception");
                                }
                                break;
                            case 3:
                                if ((deviceThird.connectionState == STATE_CONNECTING)
                                        || (deviceThird.connectionState == STATE_CONNECTED
                                        && !deviceThird.isReadyForNextFor)) {
                                    if (tryField != 0)
                                        occupyOrder = tryField;
                                    else
                                        occupyOrder = 1;
                                    occupyState = false;
                                    if (DeviceManager.checkGATT(2)) {
                                        DeviceManager.connectionQueue.get(2).disconnect();
                                        DeviceManager.connectionQueue.get(2).close();
                                        deviceThird.connectionState = STATE_DISCONNECTED;
                                        Log.d("bletrack", "occupyOrder 3 remove");
                                    } else
                                        Log.d("bletrack", "monitor 3 exception");
                                }
                                break;
                        }
                    }
                    if (!occupyState) {
                        occupyState = true;
                        switch (occupyOrder) {
                            case 1:
                                if (deviceFirst.firstConnect) {
                                    deviceFirst.firstConnect = false;
                                    if (deviceFirst.connectionState == STATE_DISCONNECTED) {
                                        connectFirst(AIMADDRESS1);
                                    }
                                } else {
                                    if (deviceFirst.connectionState == STATE_DISCONNECTED) {
                                        deviceFirst.connectionState = STATE_CONNECTING;
                                        Log.d("bletrack", "connect 1 delay");
                                        handler1.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                connectFirst(AIMADDRESS1);
                                            }
                                        }, 2000);
                                    }
                                }
                                if (deviceFirst.connectionState == STATE_CONNECTED && !deviceFirst.isReadyForNextFor) {
                                    if (DeviceManager.checkGATT(0)) {
                                        DeviceManager.connectionQueue.get(0).discoverServices();
                                        Log.d("bletrack", "1 begins finding");
                                    } else
                                        Log.d("bletrack", "discover 1 exception");
                                } else if (deviceFirst.isReadyForNextFor) {
                                    occupyState = false;
                                    occupyOrder = 2;
                                    countForCircle = 0;
                                    //   Log.d("bletrack", "1 to 2");
                                }
                                break;
                            case 2:
                                if (deviceSecond.firstConnect) {
                                    deviceSecond.firstConnect = false;
                                    if (deviceSecond.connectionState == STATE_DISCONNECTED) {
                                        connectSecond(AIMADDRESS2);
                                    }
                                } else {
                                    if (deviceSecond.connectionState == STATE_DISCONNECTED) {
                                        deviceSecond.connectionState = STATE_CONNECTING;
                                        Log.d("bletrack", "connect 2 delay");
                                        handler1.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                connectSecond(AIMADDRESS2);
                                            }
                                        }, 2000);
                                    }
                                }
                                if (deviceSecond.connectionState == STATE_CONNECTED && !deviceSecond.isReadyForNextFor) {
                                    if (DeviceManager.checkGATT(1)) {
                                        DeviceManager.connectionQueue.get(1).discoverServices();
                                        Log.d("bletrack", "2 begins finding");
                                    } else
                                        Log.d("bletrack", "discover 2 exception");
                                } else if (deviceSecond.isReadyForNextFor) {
                                    occupyState = false;
                                    occupyOrder = 3;
                                    countForCircle = 0;
                                    //   Log.d("bletrack", "2 to 3");
                                }
                                break;
                            case 3:
                                if (deviceThird.firstConnect) {
                                    deviceThird.firstConnect = false;
                                    if (deviceThird.connectionState == STATE_DISCONNECTED) {
                                        connectThird(AIMADDRESS3);
                                    }
                                } else {
                                    if (deviceThird.connectionState == STATE_DISCONNECTED) {
                                        deviceThird.connectionState = STATE_CONNECTING;
                                        Log.d("bletrack", "connect 3 delay");
                                        handler1.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                connectThird(AIMADDRESS3);
                                            }
                                        }, 2000);
                                    }
                                }
                                if (deviceThird.connectionState == STATE_CONNECTED && !deviceThird.isReadyForNextFor) {
                                    if (DeviceManager.checkGATT(2)) {
                                        DeviceManager.connectionQueue.get(2).discoverServices();
                                        Log.d("bletrack", "3 begins finding");
                                    } else
                                        Log.d("bletrack", "discover 3 exception");
                                } else if (deviceThird.isReadyForNextFor) {
                                    occupyOrder = 1;
                                    occupyState = false;
                                    countForCircle = 0;
                                    // Log.d("bletrack", "3 to 1");
                                }
                                break;
                        }
                    }


                    if (deviceFirst.connectionState != STATE_CONNECTED
                            && deviceSecond.connectionState != STATE_CONNECTED
                            && deviceThird.connectionState != STATE_CONNECTED)
                        DeviceManager.connectTime++;
                    else
                        DeviceManager.connectTime = 0;
                    if (DeviceManager.connectTime > 250) {
                        handler.post(new ToastRunnable("蓝牙断开，建议你重启APP"));
                        DeviceManager.connectTime = 0;
                    }
                    if (countForCircle % 50 == 0) {
                        Log.d("constate", "1st " + deviceFirst.connectionState
                                + " 2nd " + deviceSecond.connectionState
                                + " 3rd " + deviceThird.connectionState);
                        Log.d("constate", "1r " + deviceFirst.isReadyForNextFor
                                + " 2r " + deviceSecond.isReadyForNextFor
                                + " 3r " + deviceThird.isReadyForNextFor);
                    }
//                    if(deviceFirst.isReadyForNextFor&&countForCircle % 50 == 0) {
//                        DeviceManager.connectionQueue.get(0).readRemoteRssi();
//                    }
                    Log.d("constate", "count " + countForCircle
                            + " order " + occupyOrder
                            + " state " + occupyState + " rssi " + deviceFirst.RssiValue);
                    handler1.postDelayed(this, 20);
                }
            });

            //下面的代码用于发送心跳包
            final Handler handlerHeartBeat = new Handler();
            Runnable runnable = new Runnable() {
                private int errCount1 = 0;
                private int lastHBcount1 = 0;
                private int errCount2 = 0;
                private int lastHBcount2 = 0;
                private int errCount3 = 0;
                private int lastHBcount3 = 0;

                @Override
                public void run() {
                    byte[] heartBeat = new byte[bleDataLen];
                    heartBeat[0] = 'A';
                    heartBeat[1] = 'C';
                    heartBeat[2] = 'H';
                    heartBeat[3] = 'B';

                    if (!wifsend)
                        wifiSend(heartBeat);

                    switch (checkOrder()) {
                        case 1:
                            if (deviceFirst.characteristic7 != null) {
                                deviceFirst.characteristic7.setValue(heartBeat);
                                if (!isDataSending) {
                                    DeviceManager.connectionQueue.get(0).writeCharacteristic(deviceFirst.characteristic7);
                                    isHBSending = true;
                                    Log.e("ACHB", "1 HeartBeats send");
                                    if (deviceFirst.HBcount == lastHBcount1 && (!isDataSending))
                                        errCount1++;
                                    else
                                        errCount1 = 0;
                                    lastHBcount1 = deviceFirst.HBcount;
                                }
                            }
                            if (errCount1 >= 5) {
                                Log.e("bletrack", "HeartBeats 1 disconnect");
                                disconnect(1);
                                errCount1 = 0;
                            }
                            break;
                        case 2:
                            if (deviceSecond.characteristic7 != null) {
                                deviceSecond.characteristic7.setValue(heartBeat);
                                if (!isDataSending) {
                                    DeviceManager.connectionQueue.get(1).writeCharacteristic(deviceSecond.characteristic7);
                                    isHBSending = true;
                                    Log.e("ACHB", "2 HeartBeats send");
                                    if (deviceSecond.HBcount == lastHBcount2 && (!isDataSending))
                                        errCount2++;
                                    else
                                        errCount2 = 0;
                                    lastHBcount2 = deviceSecond.HBcount;
                                }
                            }
                            if (errCount2 >= 5) {
                                Log.e("bletrack", "HeartBeats 2 disconnect");
                                disconnect(2);
                                errCount2 = 0;
                            }
                            break;

                        case 3:
                            if (deviceThird.characteristic7 != null) {
                                deviceThird.characteristic7.setValue(heartBeat);
                                if (!isDataSending) {
                                    DeviceManager.connectionQueue.get(2).writeCharacteristic(deviceThird.characteristic7);
                                    isHBSending = true;
                                    Log.e("ACHB", "3 HeartBeats send");
                                    if (deviceThird.HBcount == lastHBcount3 && (!isDataSending))
                                        errCount3++;
                                    else
                                        errCount3 = 0;
                                    lastHBcount3 = deviceThird.HBcount;
                                }
                            }
                            if (errCount3 >= 5) {
                                Log.e("bletrack", "HeartBeats 3 disconnect");
                                    disconnect(3);
                                errCount3 = 0;
                            }
                            break;
                    }
                    isHBSending = false;
                    handlerHeartBeat.postDelayed(this, 500);
                }
            };
//        不同于上面，上面是按键按一次就会执行一次，但是这个是只会在程序启动的时候执行
            handlerHeartBeat.postDelayed(runnable, 500);
        } catch (Exception e) {
            Log.e("bletrack", e.getMessage());
        }
        Log.d("servicetrack", getClass().getSimpleName() + "oncreate");
    }

    private int checkOrder() {
        if (!deviceFirst.isReadyForNextFor
                && !deviceSecond.isReadyForNextFor && !deviceThird.isReadyForNextFor)
            return 0;
        sendOrder++;
        if (sendOrder >= 4) sendOrder = 1;
        if (deviceFirst.isReadyForNextFor && sendOrder == 1)
            return 1;
        else if (!deviceFirst.isReadyForNextFor && sendOrder == 1)
            sendOrder++;
        if (deviceSecond.isReadyForNextFor && sendOrder == 2)
            return 2;
        else if (!deviceSecond.isReadyForNextFor && sendOrder == 2)
            sendOrder++;
        if (deviceThird.isReadyForNextFor && sendOrder == 3)
            return 3;
        else if (!deviceThird.isReadyForNextFor && sendOrder == 3) {
            sendOrder = 0;
            return checkOrder();
        }
        return 0;
    }

    public class myBleBand extends Binder {
        //        表明现在是第几条命令
        byte count = 0;
        //        前一次发数的检查是否完成
        boolean isBusy = false;
        //        定时检查前面发送有没有完成，如果没有则重发
        Handler handler = new Handler();

        public void send(byte[] data) {
//            检查数据长度是否正确
            if (data.length != bleDataLen) {
                Log.e("change", "length of senddata is not equal to require");
            }
            isDataSending = true;
//            放到缓存区里
            dataTrans = data;
//            把最后一个字节当做计数
            if (data[3] == 'T') {
                dataTrans[bleDataLen - 1] = count;
//            表明这边是第几个命令，防止重复
                if (count < 100)
                    count++;
                else
                    count = 0;
            } else if (data[3] == 'C') {
                dataTrans[bleDataLen - 1] = 0;
            }
//            如果GATT有定义
            if (deviceFirst.characteristic6 != null) {
//                设置值
                deviceFirst.characteristic6.setValue(dataTrans);
//                发送
                DeviceManager.connectionQueue.get(0).writeCharacteristic(deviceFirst.characteristic6);
            }
            if (deviceSecond.characteristic6 != null) {
//                设置值
                deviceSecond.characteristic6.setValue(dataTrans);
//                发送
                DeviceManager.connectionQueue.get(1).writeCharacteristic(deviceSecond.characteristic6);
            }
            if (deviceThird.characteristic6 != null) {
//                设置值
                deviceThird.characteristic6.setValue(dataTrans);
//                发送
                DeviceManager.connectionQueue.get(2).writeCharacteristic(deviceThird.characteristic6);
            }
            wifsend = true;
            wifiSend(dataTrans);
            wifsend = false;
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
//                  没有发送成功
                    isDataSending = true;
                    reSend ++;
                    if ((!checkSendOkFirst()) && (!checkSendOkSecond()) && (!checkSendOkThird())) {
                        isBusy = true;
                        if (deviceFirst.characteristic6 != null && deviceFirst.isReadyForNextFor) {
                            deviceFirst.characteristic6.setValue(dataTrans);
                            if (!isHBSending) {
                                DeviceManager.connectionQueue.get(0).writeCharacteristic(deviceFirst.characteristic6);
                                Log.d("datasend", "write characteristic");
                            }
                        }
                        if (deviceSecond.characteristic6 != null && deviceSecond.isReadyForNextFor) {
                            deviceSecond.characteristic6.setValue(dataTrans);
                            if (!isHBSending) {
                                DeviceManager.connectionQueue.get(1).writeCharacteristic(deviceSecond.characteristic6);
                                Log.d("datasend", "write characteristic");
                            }
                        }
                        if (deviceThird.characteristic6 != null && deviceThird.isReadyForNextFor) {
                            deviceThird.characteristic6.setValue(dataTrans);
                            if (!isHBSending) {
                                DeviceManager.connectionQueue.get(2).writeCharacteristic(deviceThird.characteristic6);
                                Log.d("datasend", "write characteristic");
                            }
                        }
//                        100ms之后执行runnable
                        if ((deviceFirst.isReadyForNextFor || deviceSecond.isReadyForNextFor || deviceThird.isReadyForNextFor)) {
                            isDataSending = false;
                            handler.postDelayed(this, 500);
                        }
                    }
//                    发送成功
                    else {
                        reSend = 0;
                        handler.removeCallbacks(this);
                        isBusy = false;
                        isDataSending = false;
                    }
                }
            };
//           第一次进来的时候不会执行run，只有postDelayed触发时才会
//·          第二次的时候发现第一次还是没有发送成功就不再运行一遍
            if (!isBusy)
                handler.postDelayed(runnable, 500);
            isDataSending = false;
        }

        //        获取心跳包数据
        public byte[] getHeartBeats() {
            if (deviceFirst.isReadyForNextFor)
                return deviceFirst.dataHeartBeats;
            if (deviceSecond.isReadyForNextFor)
                return deviceSecond.dataHeartBeats;
            if (deviceThird.isReadyForNextFor)
                return deviceThird.dataHeartBeats;
            return deviceFirst.dataHeartBeats;
        }


        public int getConnectNum() {
            int conNum = 0;
            if (deviceFirst != null)
                if (deviceFirst.isReadyForNextFor == true)
                    conNum++;
            if (deviceSecond != null)
                if (deviceSecond.isReadyForNextFor == true)
                    conNum++;
            if (deviceThird != null)
                if (deviceThird.isReadyForNextFor == true)
                    conNum++;
            return conNum;
        }

        public boolean checkSendOkFirst() {
            if (reSend==2)
                return true;
//            如果二者相等则返回true
            if (Arrays.equals(deviceFirst.dataReceive, dataTrans)) {
                return true;
            }
//            判断数据是否匹配
            if (deviceFirst.dataReceive != null && dataTrans != null) {
                int i;
//                如果i==9时不相等，执行break,此时i不++，i依然不满足=10条件
                for (i = 0; i < 10; i++) {
                    if (deviceFirst.dataReceive[i] != dataTrans[i])
                        break;
                }
                if (i == 10) {
                    Log.e("bletrack", "1 communicate unstable");
                    return true;
                }
            }
            if (isDataSending == false)
                return true;
            return false;
        }

        public boolean checkSendOkSecond() {
            if (reSend==2)
                return true;
//            如果二者相等则返回true
            if (Arrays.equals(deviceSecond.dataReceive, dataTrans)) {
                return true;
            }
//            判断数据是否匹配
            if (deviceSecond.dataReceive != null && dataTrans != null) {
                int i;
//                如果i==9时不相等，执行break,此时i不++，i依然不满足=10条件
                for (i = 0; i < 10; i++) {
                    if (deviceSecond.dataReceive[i] != dataTrans[i])
                        break;
                }
                if (i == 10) {
                    Log.e("bletrack", "2 communicate unstable");
                    return true;
                }
            }
            return false;
        }

        public boolean checkSendOkThird() {
            if (reSend==2)
                return true;
//            如果二者相等则返回true
            if (Arrays.equals(deviceThird.dataReceive, dataTrans)) {
                return true;
            }
//            判断数据是否匹配
            if (deviceThird.dataReceive != null && dataTrans != null) {
                int i;
//                如果i==9时不相等，执行break,此时i不++，i依然不满足=10条件
                for (i = 0; i < 10; i++) {
                    if (deviceThird.dataReceive[i] != dataTrans[i])
                        break;
                }
                if (i == 10) {
                    Log.e("bletrack", "3 communicate unstable");
                    return true;
                }
            }
            return false;
        }

        public void setIsSending() {
            isDataSending = false;
        }

        public void setReSending() {
            reSend = 0;
        }
        //        连接是否完成
        public boolean isReadyFirst() {
            return deviceFirst.isReadyForNextFor;
        }

        public boolean isReadySecond() {
            return deviceSecond.isReadyForNextFor;
        }

        public boolean isReadyThird() {
            return deviceThird.isReadyForNextFor;
        }

        //        读取蓝牙强度
        public int readRssiFirst() {
            if (deviceFirst.isReadyForNextFor) {
                DeviceManager.connectionQueue.get(0).readRemoteRssi();
                return deviceFirst.RssiValue;
            } else {
                return 0;
            }
        }

        public int readRssiSecond() {
            if (deviceSecond.isReadyForNextFor) {
                DeviceManager.connectionQueue.get(1).readRemoteRssi();
                return deviceSecond.RssiValue;
            } else {
                return 0;
            }
        }

        public int readRssiThird() {
            if (deviceThird.isReadyForNextFor) {
                DeviceManager.connectionQueue.get(2).readRemoteRssi();
                return deviceThird.RssiValue;
            } else {
                return 0;
            }
        }
    }


    private class ToastRunnable implements Runnable {
        String mText;

        public ToastRunnable(String text) {
            mText = text;
        }

        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), mText, Toast.LENGTH_SHORT).show();
        }
    }

    //    关闭蓝牙再测
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent.getStringExtra("mode").equals("tryField")) {
            Log.d("bletrack", "tryField mode");
            tryField = tryPara;
        } else if (intent.getStringExtra("mode").equals("compete")) {
            Log.d("bletrack", "dual mode");
            tryField = 0;
        }
        return flags;
    }

    @Override
    public void onDestroy() {
        Log.d("servicetrack", "Ble Service onDestroy");
        super.onDestroy();
        disconnect(0);
        GattClose();
    }

}
