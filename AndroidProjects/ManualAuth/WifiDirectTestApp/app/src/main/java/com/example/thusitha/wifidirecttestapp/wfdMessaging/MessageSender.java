package com.example.thusitha.wifidirecttestapp.wfdMessaging;

import android.os.AsyncTask;

public abstract class MessageSender extends AsyncTask <Void, Void, Void> {
    protected String address;
    protected int port;
    protected String message = "";
}