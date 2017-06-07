package com.action.app.actionctr2.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Parcelable;
import android.util.Log;

/**
 * Created by 56390 on 2017/1/15.
 */

public class wifiBroadcastReceiver extends BroadcastReceiver {
    private WifiManager manager;
    private boolean status=false;
    wifiBroadcastReceiver(WifiManager wifiManager){
        manager=wifiManager;
        if(manager.getConnectionInfo()!=null)
        {
            status=true;
        }
    }

    public boolean checkOk(){
        return status;
    }
    @Override
    public void onReceive(Context context, Intent intent){
        if(intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
            switch (manager.getWifiState()) {
                case WifiManager.WIFI_STATE_ENABLED:
                    if(manager.getConnectionInfo().getSSID()!=null){
                        status=true;
                        Log.d("wifi","wifi has connected");
                    }
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    status=false;
                    Log.d("wifi","wifi state change -> disabled");
                    break;
                case WifiManager.WIFI_STATE_UNKNOWN:
                    status=false;
                    Log.d("wifi","wifi state change -> unknown");
                    Log.e("wifi","wifi state err");
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    status=false;
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
                        if(manager.getConnectionInfo()!=null){
                            Log.d("wifi","wifi connected");
                            status=true;
                        }
                        else {
                            manager.disconnect();
                            status=false;
                        }
                        break;
                    case DISCONNECTED:
                        Log.d("wifi","wifi disconnected");
                        status=false;
                        break;
                    case DISCONNECTING:
                        Log.d("wifi","wifi disconnecting");
                        status=false;
                        break;
                    case SUSPENDED:
                        status=false;
                        break;
                    case CONNECTING:
                        Log.d("wifi","wifi connecting");
                        break;
                }
            }
        }

    }

}
