package com.example.thusitha.wifidirecttestapp;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UdpMessageListener extends Thread implements DestroyableObject {

    private ScreenUpdater screenUpdater;
    private ClientListManager clientListManager;
    private int port;

    public UdpMessageListener (ScreenUpdater screenUpdater, ClientListManager clientListManager, int port) {
        this.screenUpdater = screenUpdater;
        this.clientListManager = clientListManager;
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

    @Override
    public void onDestroyObject() {
        this.interrupt();
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
            clientListManager.updateCurrentClient(senderAddress);
            screenUpdater.displayMessage(true, new MessageContents(System.currentTimeMillis(), messageData));
        }


    }

}
