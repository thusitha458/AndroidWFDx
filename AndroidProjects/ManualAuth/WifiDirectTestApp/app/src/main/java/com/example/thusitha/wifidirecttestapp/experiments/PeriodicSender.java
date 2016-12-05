package com.example.thusitha.wifidirecttestapp.experiments;

import android.os.Looper;
import android.os.Message;
import java.util.Arrays;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.example.thusitha.wifidirecttestapp.logging.FileLogger;
import com.example.thusitha.wifidirecttestapp.logging.FileLoggerFactory;
import com.example.thusitha.wifidirecttestapp.logging.LoggerType;
import com.example.thusitha.wifidirecttestapp.wfdMessaging.TransportProtocol;
import com.example.thusitha.wifidirecttestapp.wfdMessaging.UdpMessageSender;

public abstract class PeriodicSender extends Experiment {

    protected ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(5);
    protected static ScheduledFuture<?> t;

    protected long messageLimit;
    protected boolean isSender = false;
    protected long periodMS = 2000;
    protected long durationMS = 20000;
    protected int distanceM = 10;
    protected String destinationAddress;

//    protected static char[] messageChar = new char[100];

    @Override
    public void startExperiment() {

        if (messageManager == null) return;

//        setMessageBuffer();
        setMessageLimit();
        setFileLogger();

        setRunning(true);
        this.start();

    }

//    protected void setMessageBuffer() {
//        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "novel");
//        BufferedInputStream buf = null;
//        try {
//            buf = new BufferedInputStream(new FileInputStream(file));
//            int bytesRead = buf.read(messageBytes, 20, messageBytes.length - 20);
//            buf.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Arrays.fill(messageBytes, 0, 19, (byte) ' ');
//    }

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

            TransportProtocol protocol = messageManager.getProtocol();
            Runnable runnable;

            switch (protocol) {
                case TCP:
                    runnable = new TcpSenderRunnable(destinationAddress, messageManager.getPort(), messageLimit, fileLogger);
                    break;
                case UDP:
                    runnable = new UdpSenderRunnable(destinationAddress, messageManager.getPort(), messageLimit, fileLogger);
                    break;
                default:
                    runnable = new TcpSenderRunnable(destinationAddress, messageManager.getPort(), messageLimit, fileLogger);
                    break;
            }

            t = executor.scheduleAtFixedRate(
                    runnable,
                    1000,
                    periodMS,
                    TimeUnit.MILLISECONDS
            );

        } else {
            Looper.prepare();
            setMessageHandler();
            Looper.loop();
            while (true) {
                // nothing
            }
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

}

class UdpSenderRunnable extends UdpMessageSender {

    static long count = 0;
    static long messageLimit;
    static FileLogger fileLogger;
    static final Object countLock = new Object();

    public UdpSenderRunnable(String address, int port, long limit, FileLogger logger) {
        super(null, address, port);
        messageLimit = (limit - 1) * 5;
        fileLogger = logger;
    }

    @Override
    public void run() {

        synchronized (countLock) {
            for (int i = 0; i < 5; i++) {
                if (count >= messageLimit) {
                    PeriodicSender.t.cancel(false);
                }
                setMessage(count++);
                fileLogger.appendLog(message.concat("@").concat(Long.toString(System.currentTimeMillis())));
                super.run();
            }
        }

    }

    private void setMessage(long id) {

        String str = Long.toString(id);
        char[] strArr = str.toCharArray();
        char[] dataArr = Arrays.copyOf(strArr, 20);
        if (strArr.length < 20) {
            Arrays.fill(dataArr, strArr.length, dataArr.length - 1, ' ');
        }
        message = new String(dataArr);
    }

}

class TcpSenderRunnable extends UdpMessageSender {

    static long count = 0;
    static long messageLimit;
    static FileLogger fileLogger;
    static final Object countLock = new Object();

    public TcpSenderRunnable(String address, int port, long limit, FileLogger logger) {
        super(null, address, port);
        messageLimit = (limit - 1) * 5;
        fileLogger = logger;
    }

    @Override
    public void run() {

        synchronized (countLock) {
            for (int i = 0; i < 5; i++) {
                if (count >= messageLimit) {
                    PeriodicSender.t.cancel(false);
                }
                setMessage(count++);
                fileLogger.appendLog(message.concat("@").concat(Long.toString(System.currentTimeMillis())));
                super.run();
            }
        }

    }

    private void setMessage(long id) {

        String str = Long.toString(id);
        char[] strArr = str.toCharArray();
        char[] dataArr = Arrays.copyOf(strArr, 20);
        if (strArr.length < 20) {
            Arrays.fill(dataArr, strArr.length, dataArr.length - 1, ' ');
        }
        message = new String(dataArr);
    }

}