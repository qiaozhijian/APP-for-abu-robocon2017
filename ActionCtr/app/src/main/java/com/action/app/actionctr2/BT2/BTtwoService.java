package com.action.app.actionctr2.BT2;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.action.app.actionctr2.BeginActivity;
import com.action.app.actionctr2.wifi.wifiService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by Administrator on 2017/7/16/016.
 */

public class BTtwoService extends Service {

    private BluetoothSocket BTSocket;
    private BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();
    public BluetoothDevice mDevice = null;     //蓝牙设备
    private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    static private InputStream is;    //输入流，用来接收蓝牙数据
    static private OutputStream os;

    public String aimName = "Summer2";
    public String aimAddress = "00:15:83:12:01:59";
    private BT2Binder bt2Binder = new BT2Binder();

    public static final int bleDataLen = 12;      //    特征值的长度
    private byte[] dataReceive;                 //    接收数据缓存区
    private byte[] dataTrans;                   //    发送数据缓存区
    private byte[] dataHeartBeats;              //    心跳包的缓存区
    private int HBcount = 0;                      //    心跳包的计数

    private boolean connectState = false;

    private boolean isDataSending = false;

    @Override
    public void onCreate() {
        super.onCreate();

        notification();

        if (BTAdapter.isEnabled() == false) {  //如果蓝牙服务不可用则提示
            Toast.makeText(this, " 请退出打开蓝牙", Toast.LENGTH_LONG).show();
            return;
        }
        // 关闭再进行的服务查找
        if (BTAdapter.isDiscovering()) {
            BTAdapter.cancelDiscovery();
        }
        //并重新开始
        BTAdapter.startDiscovery();

        Log.d("servicetrack", "service onCreate");

        registerBTReceiver();
        //下面的代码用于发送心跳包
        final Handler handlerHeartBeat = new Handler();
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                byte[] heartBeat = new byte[bleDataLen];
                heartBeat[0] = 'A';
                heartBeat[1] = 'C';
                heartBeat[2] = 'H';
                heartBeat[3] = 'B';
                if (!isDataSending) {
                    wifiSend(heartBeat);
                    writeData(heartBeat);
                    Log.d("ACHB", "HeartBeats start");
                }
                else
                {
                    Log.d("ACHB", "HeartBeats stop");
                }

                handlerHeartBeat.postDelayed(this, 300);
            }
        };
