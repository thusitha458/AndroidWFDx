package com.example.thusitha.wifidirecttestapp;


import android.os.AsyncTask;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UdpMessageSender extends MessageSender {

    public UdpMessageSender (String message, String address, int port) {
        this.message = message;
        this.address = address;
        this.port = port;
    }

    @Override
    protected Void doInBackground(Void... params) {

        try {

            DatagramSocket datagramSocket = new DatagramSocket();
            InetAddress ip = InetAddress.getByName(address);
            byte [] messageBytes = message.getBytes();

            DatagramPacket datagramPacket = new DatagramPacket(messageBytes, messageBytes.length, ip, port);
            datagramSocket.send(datagramPacket);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
