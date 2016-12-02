package com.example.thusitha.wifidirecttestapp;


import android.os.Handler;

public class MessageManager implements DestroyableObject, InterThreadMessenger {

    private TransportProtocol protocol;
    private int port;
    private MessageListener messageListener = null;

    public MessageManager(TransportProtocol protocol, int port) {
        this.protocol = protocol;
        this.port = port;
    }

    public synchronized void startListener () {

        switch (protocol) {
            case TCP:
                messageListener = new TcpMessageListener(port);
                break;
            case UDP:
                messageListener = new UdpMessageListener(port);
                break;
            default:
                messageListener = new TcpMessageListener(port);
                break;
        }

        messageListener.start();

    }

    public synchronized void sendMessage (String destinationAddress, String message) {

        MessageSender messageSender;

        switch (protocol) {
            case TCP:
                messageSender = new TcpMessageSender(message, destinationAddress, port);
                break;
            case UDP:
                messageSender = new UdpMessageSender(message, destinationAddress, port);
                break;
            default:
                messageSender = new TcpMessageSender(message, destinationAddress, port);
                break;
        }

        messageSender.execute();

    }

    @Override
    public synchronized void registerHandler(Handler handler) {
        if (messageListener != null) {
            messageListener.registerHandler(handler);
        }
    }

    @Override
    public synchronized void unregisterHandler(Handler handler) {
        if (messageListener != null) {
            messageListener.unregisterHandler(handler);
        }
    }

    @Override
    public void onDestroyObject () {
        if (messageListener != null) {
            messageListener.onDestroyObject();
        }
    }

}
