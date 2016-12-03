package com.example.thusitha.wifidirecttestapp.experiments;

import android.os.Handler;
import android.os.Message;

import com.example.thusitha.wifidirecttestapp.logging.FileLogger;
import com.example.thusitha.wifidirecttestapp.threadMessaging.InterThreadMessageTypes;
import com.example.thusitha.wifidirecttestapp.wfdMessaging.MessageManager;

public abstract class Experiment extends Thread {

    protected volatile Handler messageHandler;
    protected MessageManager messageManager = null;
    protected FileLogger fileLogger;
    protected boolean isRunning = false;

    public abstract void startExperiment();
    public abstract void setParameters(String... parameters);
    protected abstract void handleWFDMessage (Message message);
    protected abstract void setFileLogger();

    //ALWAYS CALL super.setMessageHandler() when you override this
    protected void setMessageHandler () {
        messageHandler = new MessageHandler(this);
    }

    public void setMessageManager(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    public boolean isRunning() {
        return isRunning;
    }

    protected void setRunning(boolean running) {
        isRunning = running;
    }

}

class MessageHandler extends Handler {

    Experiment experiment;

    MessageHandler (Experiment experiment) {
        this.experiment = experiment;
    }
    @Override
    public void handleMessage(Message message) {

        switch (message.what) {

            case InterThreadMessageTypes.WIFI_DIRECT_MESSAGE:
                experiment.handleWFDMessage(message);
                break;

        }

    }

}