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
    private byte[] dataReceive;                 //    接收数据缓存区
    private byte[] dataTrans;                   //    发送数据缓存区
    private byte[] dataHeartBeats;              //    心跳包的缓存区
    private int HBcount = 0;                      //    心跳包的计数
    private int RssiValue = 0;                    //    RSSI

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private static boolean isReadyForNextFor1 = false;
    private static boolean isReadyForNextFor2 = false;
    private int mConnectionState = STATE_DISCONNECTED;
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
            scaner.stopScan(mScanCallback);   // 停止扫描
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
                Log.d("bletrack", "find device: " + deviceScan.getName());
                Log.d("bletrack", "device address: " + deviceScan.getAddress());
                if (!mDeviceContainer.isEmpty()) {
                    Log.d("bletrack", "123 " );
                    if (!isEquals(deviceScan)) {
                        Log.d("bletrack", "device address: 456" );
                        connectBle(deviceScan);
                    }
                } else {
                    Log.d("bletrack", "device address:789 " );
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
            Log.d("bletrack", "扫描失败");
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
        mConnectionState = STATE_CONNECTING;
        isConnectPermit = false;//创建connectionQueue.get(0)时会进入mGattCallback
        // We want to directly connect to the device, so we are setting the
        // autoConnect
        // parameter to false.
        /* 调用device中的connectGatt连接到远程设备 */

        BluetoothGatt bluetoothGatt;
        bluetoothGatt = device.connectGatt(this, false, mGattCallback);
        //如果没有
        if (checkGatt(bluetoothGatt)) {
            connectionQueue.add(bluetoothGatt);
        }

        if(connectionQueue.size()==2)
        {
            scanLeDevice(false);
        }
        return true;
    }

    private boolean checkGatt(BluetoothGatt bluetoothGatt) {
        if (!connectionQueue.isEmpty()) {
            for (BluetoothGatt btg : connectionQueue) {
                if (btg.equals(bluetoothGatt)) {
                    return false;
                }
            }
        }
        return true;
    }


    /* 连接远程设备的回调函数 */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                switch (newState) {
//                    如果连接状态正常
                    case BluetoothProfile.STATE_CONNECTED:
                        mConnectionState = STATE_CONNECTED;
//                        如果地址正确
                        if (gatt.getDevice().getAddress().equals(AIMADDRESS1)
                                || gatt.getDevice().getAddress().equals(AIMADDRESS2)) {
                            Log.d("bletrack", "ble connected");
                            if (gatt.equals(connectionQueue.get(0))) {
                                isReadyForNextFor1 = false;                           //得到特征值之后才能准备好
                                gatt.discoverServices();              //去发现服务
                                Log.d("bletrack", "gatt1 looks for service");
                            }
                            if (gatt.equals(connectionQueue.get(1))) {
                                isReadyForNextFor2 = false;                           //得到特征值之后才能准备好
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
                        mConnectionState = STATE_DISCONNECTED;
//                    加上会变快
                        Log.d("bletrack", "ble disconnected");
                        //秒连速度之快已经让车上蓝牙检测不到断开了
                        if (isConnectPermit) {
                            if (gatt.connect())//连接蓝牙，其实就是调用BluetoothGatt的连接方法
                                Log.d("bletrack", "reconnect succeed");
                            else {
                                Log.d("bletrack", "reconnect fail");
                            }
                        }
                        //scanLeDevice(true);
                        if (gatt.equals(mDeviceContainer.get(0))) {
                            isReadyForNextFor1 = false;
                        }
                        if (gatt.equals(mDeviceContainer.get(1))) {
                            isReadyForNextFor2 = false;
                        }
                        break;
                    default:                                                //正在连接等等
                        if (gatt.equals(mDeviceContainer.get(0))) {
                            isReadyForNextFor1 = false;
                        }
                        if (gatt.equals(mDeviceContainer.get(1))) {
                            isReadyForNextFor2 = false;
                        }
                        Log.e("bletrack", "unkown newstate: " + String.valueOf(newState));
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
                Log.d("bletrack", "ble gatt service success");
                if (gatt.equals(connectionQueue.get(0))) {
                    isReadyForNextFor1 = true;
                    bleTool.findService(1, connectionQueue.get(0));
                } else if (gatt.equals(connectionQueue.get(1))) {
                    isReadyForNextFor2 = true;
                    bleTool.findService(2, connectionQueue.get(1));
                }

            } else {
                if (gatt.equals(mDeviceContainer.get(0))) {
                    isReadyForNextFor1 = false;
                }
                if (gatt.equals(mDeviceContainer.get(1))) {
                    isReadyForNextFor2 = false;
                }
                Log.d("bletrack", "ble gatt service fail");
                disconnect();
            }
        }

        /*
         * 特征值的读写
         *
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            if (status == connectionQueue.get(0).GATT_SUCCESS) {
                String log_out = new String();
                for (int i = 0; i < bleDataLen; i++) {
//                    为了log的时候好观察，加了个\t
                    log_out += String.valueOf(characteristic.getValue()[i]) + '\t';
                }
                Log.d("bletrack", "read value: " + log_out);
            } else {
                Log.d("bletrack", "char read fail");
                disconnect();
            }
        }

        /* *特征值的改* */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            byte[] temp;
            Log.d("ACHB", "notify: ");
            temp = characteristic.getValue();
            String log_out = new String();
            for (int i = 0; i < 12; i++) {
                if (i < 4)
                    log_out += String.valueOf((char) (temp[i])) + '\t';
                else
                    log_out += String.valueOf(temp[i]) + '\t';
            }
//                判断是否是心跳包
            if (temp[0] == 'A' && temp[1] == 'C' && temp[2] == 'H' && temp[3] == 'B') {
                dataHeartBeats = temp;
                HBcount++;
                if (temp[characteristic.getValue().length - 1] != 0 || temp[characteristic.getValue().length - 2] != 0)
                    Log.d("ACHB", "notify: " + log_out);
            } else {
                dataReceive = temp;
                if (dataReceive.length != bleDataLen) {
                    Log.e("version err", "length of receivedata is not equal to require");
                }
                Log.d("bletrack", "notify: " + log_out);
            }
        }

        /*
         * 特征值的写
         * */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                byte[] temp;
                temp = characteristic.getValue();
                String log_out = new String();
                for (int i = 0; i < 12; i++) {
                    if (i < 4)
                        log_out += String.valueOf((char) (temp[i])) + '\t';
                    else
                        log_out += String.valueOf((int) temp[i]) + '\t';
                }
                if (temp[0] == 'A' && temp[1] == 'C' && temp[2] == 'H' && temp[3] == 'B') {

                } else
                    Log.d("bletrack", "write: " + log_out);
            } else {
                Log.d("bletrack", "char write fail");
                disconnect();
            }
        }

        /*读写蓝牙信号值 */
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            RssiValue = rssi;
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
                if (connectionQueue.get(0) == null) {
                } else {
                    connectionQueue.get(0).close();
                    connectionQueue.remove(0);
                }
                if (connectionQueue.get(1) == null) {
                } else {
                    connectionQueue.get(1).close();
                    connectionQueue.remove(1);
                }
                break;
            case 1:
                if (connectionQueue.get(0) == null) {
                } else {
                    connectionQueue.get(0).close();
                    connectionQueue.remove(0);
                }
                break;
            case 2:
                if (connectionQueue.get(1) == null) {
                } else {
                    connectionQueue.get(1).close();
                    connectionQueue.remove(1);
                }
                break;
        }
    }

    public void disconnect() {
        if (mBluetoothAdapter == null || connectionQueue.get(0) == null) {
            Log.w("bletrack", "BluetoothAdapter not initialized");
            return;
        } else {
            Log.w("bletrack", "disconnect()");
            isConnectPermit = true;
            connectionQueue.get(0).disconnect();
        }
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || connectionQueue.get(0) == null) {
            Log.w("bletrack", "BluetoothAdapter not initialized");
            return;
        }
        connectionQueue.get(0).readCharacteristic(characteristic);
    }

    // 写入特征值
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || connectionQueue.get(0) == null) {
            Log.w("bletrack", "BluetoothAdapter not initialized");
            return;
        }
        connectionQueue.get(0).writeCharacteristic(characteristic);

    }


    public void getCharacteristicDescriptor(BluetoothGattDescriptor descriptor) {
        if (mBluetoothAdapter == null || connectionQueue.get(0) == null) {
            Log.w("bletrack", "BluetoothAdapter not initialized");
            return;
        }

        connectionQueue.get(0).readDescriptor(descriptor);
    }


    public List<BluetoothGattService> getSupportedGattServices() {
        if (connectionQueue.get(0) == null)
            return null;
        return connectionQueue.get(0).getServices();

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
                private int errCount = 0;
                private int lastHBcount = 0;

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
                    if (connectionQueue.get(0) != null) {
                        if (isReadyForNextFor1) {
                            if (bleTool.charFirst7 != null) {
                                bleTool.charFirst7.setValue(heartBeat);
                                if (!isDataSending) {
                                    connectionQueue.get(0).writeCharacteristic(bleTool.charFirst7);
                                    isHBSending = true;
                                    Log.e("ACHB", "HeartBeats send");
                                }
                            }
                            if (HBcount == lastHBcount && (!isDataSending))
                                errCount++;
                            else
                                errCount = 0;
                            lastHBcount = HBcount;
                            if (errCount >= 7) {
                                Log.e("bletrack", "HeartBeats disconnect");
                              //  isReadyForNextFor1 = false;
                              //  disconnect();
                                errCount = 0;
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
                            if (HBcount == lastHBcount && (!isDataSending))
                                errCount++;
                            else
                                errCount = 0;
                            lastHBcount = HBcount;
                            if (errCount >= 13) {
                                Log.e("bletrack", "HeartBeats disconnect");
                             //   isReadyForNextFor2 = false;
                             //   disconnect();
                                errCount = 0;
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

    private static int rssiLast = 0;
    private static int rssiError = 0;

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
            if (bleTool.charFirst6 != null && connectionQueue.get(0) != null) {
//                设置值
                bleTool.charFirst6.setValue(dataTrans);
//                发送
                connectionQueue.get(0).writeCharacteristic(bleTool.charFirst6);
            }
            wifiSend(dataTrans);

            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
//                  没有发送成功
                    if (!checkSendOk()) {
                        isBusy = true;
                        if (bleTool.charFirst6 != null && connectionQueue.get(0) != null) {
                            bleTool.charFirst6.setValue(dataTrans);
                            //  if(!isHBSending) {
                            isDataSending = true;
                            connectionQueue.get(0).writeCharacteristic(bleTool.charFirst6);
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
            return dataHeartBeats;
        }

        public boolean checkSendOk() {
//            蓝牙没有准备好，就一直不重发
            if (!isReadyForNextFor1) {
                return true;
            }
//            如果二者相等则返回true
            if (Arrays.equals(dataReceive, dataTrans)) {
                return true;
            }
//            判断数据是否匹配
            if (dataReceive != null && dataTrans != null) {
                int i;
//                如果i==9时不相等，执行break,此时i不++，i依然不满足=10条件
                for (i = 0; i < 10; i++) {
                    if (dataReceive[i] != dataTrans[i])
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
        public boolean isReady() {
            return isReadyForNextFor1;
        }

        //        读取蓝牙强度
        public int readRssi() {
            if (isReadyForNextFor1) {
                connectionQueue.get(0).readRemoteRssi();
                if (rssiLast == RssiValue) rssiError++;
                else rssiError = 0;
                if (rssiError > 1) {
                    rssiError = 0;
                    //connect(AIMADDRESS1);
                    Log.d("bletrack", "rssi disconnect");
                }
                rssiLast = RssiValue;
                return RssiValue;
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
            if (mConnectionState != STATE_CONNECTED) {
                scanLeDevice(true);
                isNeedForScan = true;
                Log.d("bletrack", "connect fail rescan");
            } else {
                isNeedForScan = false;
                returnState = STATE_CONNECTION_READY;
                sharedPreferencesHelper.putString("returnState", returnState);
                Log.d("bletrack", "connect succeed");
            }
        } else if (intent.getStringExtra("data").equals("重启")) {
            disconnect();
            Log.d("bletrack", "重启");
        }


        return flags;
    }

    @Override
    public void onDestroy() {
        Log.d("servicetrack", "Ble Service onDestroy");
        super.onDestroy();
        disconnect();
        GattClose(0);
    }

}
