package com.example.thusitha.wifidirecttestapp.wfdMessaging;


public abstract class MessageSender extends Thread {
    protected String address;
    protected int port;
    protected String message = "";
}
