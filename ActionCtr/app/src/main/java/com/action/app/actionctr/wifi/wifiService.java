package com.action.app.actionctr.wifi;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 56390 on 2017/1/15.
 */

public class wifiService extends Service {
    private wifiServiceBinder binder=new wifiServiceBinder();
    private WifiManager wifiManager;

    private wifiBroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;

    private final int port=8086;

    private final String SSID="ATK-ESP8266";
    private final String presharedKey="12345678";
    private ServerSocket server;

    private Runnable runnable;
    private Runnable dataReceiveRunnable;
    private Thread runnableThread;
    private Thread dataReceiveThread;

    private BufferedReader in;

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
        destroyFlag=false;
        Log.d("wifi","wifi service start onCreate");
        wifiDataList=new ArrayList<String>();
        wifiManager=(WifiManager) getSystemService(Context.WIFI_SERVICE);
        broadcastReceiver=new wifiBroadcastReceiver(wifiManager,SSID,presharedKey);
        intentFilter=new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(broadcastReceiver,intentFilter);

        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
        }
        else if(!wifiManager.getConnectionInfo().getSSID().equals("\""+SSID+"\"")){
            wifiManager.setWifiEnabled(false);
        }

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
                    dataReceiveThread.start();
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
                while (true) {
                    String line;
                    Log.d("wifi","socket start dataReceive");
                    try {
                        in=new BufferedReader(new InputStreamReader(server.accept().getInputStream()));
                        while ((line=in.readLine())!=null&&!destroyFlag){
                            wifiDataList.add(line);
                            //while (!dataReceiveThread.isInterrupted());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e){
                        e.printStackTrace();
                        Log.e("wifi","Exception： "+Log.getStackTraceString(e));
                    }
                    if (destroyFlag){
                        try {
                            Log.d("wifi","data receive thread destroy");
                            in.close();
                            server.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.d("wifi","dataReceiveThread close successfully");
                    }

                }
            }
        };
        runnableThread=new Thread(runnable);
        dataReceiveThread=new Thread(dataReceiveRunnable);
        runnableThread.start();
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