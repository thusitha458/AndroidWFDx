package com.example.thusitha.wfdmeshserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;


public class WifiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiDirectActivity mActivity;

    public WifiDirectBroadcastReceiver (WifiP2pManager mManager, WifiP2pManager.Channel mChannel, WifiDirectActivity mActivity) {
        super();
        this.mManager = mManager;
        this.mChannel = mChannel;
        this.mActivity = mActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Log.d(WifiDirectActivity.LOG_TAG, "Wifi state enabled");
            } else {
                Log.d(WifiDirectActivity.LOG_TAG, "Wifi state enabled");
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            if (mManager == null) {
                return;
            }
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.getDetailedState().equals(NetworkInfo.DetailedState.CONNECTED)) {
                Log.d(WifiDirectActivity.LOG_TAG, "Connected intent");
                if (!mActivity.isGroupCreated()) {
                    mManager.requestConnectionInfo(mChannel, mActivity.connectionInfoListenerSetup);
                }
            } else if (networkInfo.getDetailedState().equals(NetworkInfo.DetailedState.DISCONNECTED)) {
                Log.d(WifiDirectActivity.LOG_TAG, "Disconnected intent");
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }
}
