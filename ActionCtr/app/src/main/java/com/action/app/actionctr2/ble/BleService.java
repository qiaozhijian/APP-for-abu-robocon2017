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

import com.action.app.actionctr2.BeginActivity;
import com.action.app.actionctr2.sqlite.SharedPreferencesHelper;
import com.action.app.actionctr2.wifi.wifiService;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

//连接重连，断开重连，发现服务过长
public class BleService extends Service {

    private final IBinder mBinder = new myBleBand();

    //有一个默认第一设备，第二设备，如果扫描就是哪个先进哪个是，如果直接连接就用默认的
    //private final String AIMADDRESS1 = "F4:5E:AB:B9:58:80";//1号 白色平板
    // private final String AIMADDRESS1="50:65:83:86:C6:33";//这个参数是车上用的平板 2号
    //private final static String AIMADDRESS1 = "98:7B:F3:60:C7:1C";//测试版
    private final String AIMADDRESS1 = "F4:5E:AB:B9:5A:03";// //手机
    private final String AIMADDRESS2 = "F4:5E:AB:B9:59:77";//手机
    // private final String AIMADDRESS1="98:7B:F3:60:C7:01";//
    //private final String AIMADDRESS1 = "90:59:AF:0E:60:1F";//

    private deviceManager deviceFirst = new deviceManager(AIMADDRESS1);
    private deviceManager deviceSecond = new deviceManager(AIMADDRESS1);
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
    private static final String STATE_IDLE = "STATE_IDLE";
    private static final String STATE_CONNECTION_READY = "STATE_CONNECTION_READY";
    private static final String STATE_CONNECTION_FAIL_SCAN = "STATE_CONNECTION_FAIL_SCAN";
    private static final String STATE_CONNECTION_FAIL_SCAN_RIGHT = "STATE_CONNECTION_FAIL_SCAN_RIGHT";
    private boolean isNeedForScan = false;
    private String returnState = STATE_IDLE;


    // 描述扫描蓝牙的状态
    private boolean mScanning;

    private SharedPreferencesHelper sharedPreferencesHelper;

    /**
     * @param enable (扫描使能，true:扫描开始,false:扫描停止)
     * @return void
     * @throws
     * @Title: scanLeDevice
     * @Description: TODO(扫描蓝牙设备)
     *///不能循环扫描，会卡死
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            /* 开始扫描蓝牙设备，带mLeScanCallback 回调函数 */
            Log.i("bletrack", "begin scanning");
            mScanning = true;
            //在扫描前，最好先调用一次停止扫描
            //scaner.stopScan(mScanCallback);   // 这时会引用空对象
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
                    returnState = STATE_CONNECTION_FAIL_SCAN;
                    sharedPreferencesHelper.putString("returnState", returnState);
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
                    returnState = STATE_CONNECTION_FAIL_SCAN_RIGHT;
                    sharedPreferencesHelper.putString("returnState", returnState);
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

    final Handler conHandler = new Handler();

    //Android十秒连一次
    public void connectSafe(final String address) {
        Runnable conRunnable = new Runnable() {
            String conAddress = address;

            @Override
            public void run() {
                if (conAddress.equals(deviceFirst.aimAddress) && deviceFirst.connectionState != STATE_CONNECTED) {
                    //创建deviceManager.connectionQueue.get(0)时会进入mGattCallback
                    deviceFirst.isConnectPermit = false;
                    Log.d("bletrack", "1 connectsafe");
                    connectFirst(conAddress);
                    if (deviceFirst.checkConRun(this)) {
                        deviceFirst.runnables.add(this);
                    }
                    conHandler.postDelayed(this, 10000);
                } else if (conAddress.equals(deviceSecond.aimAddress) && deviceSecond.connectionState != STATE_CONNECTED) {
                    connectSecond(conAddress);
                    deviceSecond.isConnectPermit = false;
                    Log.d("bletrack", "2 connectsafe");
                    if (deviceSecond.checkConRun(this)) {
                        deviceSecond.runnables.add(this);
                    }
                    conHandler.postDelayed(this, 10000);
                } else {
                    conHandler.removeCallbacks(this);
                }
            }
        };
        if (address.equals(deviceFirst.aimAddress)) {
            removeALLConRun(deviceFirst.runnables);
        } else if (address.equals(deviceSecond.aimAddress)) {
            removeALLSerRun(deviceSecond.runnables);
        }
        conHandler.post(conRunnable);
    }

