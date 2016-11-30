package com.example.thusitha.wifidirecttestapp;


import android.os.Looper;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;

public class Experiment1 extends Thread implements Experiment {

    public volatile Handler mHandler;
    private Handler sendMessageHandler = new Handler();

    private long currentId = 0;
    private long messageLimit;

    private long periodMS = 2000;
    private long durationMS = 20000;
    private String destinationAddress;

    private MessageHandler messageHandler = null;
    private FileLogger fileLogger;
    private boolean isRunning = false;

    private static Experiment1 instance = new Experiment1();

    private Experiment1 () {}

    public static Experiment1 getInstance () {
        return instance;
    }

    @Override
    public void startExperiment() {

        setMessageLimit();

        if (messageHandler == null) return;
        fileLogger = (new FileLoggerCreator()).getFileLogger(ExperimentFactory.Experiments.EXPERIMENT_1);
        fileLogger.createLogFile();

        isRunning = true;
        this.start();

    }

    @Override
    public void run () {
        // set handler
        Looper.prepare();
        mHandler = new Handler() { //TODO: fix this
            @Override
            public void handleMessage(Message msg) {
                // log
                fileLogger.appendLog(msg.obj.toString());
            }
        };
        Looper.loop();
        // send Messages
        Runnable messageSenderRunnable = new Runnable() {

            @Override
            public void run() {
                long nextId;
                if ((nextId = getNextId()) >= 0) {
                    messageHandler.sendMessage(
                            destinationAddress,
                            constructMessage(nextId)
                    );
                    sendMessageHandler.postDelayed(this, periodMS);
                } else {
                    sendMessageHandler.removeCallbacks(this);
                }
            }

        };

        messageSenderRunnable.run();
    }

    private String constructMessage (long id) {
        String str = Long.toString(id);
        char [] strArr = str.toCharArray();
        char [] dataArr = new char[20];
        for (int i = 0; i < dataArr.length; i++) {

            if (i < strArr.length) {
                dataArr[i] = strArr[i];
            } else {
                dataArr[i] = ' ';
            }

        }
        return new String(dataArr);
    }

    private long getNextId () {
        if (currentId < messageLimit) {
            return (currentId++);
        } else {
            return -1;
        }
    }

    private void setMessageLimit () {
        messageLimit = durationMS / periodMS;
    }


    @Override
    public void setParameters(String... parameters) {
        this.periodMS = Long.valueOf(parameters[0]);
        this.durationMS = Long.valueOf(parameters[1]);
        this.destinationAddress = parameters[2];
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public Handler getHandler() {
        return mHandler;
    }

}
