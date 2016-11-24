package com.example.thusitha.wifidirecttestapp;


import android.os.AsyncTask;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

public class MessageSender extends AsyncTask<Void, Void, Void> {

    private String dstAddress;
    private int dstPort;
    private ScreenUpdater activity;
    private String message = "";

    public MessageSender(String message, ScreenUpdater activity, String dstAddress, int dstPort) {
        this.message = message;
        this.activity = activity;
        this.dstAddress = dstAddress;
        this.dstPort = dstPort;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Socket socket = null;
        try {
            socket = new Socket(dstAddress, dstPort);
            OutputStream outputStream = socket.getOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            printStream.print(message);
            printStream.close();
        } catch (IOException e) {
            message = e.toString();
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        activity.displayMessage(false, message);
        super.onPostExecute(aVoid);
    }

}
