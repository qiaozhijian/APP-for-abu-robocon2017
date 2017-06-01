package com.action.app.actionctr.ble;

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
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.action.app.actionctr.BeginActivity;
import com.action.app.actionctr.wifi.wifiService;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class BleService extends Service {

//加载C++库
    static {
        System.loadLibrary("native-lib");
    }
    //是否在发送
    private boolean isSending=false;

//  管理器，使能，失能，获得适配器
    private BluetoothManager bleManager;
//  控制蓝牙扫描，读取连接设备
    private BluetoothAdapter bleAdapter;
//  蓝牙设备，可以发起连接
    private ArrayList<BluetoothDevice> devicesList;
//    返回扫描到的设备
    private ScanCallback mLeScanCallback;
//    GATT的回调，判断连接状态的变化，服务的读写通知回调
    private BluetoothGattCallback mGattCallback;
//    GATT实例，可以拿来断开连接，扫描服务
    private BluetoothGatt mBluetoothGatt;
//    特征值，读，写
    private BluetoothGattCharacteristic characteristic;
//    心跳包的特征值
    private BluetoothGattCharacteristic characteristicHB;
//    服务，读出特征值
    private BluetoothGattService        gattService;
//    判断蓝牙正不正常，是否可以进行下一次发数
    private boolean isReadyForNext=false;
//    RSSI
    private int RssiValue=0;
//特征值的长度
    public static final int bleDataLen=12;
//    private final String address="F4:5E:AB:B9:59:77";//这个参数是车上用的平板 1号
//    private final String address="F4:5E:AB:B9:58:80";//2号 白色平板
//    从机地址
    private final String address="F4:5E:AB:B9:5A:03";// //3号

//    接收数据缓存区
    private byte[] dataReceive;
//    发送数据缓存区
    private byte[] dataTrans;
//    心跳包的缓存区
    private byte[] dataHeartBeats;

//    心跳包的计数
    private int HBcount=0;

//    通过WiFi发数
    private void wifiSend(byte[] data){
        OutputStream out=wifiService.getOutputStream();
        if(out!=null) {
                try {
                out.write(data);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("wifi", "ble Exception: " + Log.getStackTraceString(e));
            }
        }
    }

//    蓝牙发数  binder跟所有涉及到蓝牙的activity通信
    private myBleBand dataSend=new myBleBand();
    public class myBleBand extends Binder {
//        表明现在是第几条命令
        byte count=0;
//        前一次发数的检查是否完成
        boolean isBusy=false;
//        定时检查前面发送有没有完成，如果没有则重发
        Handler handler=new Handler();

        public void send(byte[] data){
            isSending=true;
//            检查数据长度是否正确
            if(data.length!=bleDataLen){
                Log.e("version err","length of senddata is not equal to require");
            }
//            放到缓存区里
            dataTrans=data;
//            把最后一个字节当做计数
            dataTrans[bleDataLen-1]=count;
//            表明这边是第几个命令，防止重复
            if(count<100)
                count++;
            else
                count=0;
//            如果GATT有定义
            if(characteristic!=null&&mBluetoothGatt!=null) {
//                设置值
                characteristic.setValue(dataTrans);
//                发送
                mBluetoothGatt.writeCharacteristic(characteristic);
            }
            wifiSend(dataTrans);

            final Runnable runnable=new Runnable() {
                @Override
                public void run() {
//                  没有发送成功
                    if(!checkSendOk()) {
                        isBusy=true;
                        if(characteristic!=null&&mBluetoothGatt!=null) {
                            characteristic.setValue(dataTrans);
                            mBluetoothGatt.writeCharacteristic(characteristic);
                        }
//                        100ms之后执行runnable
                        handler.postDelayed(this,100);
                    }
//                    发送成功
                    else{
                        isBusy=false;
                    }
                }
            };
//           第一次进来的时候不会执行run，只有postDelayed触发时才会
//·          第二次的时候发现第一次还是没有发送成功就不再运行一遍
            if(!isBusy)
                handler.postDelayed(runnable,100);
            isSending=false;
        }
//        获取心跳包数据
        public byte[] getHeartBeats() {
            return dataHeartBeats;
        }
        public boolean checkSendOk(){
//            蓝牙没有准备好，就一直不重发
            if(!isReadyForNext) {
                return true;
            }
//            如果二者相等则返回true
            if(Arrays.equals(dataReceive,dataTrans)) {
                return true;
            }
//            判断数据是否匹配
            if(dataReceive!=null&&dataTrans!=null) {
                int i;
//                如果i==9时不相等，执行break,此时i不++，i依然不满足=10条件
                for(i=0;i<10;i++) {
                   if(dataReceive[i]!=dataTrans[i])
                       break;
                }
                if(i == 10)
                    Log.e("ble","communicate unstable");
                    return true;
            }
            return false;
        }
//        连接是否完成
        public boolean isReady(){
            return isReadyForNext;
        }
//        读取蓝牙强度
        public int readRssi(){
            if(isReadyForNext) {
                mBluetoothGatt.readRemoteRssi();
                return RssiValue;
            }
            else{
                return 0;
            }
        }
    }
//    dataSend(binder)  通信活动与服务 获得连接状态 发送数据
    @Override
    public IBinder onBind(Intent intent) {
        return dataSend;
    }

    @Override
    public void onCreate(){
        super.onCreate();

//        通知窗口
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(android.R.drawable.btn_dialog);
        builder.setContentTitle("ActionCtrBle");
        builder.setContentText("为了保证Ble的长期不被系统干掉");
        Intent intent = new Intent(this, BeginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        startForeground(1, notification);


        Log.d("Ble","Ble Service onCreate");

//        获取管理器
        bleManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//        获取适配器
        bleAdapter= bleManager.getAdapter();
//        获取设备列表
        devicesList=new ArrayList<>();

        mGattCallback=new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
//                指示GATT的状态
                if(status==BluetoothGatt.GATT_SUCCESS){
                    Log.d("Ble","connectionStateChange gatt success");
                }
                else {
                    Log.e("Ble","connectionStateChange gatt fail");
                }
//               指示连接状态
                switch (newState) {
//                    如果连接状态正常
                    case BluetoothProfile.STATE_CONNECTED:
//                        如果地址正确
                        if(gatt.getDevice().getAddress().equals(address)){
                            Log.d("Ble","ble connected");
//                            得到特征值之后才能准备好
                            isReadyForNext=false;
//                            去发现服务
                            mBluetoothGatt.discoverServices();
                        }
//                        地址不正确，断开连接
                        else {
                            Log.d("Ble","ble devicce err");
                            gatt.disconnect();
                        }
                        break;
//                    断开连接
                    case BluetoothProfile.STATE_DISCONNECTED:
//                        先把资源释放，网上搜不释放会有问题
                        if(gatt!=null)
                            gatt.close();
//                        适配器发送扫描
                        bleAdapter.getBluetoothLeScanner().startScan(mLeScanCallback);
                        isReadyForNext=false;
                        Log.d("Ble","ble disconnected");
                        break;
//                    正在连接等等
                    default:
                        isReadyForNext=false;
                        break;
                }
            }
//            当新服务被发现，进这个回调
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt,int status){
//                如果特征值和描述被更新
                if(status==BluetoothGatt.GATT_SUCCESS){
                    Log.d("Ble","ble gatt service success");
                    isReadyForNext=true;
//                    把这个服务赋
                    gattService=mBluetoothGatt.getService(UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb"));
//                    把这个特征值赋值
                    characteristic=gattService.getCharacteristic(UUID.fromString("0000fff6-0000-1000-8000-00805f9b34fb"));
                    characteristicHB=gattService.getCharacteristic(UUID.fromString("0000fff7-0000-1000-8000-00805f9b34fb"));
//                    打开通知开关
                    mBluetoothGatt.setCharacteristicNotification(characteristic,true);
                    mBluetoothGatt.setCharacteristicNotification(characteristicHB,true);
                }
                else {
                    isReadyForNext=false;
                    Log.d("Ble","ble gatt service fail");
                }
            }
//            特征值的值读取结果回调
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                String log_out=new String();
                for (int i=0;i<bleDataLen;i++){
//                    为了log的时候好观察，加了个\t
                    log_out+=String.valueOf((int)characteristic.getValue()[i])+'\t';
                }
                Log.d("Ble","read value: "+log_out);
            }
//            特征值改变
            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
                byte[] temp;
                temp=characteristic.getValue();
//                判断是否是心跳包
                if(temp[0]=='A'&&temp[1]=='C'&&temp[2]=='H'&&temp[3]=='B')
                {
                    dataHeartBeats=temp;
                    HBcount++;
                }
                else{
                    dataReceive=temp;
                    String log_out=new String();
                    for (int i=0;i<12;i++){
                        log_out+=String.valueOf((int)dataReceive[i])+'\t';
                    }

                    if(dataReceive.length!=bleDataLen){
                        Log.e("version err","length of receivedata is not equal to require");
                    }
                    Log.d("Ble","notify: "+log_out);
                }
            }
