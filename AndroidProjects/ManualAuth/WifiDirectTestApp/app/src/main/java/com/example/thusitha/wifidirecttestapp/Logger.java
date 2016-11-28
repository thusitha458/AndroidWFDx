package com.example.thusitha.wifidirecttestapp;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Thusitha on WifiDirectTestApp.
 */
public class Logger {

    private String fileName;

    private File logFile;

    public Logger (String fileName) {

        this.fileName = fileName;
        createLogFile();
    }


    private void createLogFile () {

        logFile = new File(Environment.getExternalStorageDirectory() + File.separator + fileName);

        Log.d("testlog", Environment.getExternalStorageDirectory().toString());

        if (!logFile.exists()) {

            Log.d("testlog", "file doesn't exist");

            try {
                boolean success = logFile.createNewFile();
                Log.d("testlog", "lll");
            } catch (IOException ioe) {
                Log.d("testlog", ioe.getMessage());
//                ioe.getMessage();
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


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
