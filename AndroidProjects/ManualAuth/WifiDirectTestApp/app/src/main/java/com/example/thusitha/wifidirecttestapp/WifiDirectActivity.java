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
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.example.thusitha.wifidirecttestapp.experiments.Experiment;
import com.example.thusitha.wifidirecttestapp.experiments.ExperimentFactory;
import com.example.thusitha.wifidirecttestapp.experiments.ExperimentType;
import com.example.thusitha.wifidirecttestapp.threadMessaging.InterThreadMessageTypes;
import com.example.thusitha.wifidirecttestapp.wfdMessaging.MessageManager;
import com.example.thusitha.wifidirecttestapp.wfdMessaging.TcpMessageListener;
import com.example.thusitha.wifidirecttestapp.wfdMessaging.TransportProtocol;

public class WifiDirectActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

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

    private Handler messageHandler;
    private MessageManager messageManager;
    Experiment experiment = null;
    private boolean isClientAddressSet = false;
    private boolean clientSentAMessage = false;

    private ExperimentType selectedExperimentType = ExperimentType.values()[0];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_direct);
        //text view
        textView = (TextView) findViewById(R.id.status_view);
        textView.setMovementMethod(new ScrollingMovementMethod());
        //set spinner
        Spinner experimentSpinner = (Spinner) findViewById(R.id.experiment_spinner);
        experimentSpinner.setOnItemSelectedListener(this);
        List<String> typeList = new ArrayList<>();
        for (ExperimentType type: ExperimentType.values()) {
            typeList.add(type.toString());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, typeList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        experimentSpinner.setAdapter(spinnerAdapter);
        //set message handler
        messageHandler = new ActivityMessageHandler(this);
        messageManager = new MessageManager(TransportProtocol.UDP, SERVER_PORT);
        //wifi direct
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
        messageManager.onDestroyObject();
        messageManager.unregisterHandler(messageHandler);
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

        CheckBox makeGo = (CheckBox) findViewById(R.id.is_go_check_box);
        if (makeGo.isChecked()) {
            config.groupOwnerIntent = 15;
        } else {
            config.groupOwnerIntent = 1;
        }

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

                    messageManager.startListener();
                    messageManager.registerHandler(messageHandler);
                    experiment = (new ExperimentFactory(messageManager)).getExperiment(selectedExperimentType);
                } else if (info.groupFormed) {
                    setGroupOwner(false);
                    setConnected(true);
                    textView.append("Connected, Not a GO" + '\n');
                    count = 0;

                    messageManager.startListener();
                    messageManager.registerHandler(messageHandler);
                    experiment = (new ExperimentFactory(messageManager)).getExperiment(selectedExperimentType);
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
                clientSentAMessage = true;
                receiverAddress = groupOwnerAddress;
                message = "Message from client: ";
            }
            message = message + (++count);

            messageManager.sendMessage(receiverAddress, message);
            displayMessage(false, message);
        }
    }

    public boolean isConnecting() {
        return isConnecting;
    }

    public void setConnecting(boolean connecting) {
        isConnecting = connecting;
    }


    protected void displayMessage(boolean isReceived, String message) {

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


    protected void updateCurrentClient(String ip) {
        isClientAddressSet = true;
        currentClientAddress = ip;
    }


    public void onClickStartExperiment(View view) {

        if (experiment == null) {
            Toast.makeText(this, "Not ready for an experiment yet", Toast.LENGTH_SHORT).show();
            return;
        }

        if (experiment.isRunning()) {
            Toast.makeText(this, "Experiment already started", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText periodText = (EditText) findViewById(R.id.period_text);
        EditText durationText = (EditText) findViewById(R.id.duration_text);

        String periodStr = periodText.getText().toString();
        String durationStr = durationText.getText().toString();

        if (periodStr.equals("") || durationStr.equals("")) {
            Toast.makeText(this, "Enter period/duration", Toast.LENGTH_SHORT).show();
            return;
        }

        experiment.setParameters(String.valueOf(!isGroupOwner),
                periodStr,
                durationStr,
                isGroupOwner ? currentClientAddress : groupOwnerAddress
        );

        experiment.startExperiment();
        textView.append("Experiment started...\n");
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedExperimentType = ExperimentType.values()[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        selectedExperimentType = ExperimentType.values()[0];
    }
}

class ActivityMessageHandler extends Handler {

    private WifiDirectActivity activity;

    ActivityMessageHandler(WifiDirectActivity activity) {
        this.activity = activity;
    }

    @Override
    public void handleMessage(Message message) {

        switch (message.what) {

            case InterThreadMessageTypes.CLIENT_IP_ADDRESS:
                activity.updateCurrentClient(message.obj.toString());
                break;
            case InterThreadMessageTypes.WIFI_DIRECT_MESSAGE:
                activity.displayMessage(true, message.obj.toString());
                break;
            default:
                break;

        }

    }


}