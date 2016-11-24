package com.example.thusitha.wfdmeshserver;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class WifiDirectActivity extends AppCompatActivity implements ScreenUpdater, ClientListManager {

    public static final String SERVICE_NAME = "wfd_mesh";
    private static final int SERVER_PORT = 8877;
    public static String LOG_TAG = "Logs";

    public WifiP2pManager.GroupInfoListener groupInfoListenerSetup;
    public WifiP2pManager.ConnectionInfoListener connectionInfoListenerSetup;

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiDirectBroadcastReceiver mReceiver;

    private WifiManager wifiManager;
    private WifiManager.WifiLock wifiLock;

    private IntentFilter mIntentFilter;

    private String ssid;
    private String passphrase;
    private String serverIp;

    private boolean isGroupCreated = false;

    private TextView textView;

    private String currentClientAddress = "192.168.49.255";
    private MessageListener messageListener;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_direct);

        textView = (TextView) findViewById(R.id.status_view);
        textView.setMovementMethod(new ScrollingMovementMethod());

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiLock = wifiManager.createWifiLock("server_lock");
        wifiLock.setReferenceCounted(false);
        wifiLock.acquire();

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this);

        implementConnectionInfoListener();
        implementGroupInfoListener();

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        createSoftAP();
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
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_wifi_direct, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void createSoftAP () {
        mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                if (group != null) {
                    mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                    // success
                                }

                                @Override
                                public void onFailure(int reason) {
                                    Log.d(LOG_TAG, "create group failed: " + reason);
                                }
                            });
                        }

                        @Override
                        public void onFailure(int reason) {
                            Log.d(LOG_TAG, "remove group failed: " + reason);
                        }
                    });
                } else {
                    mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            // success
                        }

                        @Override
                        public void onFailure(int reason) {
                            Log.d(LOG_TAG, "create group failed: " + reason);
                        }
                    });
                }
            }
        });
    }

    private void implementConnectionInfoListener () {
        connectionInfoListenerSetup = new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                if (info.groupFormed) {
                    if (info.isGroupOwner) {
                        serverIp = info.groupOwnerAddress.getHostAddress();
                        mManager.requestGroupInfo(mChannel, groupInfoListenerSetup);
                    }
                } else {
                    Log.d(LOG_TAG, "group didn't form");
                }
            }
        };
    }

    private void implementGroupInfoListener () {
        groupInfoListenerSetup = new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                if (group != null) {
                    ssid = group.getNetworkName();
                    passphrase = group.getPassphrase();
                    setGroupCreated(true);
                    startRegistration();
                    textView.append("Created group..." + '\n');
                    // Start server thread
                    messageListener = new MessageListener(WifiDirectActivity.this, WifiDirectActivity.this, SERVER_PORT);
                    messageListener.start();
                }
            }
        };
    }

    private void startRegistration() {
        Map<String, String> record = new HashMap<>();
        record.put("listenport", String.valueOf(SERVER_PORT));
        record.put("ssid", ssid);
        record.put("psk", passphrase);
        record.put("ip", serverIp);

        textView.append("port: " + String.valueOf(SERVER_PORT) + '\n');
        textView.append("ssid: " + ssid + '\n');
        textView.append("psk: " + passphrase + '\n');
        textView.append("ip: " + serverIp + '\n');

        WifiP2pDnsSdServiceInfo serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance(SERVICE_NAME, "_presence._tcp", record);

        mManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // success
            }

            @Override
            public void onFailure(int arg0) {
                Log.d(LOG_TAG, "failed add local service: " + arg0);
            }
        });
    }

    public void setGroupCreated (boolean value) {
        isGroupCreated = value;
    }
    public boolean isGroupCreated() {
        return isGroupCreated;
    }

    @Override
    public synchronized void updateCurrentClient(String ip) {
        currentClientAddress = ip;
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

    public void onClickSendMsg(View view) {
        if (isGroupCreated()) {
            MessageSender sender = new MessageSender("Message from server: " + (++count), WifiDirectActivity.this,
                    currentClientAddress, SERVER_PORT);
            sender.execute();
        }
    }
}
