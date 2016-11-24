package com.example.thusitha.wifidirecttestapp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MessageListener extends Thread {

    private ScreenUpdater screenUpdater;
    private ClientListManager clientListManager;
    private int port;

    public MessageListener(ScreenUpdater screenUpdater, ClientListManager clientListManager, int port) {
        this.screenUpdater = screenUpdater;
        this.clientListManager = clientListManager;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            ServerSocket socket = new ServerSocket(port);
            while (true) {
                Socket clientSocket = socket.accept();

                ProcessMessageThread processMessage = new ProcessMessageThread(clientSocket);
                processMessage.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ProcessMessageThread extends Thread {
        private Socket clientSocket;
        public ProcessMessageThread (Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {

            clientListManager.updateCurrentClient(clientSocket.getInetAddress().getHostAddress());

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
            byte[] buffer = new byte[1024];

            int bytesRead;
            InputStream inputStream = null;
            String message = null;
            try {
                inputStream = clientSocket.getInputStream();
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    message = byteArrayOutputStream.toString("UTF-8");
                }
            } catch (IOException e) {
                message = e.toString();
                e.printStackTrace();
            }
            screenUpdater.displayMessage(true, message);

        }
    }
}
