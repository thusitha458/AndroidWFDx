package com.example.thusitha.wifidirecttestapp;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;


public class LoggerExp1 implements FileLogger {

    private String fileName = "logExp1.txt";
    private File logFile;
    private static LoggerExp1 loggerExp1 = new LoggerExp1();

    private LoggerExp1() {}

    public synchronized static LoggerExp1 getInstance () {
        return loggerExp1;
    }

    @Override
    public void createLogFile (String fileName) {

        this.fileName = fileName;
        logFile = new File(Environment.getExternalStorageDirectory() + File.separator + fileName);
        Log.d(WifiDirectActivity.LOG_TAG, Environment.getExternalStorageDirectory().toString());

        if (!logFile.exists()) {

            Log.d(WifiDirectActivity.LOG_TAG, "file doesn't exist");

            try {
                boolean success = logFile.createNewFile();
                Log.d(WifiDirectActivity.LOG_TAG, "Log file is created");
            } catch (IOException ioe) {
                Log.d(WifiDirectActivity.LOG_TAG, ioe.getMessage());
                ioe.printStackTrace();
            }

        }

        startSession();

    }


    private void startSession () {

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        appendLog("\n" +
                "Start of new experiment @ "
                + currentDateTimeString +
                "\n"
                );

    }

    @Override
    public void appendLog (String text) {

        if (logFile == null) return;

        try {

            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public String getFileName() {
        return fileName;
    }

}
