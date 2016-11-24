package com.example.thusitha.wfdmeshclient;

import android.os.AsyncTask;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class MessageClient extends AsyncTask<Void, Void, Void> {

    private String dstAddress;
    private int dstPort;
    private WifiDirectActivity activity;
    private TextView textView;
    private String response = "";
    private String message = "";

    public MessageClient(String message, WifiDirectActivity activity, String dstAddress, int port, TextView textView) {
        this.message = message;
        this.activity = activity;
        this.dstAddress = dstAddress;
        dstPort = port;
        this.textView = textView;
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

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.append("Sent : " + message + '\n');
                }
            });

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
            byte[] buffer = new byte[1024];

            int bytesRead;
            InputStream inputStream = socket.getInputStream();

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
                response += byteArrayOutputStream.toString("UTF-8");
            }
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            response = "UnknownHostException: " + e.toString();
        } catch (IOException e) {
            e.printStackTrace();
            response = "IOException: " + e.toString();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.append("Got: " + response + '\n');
            }
        });
        super.onPostExecute(aVoid);
    }
}
