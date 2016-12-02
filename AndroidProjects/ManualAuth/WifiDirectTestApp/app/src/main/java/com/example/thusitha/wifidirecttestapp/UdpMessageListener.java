package com.example.thusitha.wifidirecttestapp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UdpMessageListener extends MessageListener {

    public UdpMessageListener (int port) {
        this.port = port;
    }

    @Override
    public void run() {

        byte buf [] = new byte[4096];
        DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);

        try {
            DatagramSocket socket = new DatagramSocket(port);

            while (true) {

                socket.receive(datagramPacket);

                ProcessMessageThread processMessageThread = new ProcessMessageThread(
                        datagramPacket.getAddress().getHostAddress(),
                        new String(buf, 0, datagramPacket.getLength())
                );
                processMessageThread.start();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private class ProcessMessageThread extends Thread {

        private String messageData;
        private String senderAddress;

        public ProcessMessageThread (String senderAddress, String messageData) {
            this.messageData = messageData;
            this.senderAddress = senderAddress;
        }

        @Override
        public void run () {
            sendClientIpMessage(senderAddress);
            sendWFDMessageContents(messageData);
        }


    }

}
