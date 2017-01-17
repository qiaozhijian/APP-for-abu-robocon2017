package com.action.app.actionctr.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Parcelable;
import android.util.Log;

import java.util.List;

/**
 * Created by 56390 on 2017/1/15.
 */

public class wifiBroadcastReceiver extends BroadcastReceiver {
    private WifiManager manager;
    private final String SSID;
    private final String prekey;
    private boolean status=false;
    wifiBroadcastReceiver(WifiManager wifiManager,String ssid,String key){
        manager=wifiManager;
        SSID=ssid;
        prekey =key;
    }

    public boolean checkOk(){
        return status;
    }

    @Override
    public void onReceive(Context context, Intent intent){

        if(intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
            switch (manager.getWifiState()) {
                case WifiManager.WIFI_STATE_ENABLED:
                    manager.startScan();
                    Log.d("wifi","wifi state change -> enabled");
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    manager.setWifiEnabled(true);
                    Log.d("wifi","wifi state change -> disabled");
                    break;
                case WifiManager.WIFI_STATE_UNKNOWN:
                    Log.d("wifi","wifi state change -> unknown");
                    Log.e("wifi","wifi state err");
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    Log.d("wifi","wifi state change -> enabling");
                    break;
            }
        }
        if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
            Parcelable parcelable=intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if(parcelable!=null){
                NetworkInfo.State state=((NetworkInfo)parcelable).getState();
                switch (state){
                    case CONNECTED:
                        if(manager.getConnectionInfo().getSSID().equals("\""+SSID+"\"")){
                            status=true;
                        }
                        else {
                            manager.disconnect();
                            status=false;
                        }
                        break;
                    case DISCONNECTED:
                        status=false;
                        break;
                    case DISCONNECTING:
                        status=false;
                        break;
                    case SUSPENDED:
                        status=false;
                        break;
                }
            }
        }
        if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)){
            List<ScanResult> scanResultList=manager.getScanResults();

            if(!scanResultList.isEmpty()&&scanResultList!=null)
            for (ScanResult device:scanResultList){
                Log.d("wifi","scan result SSID:"+device.SSID);
                if(device.SSID.equals(SSID)){
                    WifiConfiguration configuration=new WifiConfiguration();
                    List<WifiConfiguration> wifiConfigurationList;
                    configuration.SSID="\""+device.SSID+"\"";
                    configuration.preSharedKey="\""+ prekey +"\"";
                    configuration.status=WifiConfiguration.Status.ENABLED;
                    wifiConfigurationList=manager.getConfiguredNetworks();
                    for(WifiConfiguration config:wifiConfigurationList){
                        if(config.SSID.equals(configuration.SSID)){
                            manager.removeNetwork(config.networkId);
                        }
                    }
                    int wifiId;
                    wifiId=manager.addNetwork(configuration);
                    manager.enableNetwork(wifiId,true);
                }
            }
        }

    }

}
