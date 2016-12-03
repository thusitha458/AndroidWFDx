package com.example.thusitha.wifidirecttestapp.logging;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;


public class Logger1 implements FileLogger {

    private String fileName = "log1.txt";
    private File logFile;
    private static Logger1 logger1 = new Logger1();

    private Logger1() {}

    public synchronized static Logger1 getInstance () {
        return logger1;
    }

    @Override
    public void createLogFile () {
        createLogFile(fileName);
    }

    @Override
    public void createLogFile (String fileName) {

        this.fileName = fileName;
        logFile = new File(Environment.getExternalStorageDirectory() + File.separator + fileName);

        if (!logFile.exists()) {

            try {
                logFile.createNewFile();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

        }

        startSession();

    }


    private void startSession () {

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        appendLog("\n" +
                "Start of new experiment @ " +
                currentDateTimeString +
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
