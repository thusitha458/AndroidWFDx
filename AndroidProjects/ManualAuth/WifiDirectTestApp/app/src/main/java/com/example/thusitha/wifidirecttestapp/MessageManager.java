package com.example.thusitha.wifidirecttestapp;


public class MessageManager implements DestroyableObject {

    public enum MessagingProtocol {
        TCP, UDP
    }

    private MessagingProtocol protocol;
    private ScreenUpdater screenUpdater;
    private int port;

    private DestroyableObject destroyableListener = null;

    public MessageManager(MessagingProtocol protocol, ScreenUpdater screenUpdater, int port) {

        this.protocol = protocol;
        this.screenUpdater = screenUpdater;
        this.port = port;

    }

    public synchronized void startListener (ClientListManager clientListManager) {

        switch (protocol) {
            case TCP:
                TcpMessageListener tcpMessageListener = new TcpMessageListener(screenUpdater, clientListManager, port);
                destroyableListener = tcpMessageListener;
                tcpMessageListener.start();
                break;
            case UDP:
                UdpMessageListener udpMessageListener = new UdpMessageListener(screenUpdater, clientListManager, port);
                destroyableListener = udpMessageListener;
                udpMessageListener.start();
                break;
            default:
                break;
        }

    }

    public synchronized void sendMessage (String destinationAddress, String message) {

        switch (protocol) {
            case TCP:
                TcpMessageSender tcpMessageSender = new TcpMessageSender(message, screenUpdater, destinationAddress, port);
                tcpMessageSender.execute();
                break;
            case UDP:
                UdpMessageSender udpMessageSender = new UdpMessageSender(message, screenUpdater, destinationAddress, port);
                udpMessageSender.execute();
                break;
            default:
                break;
        }

    }

    @Override
    public void onDestroyObject () {
        if (destroyableListener != null) {
            destroyableListener.onDestroyObject();
        }
    }

}
