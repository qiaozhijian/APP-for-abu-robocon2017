package com.action.app.actionctr2.ble;

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
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
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
import java.util.List;

public class BleService extends Service {

    private final IBinder mBinder = new myBleBand();

    private static BleTool bleTool = new BleTool();

    private List<Sensor> mEnabledSensors = new ArrayList<Sensor>();
    //蓝牙相关类
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothGattService mServiceFirst;
    private BluetoothLeScanner scaner;  // android5.0把扫描方法单独弄成一个对象了

    private ArrayList<BluetoothDevice> mDeviceContainer = new ArrayList<BluetoothDevice>();
    private ArrayList<BluetoothGatt> connectionQueue = new ArrayList<BluetoothGatt>();

    public static final int bleDataLen = 12;      //    特征值的长度
    private byte[] dataReceiveFirst;                 //    接收数据缓存区
    private byte[] dataReceiveSecond;                 //    接收数据缓存区
    private byte[] dataTrans;                   //    发送数据缓存区
    private byte[] dataHeartBeatsFirst;              //    心跳包的缓存区
    private byte[] dataHeartBeatsSecond;              //    心跳包的缓存区
    private int HBcountFirst = 0;                      //    心跳包的计数
    private int HBcountSecond = 0;                      //    心跳包的计数
    private int RssiValueFirst = 0;                    //    RSSI
    private int RssiValueSecond = 0;                    //    RSSI

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private static boolean isReadyForNextFor1 = false;
    private static boolean isReadyForNextFor2 = false;
    private int mConnectionStateFirst = STATE_DISCONNECTED;
    private int mConnectionStateSecond = STATE_DISCONNECTED;
    private boolean isHBSending = false;                //是否在发送
    private boolean isDataSending = false;
    private boolean isConnectPermit = false;
    private static final String STATE_IDLE = "STATE_IDLE";
    private static final String STATE_CONNECTION_READY = "STATE_CONNECTION_READY";
    private static final String STATE_CONNECTION_FAIL_SCAN = "STATE_CONNECTION_FAIL_SCAN";
    private static final String STATE_CONNECTION_FAIL_SCAN_RIGHT = "STATE_CONNECTION_FAIL_SCAN_RIGHT";
    private boolean isNeedForScan = false;
    private String returnState = STATE_IDLE;

    private boolean isFirstFinished = false;

    // 描述扫描蓝牙的状态
    private boolean mScanning;
    private int mRssi;

    private SharedPreferencesHelper sharedPreferencesHelper;

