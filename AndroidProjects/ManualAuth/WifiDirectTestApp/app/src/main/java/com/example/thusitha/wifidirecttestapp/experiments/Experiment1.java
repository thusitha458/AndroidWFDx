package com.example.thusitha.wifidirecttestapp.experiments;


import android.os.Looper;
import android.os.Handler;
import android.os.Message;

import com.example.thusitha.wifidirecttestapp.FileLoggerFactory;
import com.example.thusitha.wifidirecttestapp.logging.LoggerType;

public class Experiment1 extends Experiment {

    private Handler periodicMessageHandler = new Handler();

    private long currentId = 0;
    private long messageLimit;

    private boolean isSender = false;
    private long periodMS = 2000;
    private long durationMS = 20000;
    private String destinationAddress;

    private static Experiment1 instance = new Experiment1();

    private Experiment1 () {}

    public static Experiment1 getInstance () {
        return instance;
    }

    @Override
    public void startExperiment() {

        if (messageManager == null) return;

        setMessageLimit();
        setFileLogger();

        setRunning(true);
        this.start();

    }

    @Override
    protected void setFileLogger () {
        fileLogger = (new FileLoggerFactory()).getFileLogger(LoggerType.LOGGER_1);
        fileLogger.createLogFile();
    }

    @Override
    public void run () {

        // if sender send Messages, else start listening for messages
        if (isSender) {
            messageSenderRunnable.run();
        } else {
            Looper.prepare();
            setMessageHandler();
            Looper.loop();
            while(true) {
                // nothing
            }
        }
    }

    protected String constructMessage (long id) {
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

    protected void setMessageLimit () {
        messageLimit = durationMS / periodMS;
    }


    @Override
    public void setParameters(String... parameters) {
        this.isSender = Boolean.valueOf(parameters[0]);
        this.periodMS = Long.valueOf(parameters[1]);
        this.durationMS = Long.valueOf(parameters[2]);
        this.destinationAddress = parameters[3];
    }

    @Override
    protected void setMessageHandler() {
        super.setMessageHandler();
        messageManager.registerHandler(messageHandler);
    }

    @Override
    void handleWFDMessage(Message message) {
        fileLogger.appendLog(message.obj.toString());
    }


    private Runnable messageSenderRunnable = new Runnable() {

        @Override
        public void run() {
            long nextId;
            if ((nextId = getNextId()) >= 0) {
                String messageData = constructMessage(nextId);
                messageManager.sendMessage(
                        destinationAddress,
                        messageData
                );
                fileLogger.appendLog(messageData.concat("@").concat(Long.toString(System.currentTimeMillis())));
                periodicMessageHandler.postDelayed(this, periodMS);
            } else {
                periodicMessageHandler.removeCallbacks(this);
            }
        }

    };

}
