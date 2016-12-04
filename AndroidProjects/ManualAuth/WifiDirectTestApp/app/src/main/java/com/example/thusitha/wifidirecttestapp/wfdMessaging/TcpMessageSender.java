package com.example.thusitha.wifidirecttestapp.wfdMessaging;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

public class TcpMessageSender extends MessageSender {

    public TcpMessageSender(String message, String address, int port) {
        this.message = message;
        this.address = address;
        this.port = port;
    }

    @Override
    public void run () {
        Socket socket;
        try {
            socket = new Socket(address, port);
            OutputStream outputStream = socket.getOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            printStream.print(message);
            printStream.close();
        } catch (IOException e) {
            message = e.toString();
            e.printStackTrace();
        }
    }

}
