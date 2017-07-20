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
import com.action.app.actionctr2.wifi.wifiService;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BleService extends Service {

    private final IBinder mBinder = new myBleBand();

    private List<Sensor> mEnabledSensors = new ArrayList<Sensor>();
    //蓝牙相关类
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothGattService mGATTService;
    private BluetoothLeScanner scaner;  // android5.0把扫描方法单独弄成一个对象了

    BluetoothGattCharacteristic characteristic;
    BluetoothGattCharacteristic characteristicHB;

    public static final int bleDataLen = 12;      //    特征值的长度
    private byte[] dataReceive;                 //    接收数据缓存区
    private byte[] dataTrans;                   //    发送数据缓存区
    private byte[] dataHeartBeats;              //    心跳包的缓存区
    private int HBcount = 0;                      //    心跳包的计数
    private int RssiValue = 0;                    //    RSSI

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private static final int STATE_NOT_READY_FOR_SERVICE = 0;
    private static final int STATE_RECEIVE_COPY = 1;
    private static final int STATE_RECEIVE_NONE = 2;
    private static boolean isReadyForNext = false;
    private int mConnectionState = STATE_DISCONNECTED;
    private boolean isHBSending = false;                //是否在发送
    private boolean isDataSending = false;
    private boolean isConnectPermit = false;

    // 描述扫描蓝牙的状态
    private boolean mScanning;
    private int mRssi;


    //private final String AIMADDRESS = "F4:5E:AB:B9:58:80";//1号 白色平板
    // private final String AIMADDRESS="50:65:83:86:C6:33";//这个参数是车上用的平板 2号
    //private final static String AIMADDRESS = "98:7B:F3:60:C7:1C";//测试版
    //private final String AIMADDRESS="F4:5E:AB:B9:5A:03";// //手机
    //private final String AIMADDRESS="F4:5E:AB:B9:59:77";//手机
    // private final String AIMADDRESS="98:7B:F3:60:C7:01";//
    private final String AIMADDRESS = "90:59:AF:0E:60:1F";//
    private final static UUID[] aimUUID = {UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb")};
    private final static UUID aimServiceUUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    private final static UUID aimChar6UUID = UUID.fromString("0000fff6-0000-1000-8000-00805f9b34fb");
    private final static UUID aimChar7UUID = UUID.fromString("0000fff7-0000-1000-8000-00805f9b34fb");


    private int state1;
    private int state2;
    private int state3;
    private int state4;
    private int state5;
    private int state6;

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
//      5.0及之前的版本
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
//            if (device.getAddress().equals(AIMADDRESS) && mScanning) {
//                // TODO Auto-generated method stub
//                Log.d("bletrack", "RIGHT DEVICE");
//                mScanning = false;
//                mBluetoothDevice = device;
//                mRssi = rssi;
//                scanLeDevice(false);
//                connect(AIMADDRESS);
//            }
//        }
//    };

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            scanLeDevice(false);//扫描到之后停止扫描
            // callbackType：确定这个回调是如何触发的
            // result：包括4.3版本的蓝牙信息，信号强度rssi，和广播数据scanRecord
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            // 批量回调，一般不推荐使用，使用上面那个会更灵活
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            // 扫描失败，并且失败原因
            Log.d("bletrack", "扫描失败");
        }
    };

    // 连接远程蓝牙
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w("bletrack",
                    "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        scanLeDevice(false);

        // Previously connected device. Try to reconnect.
        if (mBluetoothDeviceAddress != null
                && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d("bletrack", "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect())//连接蓝牙，其实就是调用BluetoothGatt的连接方法
            {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }


        /* 获取远端的蓝牙设备 */
        if (mBluetoothDevice == null) {
            mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(address);
            Log.d("bletrack", "create a bluetoothdevice");
        }
        if (mBluetoothDevice == null) {
            Log.w("bletrack", "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the
        // autoConnect
        // parameter to false.
        /* 调用device中的connectGatt连接到远程设备 */
        isConnectPermit = false;//创建mBluetoothGatt时会进入mGattCallback
        mBluetoothGatt = mBluetoothDevice.connectGatt(this, false, mGattCallback);
        Log.d("bletrack", "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }


    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w("bletrack", "BluetoothAdapter not initialized");
            return;
        }
        isConnectPermit = true;
        mBluetoothGatt.disconnect();
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
                        Log.d("bletrack", "ble connected");
                        if (gatt.getDevice().getAddress().equals(AIMADDRESS)) {
                            Log.d("bletrack", "ble connected");
                            if (mBluetoothDevice.getName() != null && mBluetoothDevice.getAddress() != null) {
                                Log.d("bletrack", mBluetoothDevice.getName());
                                Log.d("bletrack", mBluetoothDevice.getAddress());
                            }
                            isReadyForNext = false;                           //得到特征值之后才能准备好
                            mBluetoothGatt.discoverServices();              //去发现服务
                        } else {                                              //地址不正确，断开连接
                            Log.d("bletrack", "ble devicce err");
                            gatt.disconnect();
                        }
                        isConnectPermit = true;
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:               //断开连接
                        mConnectionState = STATE_DISCONNECTED;
//                    加上会变快
                        if (gatt != null)                                      //先把资源释放，网上搜不释放会有问题
                        {
                            Log.d("bletrack", "gatt release");
                            // close();
                        }
                        Log.d("bletrack", "ble disconnected");
                        //秒连速度之快已经让车上蓝牙检测不到断开了
                        if (isConnectPermit)
                            connect(AIMADDRESS);                                 //适配器发送扫描
                        //scanLeDevice(true);
                        isReadyForNext = false;
                        break;
                    default:                                                //正在连接等等
                        isReadyForNext = false;
                        break;
                }
            } else {
                Log.e("mGattCallback", "gatt  回调失败");
                disconnect();
            }
        }

        //            当新服务被发现，进这个回调
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//                如果特征值和描述被更新
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("bletrack", "ble gatt service success");
                isReadyForNext = true;
//                    把这个服务赋
                mGATTService = mBluetoothGatt.getService(aimServiceUUID);
//                    把这个特征值赋值
                characteristic = mGATTService.getCharacteristic(aimChar6UUID);
                characteristicHB = mGATTService.getCharacteristic(aimChar7UUID);
//                    打开通知开关
                enableNotification(mBluetoothGatt, aimServiceUUID, aimChar6UUID);
                enableNotification(mBluetoothGatt, aimServiceUUID, aimChar7UUID);
            } else {
                isReadyForNext = false;
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
            if (status == mBluetoothGatt.GATT_SUCCESS) {
                String log_out = new String();
                for (int i = 0; i < bleDataLen; i++) {
//                    为了log的时候好观察，加了个\t
                    log_out += String.valueOf((int) characteristic.getValue()[i]) + '\t';
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
                    log_out += String.valueOf((int) temp[i]) + '\t';
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

                } //else
                Log.d("ACHB6", "write: " + log_out);
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

        /* 读描述值*/
        @Override
        public void onDescriptorRead(BluetoothGatt gatt,
                                     BluetoothGattDescriptor descriptor, int status) {
            // TODO Auto-generated method stub
            // super.onDescriptorRead(gatt, descriptor, status);
            Log.w("bletrack", "----onDescriptorRead status: " + status);
            byte[] desc = descriptor.getValue();
            if (desc != null) {
                Log.w("bletrack", "----onDescriptorRead value: " + new String(desc));
            }
        }

        /*写描述值*/
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor, int status) {
            // TODO Auto-generated method stub
            // super.onDescriptorWrite(gatt, descriptor, status);
            // Log.w("bletrack", "--onDescriptorWrite--: " + status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            // TODO Auto-generated method stub
            // super.onReliableWriteCompleted(gatt, status);
            Log.w("bletrack", "--onReliableWriteCompleted--: " + status);
        }

    };


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


    @Override
    public IBinder onBind(Intent intent) {
        Log.d("servicetrack", getClass().getSimpleName() + "onbind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        Log.d("servicetrack", getClass().getSimpleName() + "onUnbind");
        return super.onUnbind(intent);
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }


    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w("bletrack", "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);

    }

    // 写入特征值
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w("bletrack", "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.writeCharacteristic(characteristic);

    }


    public void getCharacteristicDescriptor(BluetoothGattDescriptor descriptor) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w("bletrack", "BluetoothAdapter not initialized");
            return;
        }

        mBluetoothGatt.readDescriptor(descriptor);
    }


    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null)
            return null;
        return mBluetoothGatt.getServices();

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

        ble_init();

        //scanLeDevice(true);

        connect(AIMADDRESS);

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
                }
                if (mBluetoothGatt != null) {
                    if (isReadyForNext) {
                        if (characteristicHB != null) {
                            characteristicHB.setValue(heartBeat);
                            if (!isDataSending) {
                                mBluetoothGatt.writeCharacteristic(characteristicHB);
                                isHBSending = true;
                                Log.d("ACHB", "HeartBeats send");
                            }
                        }
                        if (HBcount == lastHBcount && (!isDataSending))
                            errCount++;
                        else
                            errCount = 0;
                        lastHBcount = HBcount;
                        if (errCount >= 15) {
                            Log.e("bletrack", "HeartBeats disconnect");
                            isReadyForNext = false;
                            disconnect();
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
            if (characteristic != null && mBluetoothGatt != null) {
//                设置值
                characteristic.setValue(dataTrans);
//                发送
                mBluetoothGatt.writeCharacteristic(characteristic);
            }
            wifiSend(dataTrans);

            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
//                  没有发送成功
                    if (!checkSendOk()) {
                        isBusy = true;
                        if (characteristic != null && mBluetoothGatt != null) {
                            characteristic.setValue(dataTrans);
                            //  if(!isHBSending) {
                            isDataSending = true;
                            mBluetoothGatt.writeCharacteristic(characteristic);
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
            if (!isReadyForNext) {
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
            return isReadyForNext;
        }

        //        读取蓝牙强度
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        disconnect();

        return flags;
    }

    @Override
    public void onDestroy() {
        Log.d("servicetrack", "Ble Service onDestroy");
        super.onDestroy();
        close();
    }

}




































//   final Handler handler=new Handler();
//
//        Runnable runnable1=new Runnable() {
//            private int state1last;
//            private int state2last;
//            private int state3last;
//            private int state4last;
//            private int state5last;
//            private int state6last;
//            @Override
//            public void run() {
//                state1=mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.GATT);
//                state2=mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.GATT_SERVER);
//                state3=mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEALTH);
//                state4=mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET);
//                state5=mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP);
//                state6=mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.SAP);
//                if(state1last!=state1||state2!=state2last||state3last!=state3||state4last!=state4||state5last!=state5||state6last!=state6)
//                {
//                    Log.d("state2.0","state1  "+String.valueOf(state1));
//                    Log.d("state2.0","state2  "+String.valueOf(state2));
//                    Log.d("state2.0","state3  "+String.valueOf(state3));
//                    Log.d("state2.0","state4  "+String.valueOf(state4));
//                    Log.d("state2.0","state5  "+String.valueOf(state5));
//                    Log.d("state2.0","state6  "+String.valueOf(state6));
//                }
//                state1last=state1;
//                state2last=state2;
//                state3last=state3;
//                state4last=state4;
//                state5last=state5;
//                state6last=state6;
//                handler.postDelayed(this,300);
//            }
//        };
//        handler.post(runnable1);











