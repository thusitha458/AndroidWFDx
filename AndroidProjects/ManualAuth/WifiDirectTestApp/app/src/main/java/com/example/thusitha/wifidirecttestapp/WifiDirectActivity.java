package com.example.thusitha.wifidirecttestapp;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class WifiDirectActivity extends AppCompatActivity implements ScreenUpdater, ClientListManager {

    public static final int SERVER_PORT = 8877;
    public static String LOG_TAG = "Logs";

    public WifiP2pManager.PeerListListener peerListListener;
    public WifiP2pManager.ConnectionInfoListener connectionInfoListener;

    private ArrayList<WifiP2pDevice> peerList = new ArrayList<>();

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiDirectBroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;

    private WifiManager wifiManager;
    private WifiManager.WifiLock wifiLock;

    private boolean isConnected = false;
    private boolean isConnecting = false;
    private boolean isGroupOwner = false;

    private String groupOwnerAddress;

    private TextView textView;

    TcpMessageListener messageListener;
    private int count = 0;

    private String currentClientAddress = null;

    // LoggerExp1
    private FileLogger fileLogger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_direct);

        //
        fileLogger = (new FileLoggerCreator()).getFileLogger(FileLoggerCreator.Experiments.EXPERIMENT_1);
        fileLogger.createLogFile();
        fileLogger.appendLog("test1");
        fileLogger.appendLog("test2");
        //

        textView = (TextView) findViewById(R.id.status_view);
        textView.setMovementMethod(new ScrollingMovementMethod());

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiLock = wifiManager.createWifiLock("wifi_p2p_lock");
        wifiLock.setReferenceCounted(false);
        wifiLock.acquire();

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this);

        implementPeerListListener();
        implementConnectionInfoListener();

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        if (wifiLock != null) {
            wifiLock.release();
        }
        if (messageListener != null) {
            messageListener.interrupt();
        }
        super.onDestroy();
    }

    private void startPeerDiscovery() {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // success
            }

            @Override
            public void onFailure(int reason) {
                Log.d(LOG_TAG, "Peer discovery failed: " + reason);
            }
        });
    }

    private void implementPeerListListener() {
        peerListListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peers) {
                if (!isConnecting()) {
                    peerList.clear();
                    peerList.addAll(peers.getDeviceList());
                    if (peerList.size() == 0) {
                        Log.d(LOG_TAG, "No peers found");
                    } else {
                        setConnecting(true);
                        connectToDevice(selectPeer());
                    }
                }
            }
        };
    }

    private WifiP2pDevice selectPeer() {
        if (peerList.size() == 0) {
            return null;
        }
        return peerList.get(0);
    }

    private void connectToDevice(WifiP2pDevice device) {

        if (device == null) {
            return;
        }

        textView.append("Connecting to " + device.deviceName + '\n');

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

//        config.groupOwnerIntent = 15;

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // success
            }

            @Override
            public void onFailure(int reason) {
                Log.d(LOG_TAG, "connect to peer failed: " + reason);
            }
        });

        setConnecting(false);
    }

    private void implementConnectionInfoListener() {
        connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(final WifiP2pInfo info) {

                groupOwnerAddress = info.groupOwnerAddress.getHostAddress();

                if (info.groupFormed && info.isGroupOwner) {
                    setGroupOwner(true);
                    setConnected(true);
                    textView.append("Connected, GO" + '\n');
                    count = 0;
                    messageListener = new TcpMessageListener(WifiDirectActivity.this, WifiDirectActivity.this, SERVER_PORT);
                    messageListener.start();
                } else if (info.groupFormed) {
                    setGroupOwner(false);
                    setConnected(true);
                    textView.append("Connected, Not a GO" + '\n');
                    count = 0;
                    messageListener = new TcpMessageListener(WifiDirectActivity.this, WifiDirectActivity.this, SERVER_PORT);
                    messageListener.start();
                }
            }
        };
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public boolean isGroupOwner() {
        return isGroupOwner;
    }

    public void setGroupOwner(boolean groupOwner) {
        isGroupOwner = groupOwner;
    }

    public void onClickConnect(View view) {
        if (!isConnected()) {
            textView.append("Started peer discovery" + '\n');
            startPeerDiscovery();
        }
    }

    public void onClickSendMsg(View view) {
        if (isConnected()) {
            String receiverAddress;
            String message;
            if (isGroupOwner()) {
                if (currentClientAddress == null) {
                    receiverAddress = "192.168.49.255"; //broadcast address, this will be overridden
                } else {
                    receiverAddress = currentClientAddress;
                }
                message = "Message from server: ";
            } else {
                receiverAddress = groupOwnerAddress;
                message = "Message from client: ";
            }
            TcpMessageSender sender = new TcpMessageSender(message + (++count),
                    WifiDirectActivity.this, receiverAddress, SERVER_PORT);
            sender.execute();
        }
    }

    public boolean isConnecting() {
        return isConnecting;
    }

    public void setConnecting(boolean connecting) {
        isConnecting = connecting;
    }

    @Override
    public synchronized void displayMessage(boolean isReceived, String message) {
        if (isReceived) {
            message = "Got: " + message;
        } else {
            message = "Sent: " + message;
        }
        final String finalMessage = message;
        WifiDirectActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.append(finalMessage + '\n');
            }
        });
    }

    @Override
    public synchronized void updateCurrentClient(String ip) {
        currentClientAddress = ip;
    }
}
