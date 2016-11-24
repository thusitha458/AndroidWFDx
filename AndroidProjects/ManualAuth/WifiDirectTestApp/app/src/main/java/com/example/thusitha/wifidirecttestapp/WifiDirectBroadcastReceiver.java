package com.example.thusitha.wifidirecttestapp;


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

    public WifiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       WifiDirectActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Log.d(WifiDirectActivity.LOG_TAG, "wifi is enabled");
            } else {
                Log.d(WifiDirectActivity.LOG_TAG, "wifi is enabled");
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            Log.d(WifiDirectActivity.LOG_TAG, "p2p peers changed");
            if (mManager != null) {
                if (!mActivity.isConnected() && !mActivity.isConnecting()) {
                    mManager.requestPeers(mChannel, mActivity.peerListListener);
                }
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (mManager == null) {
                return;
            }
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.getDetailedState().equals(NetworkInfo.DetailedState.CONNECTED)) {
                Log.d(WifiDirectActivity.LOG_TAG, "p2p connected");
                if (!mActivity.isConnected()) {
                    mManager.requestConnectionInfo(mChannel, mActivity.connectionInfoListener);
                }
            } else if (networkInfo.getDetailedState().equals(NetworkInfo.DetailedState.DISCONNECTED)) {
                Log.d(WifiDirectActivity.LOG_TAG, "p2p disconnected");
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            Log.d(WifiDirectActivity.LOG_TAG, "this device changed");

        }
    }

}