    // 连接远程蓝牙
    public boolean connectFirst(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w("bletrack",
                    "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        isNeedForScan = false;

        if (deviceManager.checkGATT(0)) {
            if (deviceManager.connectionQueue.get(0).getDevice().getAddress().equals(address)) {
                if (deviceManager.connectionQueue.get(0).connect()) {
                    deviceFirst.connectionState = STATE_CONNECTING;
                    Log.d("bletrack", "GATT1 connect()");
                    return true;
                } else {
                    Log.d("bletrack", "GATT1 connect()   fail");
                    return false;
                }
            }
        }
        Log.d("bletrack", "lala3");
        /* 获取远端的蓝牙设备 */
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        Log.d("bletrack", "lala4");
        if (device == null) {
            Log.w("bletrack", "Device not found.  Unable to connect.");
            return false;
        }
        Log.d("bletrack", "lala5");
        deviceFirst.connectionState = STATE_CONNECTING;
        // We want to directly connect to the device, so we are setting the
        // autoConnect
        // parameter to false.
        /* 调用device中的connectGatt连接到远程设备 */

        Log.d("bletrack", "lala6");
        BluetoothGatt bluetoothGatt;
        bluetoothGatt = device.connectGatt(this, false, mGattCallback);
        //如果没有
        Log.d("bletrack", "lala7");
        if (deviceManager.checkGATT(bluetoothGatt)) {
            deviceManager.connectionQueue.add(bluetoothGatt);
            Log.d("bletrack", "add new GATT");
        }
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

        if (deviceManager.checkGATT(1)) {
            Log.d("bletrack", "2lala2");
            if (deviceManager.connectionQueue.get(1).getDevice().getAddress().equals(address)) {
                if (deviceManager.connectionQueue.get(1).connect()) {
                    deviceSecond.connectionState = STATE_CONNECTING;
                    Log.d("bletrack", "GATT2 connect()");
                    return true;
                } else {
                    Log.d("bletrack", "GATT2 connect()   fail");
                    return false;
                }
            }
        }
        Log.d("bletrack", "2lala3");
        /* 获取远端的蓝牙设备 */
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        Log.d("bletrack", "2lala4");
        if (device == null) {
            Log.w("bletrack", "Device not found.  Unable to connect.");
            return false;
        }
        Log.d("bletrack", "2lala5");
        deviceSecond.connectionState = STATE_CONNECTING;
        // We want to directly connect to the device, so we are setting the
        // autoConnect
        // parameter to false.
        /* 调用device中的connectGatt连接到远程设备 */

        Log.d("bletrack", "2lala6");
        BluetoothGatt bluetoothGatt;
        bluetoothGatt = device.connectGatt(this, false, mGattCallback);
        //如果没有
        Log.d("bletrack", "2lala7");
        if (deviceManager.checkGATT(bluetoothGatt)) {
            deviceManager.connectionQueue.add(bluetoothGatt);
            Log.d("bletrack", "2add new GATT");
        }
        return true;
    }

    final Handler serHandler = new Handler();

    //Android出现找服务慢的情况
    public void discoverSafe(final BluetoothGatt gatt) {
        Runnable conRunnable = new Runnable() {
            BluetoothGatt bluetoothGatt = gatt;

            @Override
            public void run() {
                if (bluetoothGatt.getDevice().getAddress().equals(deviceFirst.aimAddress)
                        && !(deviceFirst.isReadyForNextFor)
                        && deviceFirst.connectionState == STATE_CONNECTED) {
                    if (deviceFirst.checkSerRun(this))
                        deviceFirst.serRunnables.add(this);
                    deviceFirst.findService++;
                    if (deviceFirst.findService > 2)
                        disconnect(1);
                    else
                        bluetoothGatt.discoverServices();
                    Log.d("bletrack", "1 refind service");
                    serHandler.postDelayed(this, 5000);
                } else if (bluetoothGatt.getDevice().getAddress().equals(deviceSecond.aimAddress)
                        && !deviceSecond.isReadyForNextFor
                        && deviceSecond.connectionState == STATE_CONNECTED) {
                    if (deviceSecond.checkSerRun(this))
                        deviceSecond.serRunnables.add(this);
                    deviceSecond.findService++;
                    if (deviceSecond.findService > 2)
                        disconnect(2);
                    else
                        bluetoothGatt.discoverServices();
                    Log.d("bletrack", "2 refind service");
                    serHandler.postDelayed(this, 5000);
                } else
                    serHandler.removeCallbacks(this);
            }
        };
        if (gatt.getDevice().getAddress().equals(deviceFirst.aimAddress)) {
            removeALLSerRun(deviceFirst.serRunnables);
        } else if (gatt.getDevice().getAddress().equals(deviceSecond.aimAddress)) {
            removeALLSerRun(deviceFirst.serRunnables);
        }
        conHandler.post(conRunnable);
    }

    final Handler handler = new Handler();
    /* 连接远程设备的回调函数 */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                switch (newState) {
//                    如果连接状态正常
                    case BluetoothProfile.STATE_CONNECTED:
                        if (gatt.getDevice().getAddress().equals(deviceFirst.aimAddress)) {
                            removeALLConRun(deviceFirst.runnables);
                            deviceFirst.isReadyForNextFor = false;           //得到特征值之后才能准备好
                            if (deviceFirst.connectionState == STATE_CONNECTING)
                                discoverSafe(gatt);             //去发现服务
                            deviceFirst.connectionState = STATE_CONNECTED;
                            deviceFirst.isConnectPermit = true;
                            Log.d("bletrack", "GATT 1 connected");
                            if (deviceSecond.connectionState == STATE_DISCONNECTED) {
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (deviceSecond.connectionState == STATE_DISCONNECTED) {
                                            connectSafe(deviceSecond.aimAddress);
                                            Log.d("bletrack", "GATT 2 reconnect by 1");
                                        } else
                                            handler.removeCallbacks(this);
                                    }
                                }, 3000);
                            }

                        } else if (gatt.getDevice().getAddress().equals(deviceSecond.aimAddress)) {
                            removeALLConRun(deviceSecond.runnables);
                            deviceSecond.isReadyForNextFor = false;           //得到特征值之后才能准备好
                            if (deviceSecond.connectionState == STATE_CONNECTING)
                                discoverSafe(gatt);
                            deviceSecond.connectionState = STATE_CONNECTED;
                            deviceSecond.isConnectPermit = true;
                            Log.d("bletrack", "GATT 2 connected");
                            if (deviceFirst.connectionState == STATE_DISCONNECTED) {
                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (deviceFirst.connectionState == STATE_DISCONNECTED) {
                                            connectSafe(deviceFirst.aimAddress);
                                            Log.d("bletrack", "GATT 1 reconnect by 2");
                                        } else
                                            handler.removeCallbacks(this);
                                    }
                                }, 3000);
                            }
                        }
                        break;
                    default:
                        Log.e("bletrack", "unkown newstate: " + String.valueOf(newState));
                    case BluetoothProfile.STATE_DISCONNECTED:               //断开连接
