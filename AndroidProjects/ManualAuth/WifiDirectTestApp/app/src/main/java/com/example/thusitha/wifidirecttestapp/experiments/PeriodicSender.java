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
import com.example.thusitha.wifidirecttestapp.wfdMessaging.TcpMessageSender;
import com.example.thusitha.wifidirecttestapp.wfdMessaging.TransportProtocol;
import com.example.thusitha.wifidirecttestapp.wfdMessaging.UdpMessageSender;

public abstract class PeriodicSender extends Experiment {

    protected ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(5);
    protected static ScheduledFuture<?> t;

    protected long messageLimit;
    protected boolean isSender = false;
    protected int messageSize = 4500;
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
        String fileName = "PS-"
                .concat(String.valueOf(messageSize).concat("-"))
                .concat(String.valueOf(periodMS).concat("-"))
                .concat(String.valueOf(distanceM).concat("-"));
        if (isSender) {
            fileName = fileName.concat("s.txt");
        } else {
            fileName = fileName.concat("r.txt");
        }

        fileLogger.createLogFile(fileName);
    }

    @Override
    public void run() {

        // if sender send Messages, else start listening for messages
        if (isSender) {

            TransportProtocol protocol = messageManager.getProtocol();
            Runnable runnable;

            switch (protocol) {
                case TCP:
                    runnable = new TcpSenderRunnable(destinationAddress, messageManager.getPort(), messageSize, messageLimit, fileLogger);
                    break;
                case UDP:
                    runnable = new UdpSenderRunnable(destinationAddress, messageManager.getPort(), messageSize, messageLimit, fileLogger);
                    break;
                default:
                    runnable = new TcpSenderRunnable(destinationAddress, messageManager.getPort(), messageSize, messageLimit, fileLogger);
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
        this.messageSize = Integer.valueOf(parameters[1]);
        if (messageSize < 20) {
            messageSize = 20;
        }
        this.periodMS = Long.valueOf(parameters[2]);
        this.durationMS = Long.valueOf(parameters[3]);
        this.distanceM = Integer.valueOf(parameters[4]);
        this.destinationAddress = parameters[5];
    }

    @Override
    protected void setMessageHandler() {
        super.setMessageHandler();
        messageManager.registerHandler(messageHandler);
    }

    @Override
    protected void handleWFDMessage(Message message) {
        String [] parts = (message.obj.toString()).split("@");
        char [] tempBuf = Arrays.copyOfRange((parts[0]).toCharArray(), 0, 19);
        fileLogger.appendLog((new String(tempBuf)).concat("@").concat(parts[1]));
    }

}

class UdpSenderRunnable extends UdpMessageSender {

    static long count = 0;
    static long messageLimit;
    static FileLogger fileLogger;
    static final Object countLock = new Object();
    private final int messageSize;

    public UdpSenderRunnable(String address, int port, int size, long limit, FileLogger logger) {
        super(null, address, port);
        messageSize = size;
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
                char [] tempBuf = Arrays.copyOfRange(message.toCharArray(), 0, 19);
                fileLogger.appendLog((new String(tempBuf)).concat("@").concat(Long.toString(System.currentTimeMillis())));
                super.run();
            }
        }

    }

    private void setMessage(long id) {

        String str = Long.toString(id);
        char[] strArr = str.toCharArray();
        char[] dataArr = Arrays.copyOf(strArr, messageSize);
        if (strArr.length < 20) {
            Arrays.fill(dataArr, strArr.length, dataArr.length - 1, ' ');
        }
        message = new String(dataArr);
    }

}

class TcpSenderRunnable extends TcpMessageSender {

    static long count = 0;
    static long messageLimit;
    static FileLogger fileLogger;
    static final Object countLock = new Object();
    private final int messageSize;

    public TcpSenderRunnable(String address, int port, int size, long limit, FileLogger logger) {
        super(null, address, port);
        this.messageSize = size;
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
                char [] tempBuf = Arrays.copyOfRange(message.toCharArray(), 0, 19);
                fileLogger.appendLog((new String(tempBuf)).concat("@").concat(Long.toString(System.currentTimeMillis())));
                super.run();
            }
        }

    }

    private void setMessage(long id) {

        String str = Long.toString(id);
        char[] strArr = str.toCharArray();
        char[] dataArr = Arrays.copyOf(strArr, messageSize);
        if (strArr.length < 20) {
            Arrays.fill(dataArr, strArr.length, dataArr.length - 1, ' ');
        }
        message = new String(dataArr);
    }

}