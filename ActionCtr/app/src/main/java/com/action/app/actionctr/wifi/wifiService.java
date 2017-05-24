package com.action.app.actionctr.wifi;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompatExtras;
import android.util.Log;


import com.action.app.actionctr.BeginActivity;
import com.action.app.actionctr.BleConnectActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Created by 56390 on 2017/1/15.
 */

public class wifiService extends Service {
    private wifiServiceBinder binder=new wifiServiceBinder();
    private WifiManager wifiManager;

    private wifiBroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;

    private final int port=8080;
    private ServerSocket server;

    private Runnable runnable;
    private Runnable dataReceiveRunnable;

    private InputStream in;

    private ArrayList<String> wifiDataList;

    private boolean destroyFlag=false;

    public class wifiServiceBinder extends Binder{
        public ArrayList<String> getWifiStringDataList(){
            return wifiDataList;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    @Override
    public void onCreate() {
        super.onCreate();


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(android.R.drawable.btn_dialog);
        builder.setContentTitle("ActionCtrWifi");
        builder.setContentText("为了保证wifi的长期不被系统干掉");
        Intent intent = new Intent(this, BeginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        startForeground(2, notification);

        destroyFlag=false;
        Log.d("wifi","wifi service start onCreate");
        wifiDataList=new ArrayList<String>();
        wifiManager=(WifiManager) getSystemService(Context.WIFI_SERVICE);
        broadcastReceiver=new wifiBroadcastReceiver(wifiManager);
        intentFilter=new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(broadcastReceiver,intentFilter);

//        if(!wifiManager.isWifiEnabled()){
//            wifiManager.setWifiEnabled(true);
//        }

        runnable=new Runnable() {
            @Override
            public void run() {
                while (!broadcastReceiver.checkOk()){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Log.d("wifi","socket connecting");
                    server=new ServerSocket(port);
                    server.setSoTimeout(3000);
                    Log.d("wifi","socket connect successfully");
                    new Thread(dataReceiveRunnable).start();
                }catch (UnknownHostException e) {
                    Log.e("wifi","socket init unknownhost err");
                    e.printStackTrace();
                    Log.e("wife","Exception: "+Log.getStackTraceString(e));
                } catch (IOException e) {
                    Log.e("wifi","socket init io err");
                    e.printStackTrace();
                    Log.e("wife","Exception: "+Log.getStackTraceString(e));
                }catch (Exception e) {
                    Log.e("wifi","socket init connect err");
                    e.printStackTrace();
                    Log.e("wifi","Exception: "+Log.getStackTraceString(e));
                }
            }
        };
        dataReceiveRunnable=new Runnable() {
            @Override
            public void run() {
                try {
                    in=server.accept().getInputStream();
                    Log.d("wifi","socket start dataReceive");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e){
                    e.printStackTrace();
                    Log.e("wifi","Exception： "+Log.getStackTraceString(e));
                }
                String line=new String("");
                while (!destroyFlag&&broadcastReceiver.checkOk()&&in!=null) {
                    char    dataRead;
                    try {
                        if(in.available()>=1){
                            dataRead=(char)in.read();
                            line+=dataRead;
                            if(dataRead=='\n'){
                                wifiDataList.add(line.substring(0,line.length()-1));
                                line="";
                            }
                        }
                    }  catch (Exception e){
                        e.printStackTrace();
                        Log.e("wifi","Exception： "+Log.getStackTraceString(e));
                    }
                }
                try {
                    Log.d("wifi","data receive thread destroy or disconnect");
                    if(in!=null){
                        in.close();
                    }
                    if(server!=null){
                        server.close();
                        Log.d("wifi","dataReceiveThread close successfully");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if((!broadcastReceiver.checkOk()&&!destroyFlag)||in==null){
                    new Thread(runnable).start();
                }
            }
        };
        new Thread(runnable).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("wifi","wifi service start onStartCommand");
        return super.onStartCommand(intent,flags,startId);
    }
    @Override
    public void onDestroy(){
        Log.d("wifi","wifi Service onDestroy");
        unregisterReceiver(broadcastReceiver);
        destroyFlag=true;
        super.onDestroy();

    }
}