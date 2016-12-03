package com.example.thusitha.wifidirecttestapp.wfdMessaging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpMessageListener extends MessageListener {

    public TcpMessageListener(int port) {
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

            sendClientIpMessage(clientSocket.getInetAddress().getHostAddress());

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

            sendWFDMessageContents(message);

        }
    }




}
