package com.example.thusitha.wifidirecttestapp;


import android.os.AsyncTask;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UdpMessageSender extends AsyncTask<Void, Void, Void> {

    private String message;
    private ScreenUpdater screenUpdater;
    private String dstAddress;
    private int dstPort;

    public UdpMessageSender (String message, ScreenUpdater screenUpdater, String dstAddress, int dstPort) {
        this.message = message;
        this.screenUpdater = screenUpdater;
        this.dstAddress = dstAddress;
        this.dstPort = dstPort;
    }

    @Override
    protected Void doInBackground(Void... params) {

        try {

            DatagramSocket datagramSocket = new DatagramSocket();
            InetAddress ip = InetAddress.getByName(dstAddress);
            byte [] messageBytes = message.getBytes();

            DatagramPacket datagramPacket = new DatagramPacket(messageBytes, messageBytes.length, ip, dstPort);
            datagramSocket.send(datagramPacket);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        screenUpdater.displayMessage(false, message);
        super.onPostExecute(aVoid);
    }

}
