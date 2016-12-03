package com.example.thusitha.wifidirecttestapp.experiments;


import android.os.Looper;
import android.os.Handler;
import android.os.Message;

import com.example.thusitha.wifidirecttestapp.logging.FileLoggerFactory;
import com.example.thusitha.wifidirecttestapp.logging.LoggerType;

public abstract class PeriodicSender extends Experiment {

    protected Handler periodicMessageHandler = new Handler();

    protected long currentId = 0;
    protected long messageLimit;

    protected boolean isSender = false;
    protected long periodMS = 2000;
    protected long durationMS = 20000;
    protected int distanceM = 10;
    protected String destinationAddress;


    @Override
    public void startExperiment() {

        if (messageManager == null) return;

        setMessageLimit();
        setFileLogger();

        setRunning(true);
        this.start();

    }

    @Override
    protected void setFileLogger() {
        fileLogger = (new FileLoggerFactory()).getFileLogger(LoggerType.LOGGER_1);
        fileLogger.createLogFile("PS-"
                .concat(String.valueOf(periodMS).concat("-"))
                .concat(String.valueOf(durationMS).concat("-"))
                .concat(String.valueOf(distanceM))
                .concat(".txt")
        );
    }

    @Override
    public void run() {

        // if sender send Messages, else start listening for messages
        if (isSender) {
            messageSenderRunnable.run();
        } else {
            Looper.prepare();
            setMessageHandler();
            Looper.loop();
            while (true) {
                // nothing
            }
        }
    }

    protected String constructMessage(long id) {
        String str = Long.toString(id);
        char[] strArr = str.toCharArray();
        char[] dataArr = new char[20];
        for (int i = 0; i < dataArr.length; i++) {

            if (i < strArr.length) {
                dataArr[i] = strArr[i];
            } else {
                dataArr[i] = ' ';
            }

        }
        return new String(dataArr);
    }

    protected long getNextId() {
        if (currentId < messageLimit) {
            return (currentId++);
        } else {
            return -1;
        }
    }

    protected void setMessageLimit() {
        messageLimit = durationMS / periodMS;
    }


    @Override
    public void setParameters(String... parameters) {
        this.isSender = Boolean.valueOf(parameters[0]);
        this.periodMS = Long.valueOf(parameters[1]);
        this.durationMS = Long.valueOf(parameters[2]);
        this.distanceM = Integer.valueOf(parameters[3]);
        this.destinationAddress = parameters[4];
    }

    @Override
    protected void setMessageHandler() {
        super.setMessageHandler();
        messageManager.registerHandler(messageHandler);
    }

    @Override
    protected void handleWFDMessage(Message message) {
        fileLogger.appendLog(message.obj.toString());
    }


    protected Runnable messageSenderRunnable = new Runnable() {

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