    //private final String AIMADDRESS1 = "F4:5E:AB:B9:58:80";//1号 白色平板
    // private final String AIMADDRESS1="50:65:83:86:C6:33";//这个参数是车上用的平板 2号
    //private final static String AIMADDRESS1 = "98:7B:F3:60:C7:1C";//测试版
    private final String AIMADDRESS1 = "F4:5E:AB:B9:5A:03";// //手机
    private final String AIMADDRESS2 = "F4:5E:AB:B9:59:77";//手机
    // private final String AIMADDRESS1="98:7B:F3:60:C7:01";//
    //private final String AIMADDRESS1 = "90:59:AF:0E:60:1F";//

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
                    Log.d("bletrack", "mDeviceContainer is not empty ");
                    if (!isEquals(deviceScan)) {
                        Log.d("bletrack", "add new device");
                        Log.d("bletrack", "find device: " + deviceScan.getName());
                        Log.d("bletrack", "device address: " + deviceScan.getAddress());
                        connectBle(deviceScan);
                    }
                } else {
                    Log.d("bletrack", "mDeviceContainer is empty ");
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
        connect(device.getAddress());
    }

    // 连接远程蓝牙
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w("bletrack",
                    "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        isNeedForScan = false;

        //scanLeDevice(false);

        /* 获取远端的蓝牙设备 */
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        if (device == null) {
            Log.w("bletrack", "Device not found.  Unable to connect.");
            return false;
        }
        Log.d("bletrack", "start connecting");
        if(address.w)
        mConnectionStateFirst = STATE_CONNECTING;
        isConnectPermit = false;//创建connectionQueue.get(0)时会进入mGattCallback
        // We want to directly connect to the device, so we are setting the
        // autoConnect
        // parameter to false.
        /* 调用device中的connectGatt连接到远程设备 */

        BluetoothGatt bluetoothGatt;
        bluetoothGatt = device.connectGatt(this, false, mGattCallback);
        //如果没有
        if (checkGATT(bluetoothGatt)) {
            connectionQueue.add(bluetoothGatt);
        }
        if (connectionQueue.size() == 2) {
            scanLeDevice(false);
            Log.d("bletrack", "two devices connect");
        }
        return true;
    }

    static private int errorCountForRead = 0;
    static private int errorCountForChange = 0;
    static private int errorCountForWrite = 0;
    static private int errorCountForService = 0;
    static private int errorCountForRssi = 0;
    private final int permitForCount = 5;
    /* 连接远程设备的回调函数 */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                switch (newState) {
//                    如果连接状态正常
                    case BluetoothProfile.STATE_CONNECTED:
                        mConnectionStateFirst = STATE_CONNECTED;
//                        如果地址正确
                        if (gatt.getDevice().getAddress().equals(AIMADDRESS1)
                                || gatt.getDevice().getAddress().equals(AIMADDRESS2)) {
                            Log.d("bletrack", "ble connected");
                            if (checkGATT(gatt, 0)) {
                                isReadyForNextFor1 = false;           //得到特征值之后才能准备好
                                gatt.discoverServices();              //去发现服务
                                Log.d("bletrack", "gatt1 looks for service");
                            }
                            
                            if (checkGATT(gatt, 1)) {
                                isReadyForNextFor2 = false;           //得到特征值之后才能准备好
                                gatt.discoverServices();              //去发现服务
                                Log.d("bletrack", "gatt2 looks for service");
                            }
                            isConnectPermit = true;
                        } else {                                              //地址不正确，断开连接
                            Log.d("bletrack", "ble devicce err");
                            gatt.disconnect();
                        }
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:               //断开连接
                        mConnectionStateFirst = STATE_DISCONNECTED;
//                    加上会变快
                        Log.d("bletrack", "ble disconnected");
                        //scanLeDevice(true);
                        if (checkGATT(gatt, 0)) {
                            isReadyForNextFor1 = false;
                        }

                        if (checkGATT(gatt, 1)) {
                            isReadyForNextFor2 = false;
                        }
                        //秒连速度之快已经让车上蓝牙检测不到断开了
                        if (isConnectPermit) {
                            if(!isReadyForNextFor2)
                                connect(AIMADDRESS2);
                            if(!isReadyForNextFor1)
                                connect(AIMADDRESS1);
                        }

                        break;
                    default:                                                //正在连接等等
                        if (checkGATT(gatt, 0)) {
                            isReadyForNextFor1 = false;
                        }

                        if (checkGATT(gatt, 1)) {
                            isReadyForNextFor2 = false;
                        }

                        Log.e("bletrack", "unkown newstate: " + String.valueOf(newState));
                        break;
                }
                errorCountForChange = 0;
            } else {
                errorCountForChange++;
                Log.e("bletrack", "unkown disconnected: " + String.valueOf(status));
                if (errorCountForChange > permitForCount) {
                    gatt.close();
                    scanLeDevice(true);
                    errorCountForChange = 0;
                }
            }
        }

        //            当新服务被发现，进这个回调
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//                如果特征值和描述被更新
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("bletrack", "ble gatt service success");
                if (checkGATT(gatt, 0)) {
                    isReadyForNextFor1 = true;
                    bleTool.findService(1, connectionQueue.get(0));
                }

                if (checkGATT(gatt, 1)) {
                    isReadyForNextFor2 = true;
                    bleTool.findService(2, connectionQueue.get(1));
                }
                errorCountForService = 0;
            } else {
                errorCountForService++;
                if (errorCountForService > permitForCount) {
                    if (checkGATT(gatt, 0)) {
                        isReadyForNextFor1 = false;
                        Log.d("bletrack", "ble gatt1 service fail");
                    }

                    if (checkGATT(gatt, 1)) {
                        isReadyForNextFor2 = false;
                        Log.d("bletrack", "ble gatt2 service fail");
                    }
                    gatt.disconnect();
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
            if (status == gatt.GATT_SUCCESS) {
                if (checkGATT(gatt, 0)) {
                    String log_out = new String();
                    for (int i = 0; i < bleDataLen; i++) {
//                    为了log的时候好观察，加了个\t
                        log_out += String.valueOf(characteristic.getValue()[i]) + '\t';
                    }
                    Log.d("bletrack", "gatt 0 read value: " + log_out);
                }


                if (checkGATT(gatt, 1)) {
                    String log_out = new String();
                    for (int i = 0; i < bleDataLen; i++) {
//                    为了log的时候好观察，加了个\t
                        log_out += String.valueOf(characteristic.getValue()[i]) + '\t';
                    }
                    Log.d("bletrack", "gatt 1 read value: " + log_out);
                }
                errorCountForRead = 0;
            } else {
                errorCountForRead++;
                if (errorCountForRead > permitForCount) {
                    if (checkGATT(gatt, 0)) {
                        Log.d("bletrack", "ble gatt1 service fail");
                    }
                    if (checkGATT(gatt, 1)) {
                        Log.d("bletrack", "ble gatt2 service fail");
                    }
                    gatt.disconnect();
                }
            }
        }

        //是不是从机设置有问题，一是多少错误就断开，二是多长广播还是说，速度重连的要求
        /* *特征值的改* */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            String log_out = new String();

            if (checkGATT(bleTool.charFirst6, characteristic)) {
                dataReceiveFirst = characteristic.getValue();
                log_out = bleTool.getCharValue(characteristic);
                Log.d("bletrack", "first notify: " + log_out);
            }

            if (checkGATT(bleTool.charFirst7, characteristic)) {
                dataHeartBeatsFirst = characteristic.getValue();
                HBcountFirst++;
                log_out = bleTool.getCharValue(characteristic);
                Log.d("ACHB", "first notify: " + log_out);
                if (dataReceiveFirst.length != bleDataLen) {
                    Log.e("bletrack", "notify first length is not equal to require");
                }
            }

            if (checkGATT(bleTool.charSecond6, characteristic)) {
                dataReceiveSecond = characteristic.getValue();
                log_out = bleTool.getCharValue(characteristic);
                Log.d("bletrack", "second notify: " + log_out);
            }

            if (checkGATT(bleTool.charSecond7, characteristic)) {
                dataHeartBeatsSecond = characteristic.getValue();
                HBcountSecond++;
                log_out = bleTool.getCharValue(characteristic);
                Log.d("ACHB", "second notify: " + log_out);
                if (dataReceiveSecond.length != bleDataLen) {
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
                if (checkGATT(bleTool.charFirst6, characteristic)) {
                    log_out = bleTool.getCharValue(characteristic);
                    Log.d("bletrack", "first write: " + log_out);
                }

                if (checkGATT(bleTool.charFirst7, characteristic)) {
                    log_out = bleTool.getCharValue(characteristic);
                    Log.d("ACHB", "first write: " + log_out);
                    if (dataReceiveFirst.length != bleDataLen) {
                        Log.e("bletrack", "write first length is not equal to require");
                    }
                }

                if (checkGATT(bleTool.charSecond6, characteristic)) {
                    log_out = bleTool.getCharValue(characteristic);
                    Log.d("bletrack", "second write: " + log_out);
                }

                if (checkGATT(bleTool.charSecond7, characteristic)) {
                    log_out = bleTool.getCharValue(characteristic);
                    Log.d("ACHB", "second write: " + log_out);
                    if (dataReceiveSecond.length != bleDataLen) {
                        Log.e("bletrack", "write second length is not equal to require");
                    }
                }
                errorCountForWrite = 0;

            } else {
                errorCountForWrite++;
                if (errorCountForWrite > errorCountForWrite) {
                    if (checkGATT(gatt, 0)) {
                        Log.d("bletrack", "gatt first char write fail");
                    }
                    if (checkGATT(gatt, 1)) {
                        Log.d("bletrack", "gatt second char write fail");
                    }
                    gatt.disconnect();
                }
            }
        }

        /*读写蓝牙信号值 */
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (checkGATT(gatt, 0)) {
                    RssiValueFirst = rssi;
                }
                if (checkGATT(gatt, 1)) {
                    RssiValueSecond = rssi;
                }
                errorCountForRssi = 0;
            } else {
                errorCountForRssi++;
                if (errorCountForRssi > errorCountForWrite) {
                    if (checkGATT(gatt, 0)) {
                        Log.d("bletrack", "gatt first rssi fail");
                    }
                    if (checkGATT(gatt, 1)) {
                        Log.d("bletrack", "gatt second rssi fail");
                    }
                    gatt.disconnect();
                }
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
                if (checkGATT(0)) {
                    connectionQueue.get(0).close();
                    connectionQueue.remove(0);
                    Log.w("bletrack", "gatt 1 close()");
                }

                if (checkGATT(1)) {
                    connectionQueue.get(1).close();
                    connectionQueue.remove(1);
                    Log.w("bletrack", "gatt 2 close()");
                }
                break;
            case 1:
                if (checkGATT(0)) {
                    connectionQueue.get(0).close();
                    connectionQueue.remove(0);
                    Log.w("bletrack", "gatt 1 close()");
                }
                break;
            case 2:
                if (checkGATT(1)) {
                    connectionQueue.get(1).close();
                    connectionQueue.remove(1);
                    Log.w("bletrack", "gatt 2 close()");
                }
                break;
        }
    }

    public void disconnect(int i) {
        if (mBluetoothAdapter == null || connectionQueue.get(0) == null) {
            Log.w("bletrack", "BluetoothAdapter not initialized");
            return;
        } else {
            isConnectPermit = true;
            switch (i) {
                case 0:
                    if (checkGATT(0)) {
                        connectionQueue.get(0).disconnect();
                        Log.w("bletrack", "gatt 1 discount()");
                    }

                    if (checkGATT(1)) {
                        connectionQueue.get(1).disconnect();
                        Log.w("bletrack", "gatt 2 discount()");
                    }

                    break;
                case 1:
                    if (checkGATT(0)) {
                        connectionQueue.get(0).disconnect();
                        Log.w("bletrack", "gatt 1 discount()");
                    }

                    break;
                case 2:
                    if (checkGATT(1)) {
                        connectionQueue.get(1).disconnect();
                        Log.w("bletrack", "gatt 2 discount()");
                    }

                    break;
            }
        }
    }

    private boolean checkGATT(int order) {

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

    private boolean checkGATT(BluetoothGatt gatt, int order) {

        if (connectionQueue.size() > order) {
            if (gatt.equals(connectionQueue.get(order)))
                return true;
            else {
                Log.d("checkGATT", String.valueOf(order) + "equal null");
                return false;
            }
        } else {
            Log.d("checkGATT", String.valueOf(order) + "equal size fail");
            return false;
        }
    }

    private boolean checkGATT(BluetoothGatt bluetoothGatt) {
        if (!connectionQueue.isEmpty()) {
            for (BluetoothGatt btg : connectionQueue) {
                if (btg.equals(bluetoothGatt)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkGATT(BluetoothGattCharacteristic blechar, BluetoothGattCharacteristic characteristic) {

        if (blechar != null) {
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

            scanLeDevice(true);

            //connect(AIMADDRESS1);

            //下面的代码用于发送心跳包
            final Handler handlerHeartBeat = new Handler();
            Runnable runnable = new Runnable() {
                private int errCount1 = 0;
                private int lastHBcountFirst = 0;
                private int errCount2 = 0;
                private int lastHBcountSecond = 0;

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
                    if (connectionQueue.size() > 0) {
                        if (isReadyForNextFor1) {
                            if (bleTool.charFirst7 != null) {
                                bleTool.charFirst7.setValue(heartBeat);
                                if (!isDataSending) {
                                    connectionQueue.get(0).writeCharacteristic(bleTool.charFirst7);
                                    isHBSending = true;
                                    Log.e("ACHB", "HeartBeats send");
                                }
                            }
                            if (HBcountFirst == lastHBcountFirst && (!isDataSending))
                                errCount1++;
                            else
                                errCount1 = 0;
                            lastHBcountFirst = HBcountFirst;
                            if (errCount1 >= 13) {
                                Log.e("bletrack", "HeartBeats disconnect");
                                isReadyForNextFor1 = false;
                                disconnect(1);
                                errCount1 = 0;
                            }
                        }
                        if (isReadyForNextFor2) {
                            if (bleTool.charSecond7 != null) {
                                bleTool.charSecond7.setValue(heartBeat);
                                if (!isDataSending) {
                                    connectionQueue.get(1).writeCharacteristic(bleTool.charSecond7);
                                    isHBSending = true;
                                    Log.e("ACHB", "HeartBeats send");
                                }
                            }
                            if (HBcountSecond == lastHBcountSecond && (!isDataSending))
                                errCount2++;
                            else
                                errCount2 = 0;
                            lastHBcountSecond = HBcountSecond;
                            if (errCount2 >= 13) {
                                Log.e("bletrack", "HeartBeats disconnect");
                                isReadyForNextFor2 = false;
                                disconnect(2);
                                errCount2 = 0;
                            }
                        }
                    }

                    isHBSending = false;
                    handlerHeartBeat.postDelayed(this, 300);
                }
            };
//        不同于上面，上面是按键按一次就会执行一次，但是这个是只会在程序启动的时候执行
            handlerHeartBeat.postDelayed(runnable, 3000);
        } catch (Exception e) {
            Log.e("bletrack", e.getMessage());
        }
        Log.d("servicetrack", getClass().getSimpleName() + "oncreate");
    }

    //    蓝牙发数  binder跟所有涉及到蓝牙的activity通信
    private myBleBand dataSend = new myBleBand();

    private static int rssiLastFirst = 0;
    private static int rssiErrorFirst = 0;
    private static int rssiLastSecond = 0;
    private static int rssiErrorSecond = 0;

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
            if (bleTool.charFirst6 != null && connectionQueue.size() > 0) {
//                设置值
                bleTool.charFirst6.setValue(dataTrans);
//                发送
                connectionQueue.get(0).writeCharacteristic(bleTool.charFirst6);
            }
            if (bleTool.charSecond6 != null && connectionQueue.size() > 1) {
//                设置值
                bleTool.charSecond6.setValue(dataTrans);
//                发送
                connectionQueue.get(1).writeCharacteristic(bleTool.charSecond6);
            }
            wifiSend(dataTrans);

            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
//                  没有发送成功
                    if ((!checkSendOkFirst()) && (!checkSendOkSecond())) {
                        isBusy = true;
                        if (bleTool.charFirst6 != null && connectionQueue.size() > 0) {
                            bleTool.charFirst6.setValue(dataTrans);
                            //  if(!isHBSending) {
                            isDataSending = true;
                            connectionQueue.get(0).writeCharacteristic(bleTool.charFirst6);
                            Log.d("datasend", "write characteristic");
                            //  }
                        }
                        if (bleTool.charSecond6 != null && connectionQueue.size() > 1) {
                            bleTool.charSecond6.setValue(dataTrans);
                            //  if(!isHBSending) {
                            isDataSending = true;
                            connectionQueue.get(1).writeCharacteristic(bleTool.charSecond6);
                            Log.d("datasend", "write characteristic");
                            //  }
                        }
//                        100ms之后执行runnable
                        handler.postDelayed(this, 100);
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
                handler.postDelayed(runnable, 100);
        }

        //        获取心跳包数据
        public byte[] getHeartBeats() {
            if (isReadyForNextFor1)
                return dataHeartBeatsFirst;
            if (isReadyForNextFor2)
                return dataHeartBeatsSecond;
            return dataHeartBeatsFirst;
        }


        public int getConnectNum() {
            int conNum = 0;
            if (isReadyForNextFor1 == true)
                conNum++;
            if (isReadyForNextFor2 == true)
                conNum++;
            return conNum;
        }


        public boolean checkSendOkFirst() {
//            如果二者相等则返回true
            if (Arrays.equals(dataReceiveFirst, dataTrans)) {
                return true;
            }
//            判断数据是否匹配
            if (dataReceiveFirst != null && dataTrans != null) {
                int i;
//                如果i==9时不相等，执行break,此时i不++，i依然不满足=10条件
                for (i = 0; i < 10; i++) {
                    if (dataReceiveFirst[i] != dataTrans[i])
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
            if (!isReadyForNextFor2) {
                return true;
            }
//            如果二者相等则返回true
            if (Arrays.equals(dataReceiveSecond, dataTrans)) {
                return true;
            }
//            判断数据是否匹配
            if (dataReceiveSecond != null && dataTrans != null) {
                int i;
//                如果i==9时不相等，执行break,此时i不++，i依然不满足=10条件
                for (i = 0; i < 10; i++) {
                    if (dataReceiveSecond[i] != dataTrans[i])
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
            return isReadyForNextFor1;
        }

        public boolean isReadySecond() {
            return isReadyForNextFor2;
        }

        //        读取蓝牙强度
        public int readRssiFirst() {
            if (isReadyForNextFor1) {
                connectionQueue.get(0).readRemoteRssi();
                if (rssiLastFirst == RssiValueFirst) rssiErrorFirst++;
                else rssiErrorFirst = 0;
                if (rssiErrorFirst > 1) {
                    rssiErrorFirst = 0;
                    //connect(AIMADDRESS1);
                    Log.d("RSSI", "rssi disconnect");
                }
                rssiLastFirst = RssiValueFirst;
                return RssiValueFirst;
            } else {
                return 0;
            }
        }

        public int readRssiSecond() {
            if (isReadyForNextFor2) {
                connectionQueue.get(1).readRemoteRssi();
                if (rssiLastSecond == RssiValueSecond) rssiErrorSecond++;
                else rssiErrorSecond = 0;
                if (rssiErrorSecond > 1) {
                    rssiErrorSecond = 0;
                    //connect(AIMADDRESS1);
                    Log.d("RSSI", "rssi disconnect");
                }
                rssiLastSecond = RssiValueSecond;
                return RssiValueSecond;
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

        if (intent.getStringExtra("data").equals("scan")) {
            if (mConnectionStateFirst != STATE_CONNECTED) {
                scanLeDevice(true);
                isNeedForScan = true;
                Log.d("bletrack", "connect fail rescan");
            } else {
                isNeedForScan = false;
                returnState = STATE_CONNECTION_READY;
                sharedPreferencesHelper.putString("returnState", returnState);
                Log.d("bletrack", "connect succeed");
            }
        }
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