//        不同于上面，上面是按键按一次就会执行一次，但是这个是只会在程序启动的时候执行
        handlerHeartBeat.postDelayed(runnable, 3000);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("servicetrack", getClass().getSimpleName() + "onbind");
        return bt2Binder;
    }

    /**
     * 注册广播
     */
    public void registerBTReceiver() {
        // 设置广播信息过滤
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        // 注册广播接收器，接收并处理搜索结果
        registerReceiver(BTReceive, intentFilter);
    }

    /**
     * 广播接收者
     */
    private BroadcastReceiver BTReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            //找到设备通知  ACTION_FOUND,设备已配对通知  ACTION_BOND_STATE_CHANGED
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                扫描一会后扫描不到，要改
                Log.d("bletrack", "device :" + mDevice.getName());
                Log.d("bletrack", "device address: " + mDevice.getAddress());
                // 如果查找到的设备符合，添加到UI上
                if (mDevice.getName().equals(aimName) && mDevice.getAddress().equals(aimAddress)) {
                    if (BTAdapter.isDiscovering()) {
                        //不用停止发现，连接过程好像会停止发现
                        // BTAdapter.cancelDiscovery();
                        Log.d("bletrack", "停止发现");
                    }
                    Log.d("bletrack", "开始连接设备");
                    bondBT(mDevice.getName());
                    connectState = true;
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                // 获取蓝牙设备的连接状态
                int connectState = mDevice.getBondState();
                // 已配对
                if (connectState == BluetoothDevice.BOND_BONDED) {
                    try {
                        clientThread clientConnectThread = new clientThread();
                        clientConnectThread.start();
                        Log.d("bletrack", "已配对");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d("bletrack", "ACTION_DISCOVERY_STARTED");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d("bletrack", "ACTION_DISCOVERY_FINISHED");
            } else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                Log.d("bletrack", BluetoothAdapter.EXTRA_CONNECTION_STATE);

            }
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        //注销广播
        unregisterReceiver(BTReceive);
        try {
            BTSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 添加找到的BT
     */
    private void addBT() {
        bondBT(mDevice.getName());
    }

    /**
     * 绑定蓝牙
     *
     * @param
     */
    private void bondBT(String deviceName) {
        // 搜索蓝牙设备的过程占用资源比较多，一旦找到需要连接的设备后需要及时关闭搜索
        BTAdapter.cancelDiscovery();
        // 获取蓝牙设备的连接状态
        int connectState = mDevice.getBondState();

        switch (connectState) {
            // 未配对
            case BluetoothDevice.BOND_NONE:
                try {
                    Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                    createBondMethod.invoke(mDevice);
                    Log.d("bletrack", "未配对，先配对");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            // 已配对
            case BluetoothDevice.BOND_BONDED:
                try {
//                    已配对，执行连接
                    Log.d("bletrack", "已配对，直接连接");
                    clientThread clientConnectThread = new clientThread();
                    clientConnectThread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

        }
    }

    /**
     * 开启客户端
     */
    private class clientThread extends Thread {
        public void run() {
            try {
                //创建一个Socket连接：只需要服务器在注册时的UUID号
                BTSocket = mDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                //连接
                BTSocket.connect();
                //启动接受数据
                readThread mreadThread = new readThread();
                mreadThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    public void writeData(byte[] bos) {
        int i = 0;
        OutputStream os = null;
        Log.d("shantui", "write1");
        try {
            Log.d("shantui", "write2");
            if (BTSocket != null)
                os = BTSocket.getOutputStream();
            else
                Log.d("shantui", "write3");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("shantui", "os 不能正常创建");
        }
        if (os != null) {
            try {
                Log.d("shantui", "write4");
                os.write(bos);
                Log.d("shantui", "write5");
                os.flush();
                Log.d("shantui", "write6");
            } catch (IOException e) {
                Log.d("shantui", "write error");
            }
        } else
            Log.d("shantui", "os = null");

    }

    /**
     * 读取数据
     */
    private class readThread extends Thread {
        public void run() {
            byte[] buffer = new byte[1024];
            String log_out = new String();
            int bytes;
            InputStream is = null;
            try {
                is = BTSocket.getInputStream();
            } catch (IOException e1) {
                Log.d("bletrack", "接收数据失败");
                e1.printStackTrace();
            }
            while (true) {
                try {
                    if ((bytes = is.read(buffer)) > 0) {
                        byte[] buf_data = new byte[bytes];
                        for (int i = 0; i < bytes; i++) {
                            buf_data[i] = buffer[i];
                            log_out += String.valueOf((char) (buf_data[i])) + '\t';
                        }
                        Log.d("bletrack", "input:" + log_out);
                    }
                } catch (IOException e) {
                    try {
                        is.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    break;
                }
            }
        }
    }


    public class BT2Binder extends Binder {
        private Handler handler = new Handler();
        byte count = 0;

        public void sendbyBT2(byte[] data) {
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

            isDataSending=true;

            wifiSend(dataTrans);

            writeData(dataTrans);

            isDataSending=false;

//            final Runnable runnable = new Runnable() {
//                @Override
//                public void run() {
////                  没有发送成功
//                    if (!checkSendOk()) {
//                        writeData(dataTrans);
////                        100ms之后执行runnable
//                        handler.postDelayed(this, 100);
//                    }
//                }
//            };
//            handler.postDelayed(runnable, 100);
        }

        boolean checkSendOk() {
            return true;
        }

        boolean isConnect() {
            return connectState;
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

}