//                    加上会变快
                        if (deviceManager.checkGATT(gatt, 0)) {
                            Log.d("bletrack", "gatt 1 disconnect");
                            deviceFirst.isReadyForNextFor = false;
                            deviceFirst.connectionState = STATE_DISCONNECTED;
                        } else if (deviceManager.checkGATT(gatt, 1)) {
                            Log.d("bletrack", "gatt 2 disconnect");
                            deviceSecond.connectionState = STATE_DISCONNECTED;
                            deviceSecond.isReadyForNextFor = false;
                        }
                        //秒连速度之快已经让车上蓝牙检测不到断开了
                        if (deviceFirst.isConnectPermit)
                            if (deviceFirst.connectionState == STATE_DISCONNECTED) {
                                Log.d("bletrack", "gatt 1 reconnect");
                                connectSafe(AIMADDRESS1);
                            }
                        if (deviceSecond.isConnectPermit)
                            if (deviceSecond.connectionState == STATE_DISCONNECTED) {
                                Log.d("bletrack", "gatt 2 reconnect");
                                connectSafe(AIMADDRESS2);
                            }
                        break;
                }
            } else {
                Log.e("bletrack", "unkown disconnected: " + String.valueOf(status));
                gatt.close();
                scanLeDevice(true);
            }
        }

        //            当新服务被发现，进这个回调
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//                如果特征值和描述被更新
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (deviceManager.checkGATT(gatt, 0)) {
                    removeALLSerRun(deviceFirst.serRunnables);
                    deviceFirst.isReadyForNextFor = true;
                    deviceFirst.findService = 0;
                    deviceFirst.findService(deviceManager.connectionQueue.get(0));
                    Log.d("bletrack", " gatt 1 service success");
                } else if (deviceManager.checkGATT(gatt, 1)) {
                    removeALLSerRun(deviceSecond.serRunnables);
                    deviceSecond.isReadyForNextFor = true;
                    deviceSecond.findService = 0;
                    deviceSecond.findService(deviceManager.connectionQueue.get(1));
                    Log.d("bletrack", " gatt 2 service success");
                }
            } else {
                if (deviceManager.checkGATT(gatt, 0)) {
                    deviceFirst.isReadyForNextFor = false;
                    Log.d("bletrack", "ble gatt1 service fail");
                } else if (deviceManager.checkGATT(gatt, 1)) {
                    deviceSecond.isReadyForNextFor = false;
                    Log.d("bletrack", "ble gatt2 service fail");
                }
                gatt.disconnect();
            }
        }

        /*
         * 特征值的读写
         *
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            if (status == gatt.GATT_SUCCESS) {
                if (deviceManager.checkGATT(gatt, 0)) {
                    String log_out = new String();
                    for (int i = 0; i < bleDataLen; i++) {
//                    为了log的时候好观察，加了个\t
                        log_out += String.valueOf(characteristic.getValue()[i]) + '\t';
                    }
                    Log.d("bletrack", "gatt 0 read value: " + log_out);
                } else if (deviceManager.checkGATT(gatt, 1)) {
                    String log_out = new String();
                    for (int i = 0; i < bleDataLen; i++) {
//                    为了log的时候好观察，加了个\t
                        log_out += String.valueOf(characteristic.getValue()[i]) + '\t';
                    }
                    Log.d("bletrack", "gatt 1 read value: " + log_out);
                }
            } else {
                if (deviceManager.checkGATT(gatt, 0)) {
                    Log.d("bletrack", "ble gatt1 service fail");
                } else if (deviceManager.checkGATT(gatt, 1)) {
                    Log.d("bletrack", "ble gatt2 service fail");
                }
                gatt.disconnect();

            }
        }

        //是不是从机设置有问题，一是多少错误就断开，二是多长广播还是说，速度重连的要求
        /* *特征值的改* */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            String log_out = new String();

            if (deviceManager.checkGATT(deviceFirst.characteristic6, characteristic)) {
                deviceFirst.dataReceive = characteristic.getValue();
                log_out = deviceFirst.getCharValue(characteristic);
                Log.d("bletrack", "first notify: " + log_out);
            } else if (deviceManager.checkGATT(deviceFirst.characteristic7, characteristic)) {
                deviceFirst.dataHeartBeats = characteristic.getValue();
                deviceFirst.HBcount++;
                log_out = deviceFirst.getCharValue(characteristic);
                Log.d("ACHB", "first notify: " + log_out);
                if (deviceFirst.dataReceive != null)
                    if (deviceFirst.dataReceive.length != bleDataLen) {
                        Log.e("bletrack", "notify first length is not equal to require");
                    }
            } else if (deviceManager.checkGATT(deviceSecond.characteristic6, characteristic)) {
                deviceSecond.dataReceive = characteristic.getValue();
                log_out = deviceSecond.getCharValue(characteristic);
                Log.d("bletrack", "second notify: " + log_out);
            } else if (deviceManager.checkGATT(deviceSecond.characteristic7, characteristic)) {
                deviceSecond.dataHeartBeats = characteristic.getValue();
                deviceSecond.HBcount++;
                log_out = deviceSecond.getCharValue(characteristic);
                Log.d("ACHB", "second notify: " + log_out);
                if (deviceSecond.dataReceive != null)
                    if (deviceSecond.dataReceive.length != bleDataLen) {
                        Log.e("bletrack", "notify second length is not equal to require");
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
            String log_out = new String();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (deviceManager.checkGATT(deviceFirst.characteristic6, characteristic)) {
                    log_out = deviceFirst.getCharValue(characteristic);
                    Log.d("bletrack", "first write: " + log_out);
                } else if (deviceManager.checkGATT(deviceFirst.characteristic7, characteristic)) {
                    log_out = deviceFirst.getCharValue(characteristic);
                    Log.d("ACHB", "first write: " + log_out);
                    if (deviceFirst.dataReceive != null)
                        if (deviceFirst.dataReceive.length != bleDataLen) {
                            Log.e("bletrack", "write first length is not equal to require");
                        }
                } else if (deviceManager.checkGATT(deviceSecond.characteristic6, characteristic)) {
                    log_out = deviceSecond.getCharValue(characteristic);
                    Log.d("bletrack", "second write: " + log_out);
                } else if (deviceManager.checkGATT(deviceSecond.characteristic7, characteristic)) {
                    log_out = deviceSecond.getCharValue(characteristic);
                    Log.d("ACHB", "second write: " + log_out);
                    if (deviceSecond.dataReceive != null)
                        if (deviceSecond.dataReceive.length != bleDataLen) {
                            Log.e("bletrack", "write second length is not equal to require");
                        }
                }

            } else {
                if (deviceManager.checkGATT(gatt, 0)) {
                    Log.d("bletrack", "gatt first char write fail");
                }
                if (deviceManager.checkGATT(gatt, 1)) {
                    Log.d("bletrack", "gatt second char write fail");
                }
                gatt.disconnect();

            }
        }

        /*读写蓝牙信号值 */
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (deviceManager.checkGATT(gatt, 0)) {
                    deviceFirst.RssiValue = rssi;
                } else if (deviceManager.checkGATT(gatt, 1)) {
                    deviceSecond.RssiValue = rssi;
                }
            } else {
                if (deviceManager.checkGATT(gatt, 0)) {
                    Log.d("bletrack", "gatt first rssi fail");
                } else if (deviceManager.checkGATT(gatt, 1)) {
                    Log.d("bletrack", "gatt second rssi fail");
                }
                gatt.disconnect();
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("servicetrack", getClass().getSimpleName() + "onbind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        GattClose(0);
        Log.d("servicetrack", getClass().getSimpleName() + "onUnbind");
        return super.onUnbind(intent);
    }

    public void GattClose(int i) {
        switch (i) {
            case 0:
                if (deviceManager.checkGATT(0)) {
                    deviceManager.connectionQueue.get(0).close();
                    deviceManager.connectionQueue.remove(0);
                    Log.w("bletrack", "gatt 1 close()");
                }

                if (deviceManager.checkGATT(1)) {
                    deviceManager.connectionQueue.get(1).close();
                    deviceManager.connectionQueue.remove(1);
                    Log.w("bletrack", "gatt 2 close()");
                }
                break;
            case 1:
                if (deviceManager.checkGATT(0)) {
                    deviceManager.connectionQueue.get(0).close();
                    deviceManager.connectionQueue.remove(0);
                    Log.w("bletrack", "gatt 1 close()");
                }
                break;
            case 2:
                if (deviceManager.checkGATT(1)) {
                    deviceManager.connectionQueue.get(1).close();
                    deviceManager.connectionQueue.remove(1);
                    Log.w("bletrack", "gatt 2 close()");
                }
                break;
        }
    }

    public void disconnect(int i) {
        if (mBluetoothAdapter == null || deviceManager.connectionQueue.size() == 0) {
            Log.w("bletrack", "BluetoothAdapter not initialized");
            return;
        } else {
            switch (i) {
                case 0:
                    if (deviceManager.checkGATT(0)) {
                        deviceManager.connectionQueue.get(0).disconnect();
                        deviceFirst.isConnectPermit = true;
                        Log.w("bletrack", "gatt 1 discount()");
                    }

                    if (deviceManager.checkGATT(1)) {
                        deviceManager.connectionQueue.get(1).disconnect();
                        deviceSecond.isConnectPermit = true;
                        Log.w("bletrack", "gatt 2 discount()");
                    }

                    break;
                case 1:
                    if (deviceManager.checkGATT(0)) {
                        deviceManager.connectionQueue.get(0).disconnect();
                        deviceFirst.isConnectPermit = true;
                        Log.w("bletrack", "gatt 1 discount()");
                    }

                    break;
                case 2:
                    if (deviceManager.checkGATT(1)) {
                        deviceManager.connectionQueue.get(1).disconnect();
                        deviceSecond.isConnectPermit = true;
                        Log.w("bletrack", "gatt 2 discount()");
                    }

                    break;
            }
        }
    }

    private void removeALLConRun(ArrayList<Runnable> runnables) {
        if (!runnables.isEmpty()) {
            for (Runnable run : runnables) {
                conHandler.removeCallbacks(run);
            }
        }
    }

    private void removeALLSerRun(ArrayList<Runnable> runnables) {
        if (!runnables.isEmpty()) {
            for (Runnable run : runnables) {
                serHandler.removeCallbacks(run);
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
        scaner = mBluetoothAdapter.getBluetoothLeScanner();
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

    @Override
    public void onCreate() {
        super.onCreate();

        notification();
        sharedPreferencesHelper = new SharedPreferencesHelper(this, "data");
        try {
            ble_init();

            //scanLeDevice(true);

            connectSafe(deviceFirst.aimAddress);

             connectSafe(deviceSecond.aimAddress);

            final Handler handler1 = new Handler();
            handler1.post(new Runnable() {
                @Override
                public void run() {
                    Log.d("constate", "1  " + String.valueOf(deviceFirst.connectionState));
                    Log.d("constate", "2  " + String.valueOf(deviceSecond.connectionState));
                    handler1.postDelayed(this, 1000);
                }
            });


            //下面的代码用于发送心跳包
            final Handler handlerHeartBeat = new Handler();
            Runnable runnable = new Runnable() {
                private int errCount1 = 0;
                private int lastHBcount1 = 0;
                private int errCount2 = 0;
                private int lastHBcount2 = 0;

                @Override
                public void run() {
                    byte[] heartBeat = new byte[bleDataLen];
                    heartBeat[0] = 'A';
                    heartBeat[1] = 'C';
                    heartBeat[2] = 'H';
                    heartBeat[3] = 'B';
                    if (!isDataSending) {
                        wifiSend(heartBeat);
                        //Log.d("wifitrack", "wifisend");
                    }
                    if (deviceManager.connectionQueue.size() > 0) {
                        if (deviceFirst.isReadyForNextFor) {
                            if (deviceFirst.characteristic7 != null) {
                                deviceFirst.characteristic7.setValue(heartBeat);
                                if (!isDataSending) {
                                    deviceManager.connectionQueue.get(0).writeCharacteristic(deviceFirst.characteristic7);
                                    isHBSending = true;
                                    Log.e("ACHB", "HeartBeats send");
                                }
                            }
                            if (deviceFirst.HBcount == lastHBcount1 && (!isDataSending))
                                errCount1++;
                            else
                                errCount1 = 0;
                            lastHBcount1 = deviceFirst.HBcount;
                            if (errCount1 >= 7) {
                                Log.e("bletrack", "HeartBeats 1 disconnect");
                                deviceFirst.isReadyForNextFor = false;
                                disconnect(1);
                                errCount1 = 0;
                            }
                        } else {
                            errCount1 = 0;
                        }
                        if (deviceSecond.isReadyForNextFor) {
                            if (deviceSecond.characteristic7 != null) {
                                deviceSecond.characteristic7.setValue(heartBeat);
                                if (!isDataSending) {
                                    deviceManager.connectionQueue.get(1).writeCharacteristic(deviceSecond.characteristic7);
                                    isHBSending = true;
                                    Log.e("ACHB", "HeartBeats send");
                                }
                            }
                            if (deviceSecond.HBcount == lastHBcount2 && (!isDataSending))
                                errCount2++;
                            else
                                errCount2 = 0;
                            lastHBcount2 = deviceSecond.HBcount;
                            if (errCount2 >= 8) {
                                Log.e("bletrack", "HeartBeats 2 disconnect");
                                deviceSecond.isReadyForNextFor = false;
                                disconnect(2);
                                errCount2 = 0;
                            }
                        } else {
                            errCount2 = 0;
                        }
                    }

                    isHBSending = false;
                    handlerHeartBeat.postDelayed(this, 600);
                }
            };
//        不同于上面，上面是按键按一次就会执行一次，但是这个是只会在程序启动的时候执行
            handlerHeartBeat.postDelayed(runnable, 500);
        } catch (Exception e) {
            Log.e("bletrack", e.getMessage());
        }
        Log.d("servicetrack", getClass().getSimpleName() + "oncreate");
    }

    //    蓝牙发数  binder跟所有涉及到蓝牙的activity通信
    private myBleBand dataSend = new myBleBand();


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
//            放到缓存区里
            dataTrans = data;
//            把最后一个字节当做计数
            dataTrans[bleDataLen - 1] = count;
//            表明这边是第几个命令，防止重复
            if (count < 100)
                count++;
            else
                count = 0;
//            如果GATT有定义
            if (deviceFirst.characteristic6 != null && deviceManager.connectionQueue.size() > 0) {
//                设置值
                deviceFirst.characteristic6.setValue(dataTrans);
//                发送
                deviceManager.connectionQueue.get(0).writeCharacteristic(deviceFirst.characteristic6);
            }
            if (deviceSecond.characteristic6 != null && deviceManager.connectionQueue.size() > 1) {
//                设置值
                deviceSecond.characteristic6.setValue(dataTrans);
//                发送
                deviceManager.connectionQueue.get(1).writeCharacteristic(deviceSecond.characteristic6);
            }
            wifiSend(dataTrans);

            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
//                  没有发送成功
                    if ((!checkSendOkFirst()) && (!checkSendOkSecond())) {
                        isBusy = true;
                        if (deviceFirst.characteristic6 != null && deviceManager.connectionQueue.size() > 0 && deviceFirst.isReadyForNextFor) {
                            deviceFirst.characteristic6.setValue(dataTrans);
                            //  if(!isHBSending) {
                            isDataSending = true;
                            deviceManager.connectionQueue.get(0).writeCharacteristic(deviceFirst.characteristic6);
                            Log.d("datasend", "write characteristic");
                            //  }
                        }
                        if (deviceSecond.characteristic6 != null && deviceManager.connectionQueue.size() > 1 && deviceSecond.isReadyForNextFor) {
                            deviceSecond.characteristic6.setValue(dataTrans);
                            //  if(!isHBSending) {
                            isDataSending = true;
                            deviceManager.connectionQueue.get(1).writeCharacteristic(deviceSecond.characteristic6);
                            Log.d("datasend", "write characteristic");
                            //  }
                        }
//                        100ms之后执行runnable
                        if (deviceFirst.isReadyForNextFor || deviceSecond.isReadyForNextFor)
                            handler.postDelayed(this, 150);
                    }
//                    发送成功
                    else {
                        isBusy = false;
                        isDataSending = false;
                    }
                }
            };
//           第一次进来的时候不会执行run，只有postDelayed触发时才会
//·          第二次的时候发现第一次还是没有发送成功就不再运行一遍
            if (!isBusy)
                handler.postDelayed(runnable, 150);
        }

        //        获取心跳包数据
        public byte[] getHeartBeats() {
            if (deviceFirst.isReadyForNextFor)
                return deviceFirst.dataHeartBeats;
            if (deviceSecond.isReadyForNextFor)
                return deviceSecond.dataHeartBeats;
            return deviceFirst.dataHeartBeats;
        }


        public int getConnectNum() {
            int conNum = 0;
            if (deviceFirst.isReadyForNextFor == true)
                conNum++;
            if (deviceSecond.isReadyForNextFor == true)
                conNum++;
            return conNum;
        }


        public boolean checkSendOkFirst() {
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
                    Log.e("bletrack", "communicate unstable");
                    return true;
                }
            }
            return false;
        }

        public boolean checkSendOkSecond() {
//            蓝牙没有准备好，就一直不重发
            if (!deviceSecond.isReadyForNextFor) {
                return true;
            }
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
                    Log.e("bletrack", "communicate unstable");
                    return true;
                }
            }
            return false;
        }

        //        连接是否完成
        public boolean isReadyFirst() {
            return deviceFirst.isReadyForNextFor;
        }

        public boolean isReadySecond() {
            return deviceSecond.isReadyForNextFor;
        }

        //        读取蓝牙强度
        public int readRssiFirst() {
            if (deviceFirst.isReadyForNextFor) {
                deviceManager.connectionQueue.get(0).readRemoteRssi();
                return deviceFirst.RssiValue;
            } else {
                return 0;
            }
        }

        public int readRssiSecond() {
            if (deviceSecond.isReadyForNextFor) {
                deviceManager.connectionQueue.get(1).readRemoteRssi();
                return deviceSecond.RssiValue;
            } else {
                return 0;
            }
        }

    }

    //    如果连接上
//    如果没连接上 扫到设备
//    如果没连接上 连接到指定设备
//    通过handler获得数据，改变文本框的字
//    关闭蓝牙再测
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

//        if (intent.getStringExtra("data").equals("scan")) {
//
//                if(deviceFirst.connectionState ==STATE_DISCONNECTED)
//                {
//
//                    isNeedForScan = true;
//                    Log.d("bletrack", "connect1 fail rescan");
//                }else if(deviceSecond.connectionState ==STATE_DISCONNECTED){
//
//                    isNeedForScan = true;
//                    Log.d("bletrack", "connect2 fail rescan");
//                }
//            if (mConnectionStateFirst ==STATE_DISCONNECTED) {
//                scanLeDevice(true);
//                isNeedForScan = true;
//                Log.d("bletrack", "connect fail rescan");
//            } else {
//                isNeedForScan = false;
//                returnState = STATE_CONNECTION_READY;
//                sharedPreferencesHelper.putString("returnState", returnState);
//                Log.d("bletrack", "connect succeed");
//            }
//        }
        return flags;
    }

    @Override
    public void onDestroy() {
        Log.d("servicetrack", "Ble Service onDestroy");
        super.onDestroy();
        disconnect(0);
        GattClose(0);
    }

}
