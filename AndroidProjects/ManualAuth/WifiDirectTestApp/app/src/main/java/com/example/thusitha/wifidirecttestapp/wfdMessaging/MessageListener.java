package com.example.thusitha.wifidirecttestapp.wfdMessaging;

import android.os.Handler;
import android.os.Message;

import com.example.thusitha.wifidirecttestapp.DestroyableObject;
import com.example.thusitha.wifidirecttestapp.threadMessaging.InterThreadMessageTypes;
import com.example.thusitha.wifidirecttestapp.threadMessaging.InterThreadMessenger;

import java.util.HashSet;
import java.util.Set;

public abstract class MessageListener extends Thread implements InterThreadMessenger, DestroyableObject {

    protected Set<Handler> handlerSet = new HashSet<>();
    protected int port;

    @Override
    public void registerHandler (Handler handler) {
        handlerSet.add(handler);
    }

    @Override
    public void unregisterHandler (Handler handler) {
        handlerSet.remove(handler);
    }

    protected void sendClientIpMessage (String clientIp) {
        for (Handler handler: handlerSet) {
            Message message = Message.obtain();
            message.what = InterThreadMessageTypes.CLIENT_IP_ADDRESS;
            message.obj = clientIp;
            handler.sendMessage(message);
        }
    }

    protected void sendWFDMessageContents(String data) {
        for (Handler handler: handlerSet) {
            Message message = Message.obtain();
            message.what = InterThreadMessageTypes.WIFI_DIRECT_MESSAGE;
            message.obj = data.concat("@").concat(Long.toString(System.currentTimeMillis()));
            handler.sendMessage(message);
        }
    }

    @Override
    public void onDestroyObject() {
        this.interrupt();
    }

}