//            写特征值的结果回调
            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){
                String log_out=new String();
                for (int i=0;i<12;i++){
                    log_out+=String.valueOf((int)characteristic.getValue()[i])+'\t';
                }
                Log.d("Ble","write: "+log_out);
            }
//            RSSI的结果获取
            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                RssiValue=rssi;
            }
        };
//        扫描回调
        mLeScanCallback = new ScanCallback()
        {

            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
//                获得扫描到的设备
                BluetoothDevice device=result.getDevice();
//                如果设备表里没有这个设备
                if (!devicesList.contains(device)) {
                    devicesList.add(device);
                    Log.d("Ble", "find device , name= " + device.getName());
                    Log.d("Ble", "device address="+device.getAddress());
//                  判断设备地址是否正确
                    if(device.getAddress().equals(address)){
//                        如果正确则情况设备表
                        devicesList.clear();
//                        device.getAddress();device.getUuids(); 可以获得UUID
//                        停止扫描
                        bleAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
//                        开始连接
                        mBluetoothGatt=device.connectGatt(BleService.this, false, mGattCallback);
                    }
                }
            }
        };
//        开始扫描并注册扫描回调  只执行一次
        bleAdapter.getBluetoothLeScanner().startScan(mLeScanCallback);

        //下面的代码用于发送心跳包
        final Handler handlerHeartBeat=new Handler();
        Runnable runnable=new Runnable() {
            private int errCount=0;
            private int lastHBcount=0;
            @Override
            public void run() {
                byte[] heartBeat=new byte[bleDataLen];
                heartBeat[0]='A';
                heartBeat[1]='C';
                heartBeat[2]='H';
                heartBeat[3]='B';
                if(!isSending){
                    wifiSend(heartBeat);
                }
                if(isReadyForNext){
                    if(characteristicHB!=null) {
                        characteristicHB.setValue(heartBeat);
                        mBluetoothGatt.writeCharacteristic(characteristicHB);
                    }
                    if(HBcount==lastHBcount)
                        errCount++;
                    else
                        errCount=0;
                    lastHBcount=HBcount;
                    if(errCount>=15) {
                        Log.e("Ble","HeartBeats disconnect");
                        isReadyForNext=false;
                        mBluetoothGatt.disconnect();
                        errCount=0;
                    }
                }
                handlerHeartBeat.postDelayed(this,300);
            }
        };
        handlerHeartBeat.postDelayed(runnable,3000);
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
        if(mBluetoothGatt!=null)
            mBluetoothGatt.close();
    }
}
