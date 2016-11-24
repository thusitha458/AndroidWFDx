package com.example.thusitha.wfdmeshclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WifiDirectActivity extends AppCompatActivity {

    public static String LOG_TAG = "Logs";
    private static String SERVICE_NAME = "wfd_mesh";
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiManager wifiManager;
    private IntentFilter intentFilter;
    private WifiReceiver wifiReceiver;

    private boolean serverFound = false;
    private boolean isConnected = false;
    private boolean isConnecting = false;

    private String ssid;
    private String passphrase;
    private String serverIp;
    private int serverPort;

    private TextView textView;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_direct);

        textView = (TextView) findViewById(R.id.status_view);
        textView.setMovementMethod(new ScrollingMovementMethod());


        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//        wifiManager.disconnect();

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        wifiReceiver = new WifiReceiver();


        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
    }

    @Override
    protected void onResume() {
        registerReceiver(wifiReceiver, intentFilter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(wifiReceiver);
        super.onPause();
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

    private void discoverService () {
        final HashMap<String, String> recordCopy = new HashMap<>();

        WifiP2pManager.DnsSdTxtRecordListener txtListener = new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomain, Map record, WifiP2pDevice device) {
                if (!isConnected()) {
                    Log.d(LOG_TAG, "DnsSdTxtRecord available -" + record.toString());
                    recordCopy.put("listenport", (String) record.get("listenport"));
                    recordCopy.put("ssid", (String) record.get("ssid"));
                    recordCopy.put("psk", (String) record.get("psk"));
                    recordCopy.put("ip", (String) record.get("ip"));
                }
            }
        };

        WifiP2pManager.DnsSdServiceResponseListener servListener = new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice resourceType) {
                if (!isConnected()) {
                    if (instanceName.equals(SERVICE_NAME)) {
                        serverPort = Integer.parseInt(recordCopy.get("listenport"));
                        ssid = recordCopy.get("ssid");
                        passphrase = recordCopy.get("psk");
                        serverIp = recordCopy.get("ip");
                        Log.d(LOG_TAG, "onDnsSdServiceAvailable: " + instanceName);
                        // Try connecting using wifi manager
                        textView.append("Connecting to " + ssid + '\n');
                        connectToAP();
                    }
                }
            }
        };

        mManager.setDnsSdResponseListeners(mChannel, servListener, txtListener);

        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();

        mManager.addServiceRequest(mChannel, serviceRequest, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // Success!
            }

            @Override
            public void onFailure(int code) {
                Log.d(LOG_TAG, "add service request failed: " + code);
            }
        });

        mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // Success!
            }

            @Override
            public void onFailure(int code) {
                Log.d(LOG_TAG, "discover services failed: " + code);
            }

        });
    }

    private void connectToAP () {

        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + ssid + "\"";
        conf.preSharedKey = "\""+ passphrase +"\"";

        Log.d(LOG_TAG, ssid);
        Log.d(LOG_TAG, passphrase);

        wifiManager.disconnect();

        // check if the network is already in the configured networks list
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        if (list != null && list.size() > 0) {
            for (WifiConfiguration i: list) {
//                wifiManager.disconnect();
                if (i.SSID.equals("\"" + ssid + "\"")) {
                    // remove
                    wifiManager.removeNetwork(i.networkId);
                } else {
                    // disable
                    wifiManager.disableNetwork(i.networkId);
                }
            }
        }

        int netId = wifiManager.addNetwork(conf);
        if (netId != -1) {
            setServerFound(true);
//            boolean temp = wifiManager.disconnect();
//            Log.d(LOG_TAG, "disconnected: " + temp);
            boolean temp = wifiManager.enableNetwork(netId, true);
            Log.d(LOG_TAG, "enablenetwork: " + temp);
            boolean reconnected = wifiManager.reconnect();

            if (reconnected) {

//                wifiManager.setWifiEnabled(false);
                Log.d(LOG_TAG, "flipping wifi state");
                wifiManager.setWifiEnabled(true);
                Log.d(LOG_TAG, "flipped wifi state");
                // wait for broadcast receiver to get the connected intent
            }
        }

    }

    private void completeConnection () {
        setConnected(true);
        textView.append("Connection successful" + '\n');
        count = 0;
    }

    public boolean getServerFound() {
        return serverFound;
    }

    public void setServerFound(boolean value) {
        serverFound = value;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected (boolean value) {
        isConnected = value;
    }

//    public boolean isWifiEnabled() {
//        return isWifiEnabled;
//    }

//    public void setIsWifiEnabled(boolean value) {
//        isWifiEnabled = value;
//    }

    public boolean isConnecting() {
        return isConnecting;
    }

    public void setConnecting(boolean connecting) {
        isConnecting = connecting;
    }

    public void onClickConnect(View view) {
        if (!isConnected()) {
            textView.append("Trying to connect to a server..." + '\n');
            discoverService();
        }
    }

    public void onClickSendMessage(View view) {
        if (isConnected()) {
            MessageClient client = new MessageClient("Message " + (++count), WifiDirectActivity.this, serverIp, serverPort, textView);
            client.execute();
        }
    }


    private class WifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                if (!isConnected()) {
                    NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    if (networkInfo.getDetailedState().equals(NetworkInfo.DetailedState.CONNECTED)) {
                        Log.d(LOG_TAG, "Network state connected");
                        if (getServerFound()) {
                            completeConnection();
                        }
                    } else if (networkInfo.getDetailedState().equals(NetworkInfo.DetailedState.DISCONNECTED)) {
                        Log.d(LOG_TAG, "Network state disconnected");
                    }
                }
            } else if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
                SupplicantState supplicantState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                Log.d(LOG_TAG, supplicantState.toString());
            }
        }
    }
}
