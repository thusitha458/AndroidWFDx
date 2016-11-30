package com.example.thusitha.wifidirecttestapp;


public class MessageContents {

    public Long timestamp;
    public String data;

    public MessageContents (long timestamp, String data) {
        this.timestamp = timestamp;
        this.data = data;
    }

    @Override
    public String toString () {
        String str = (data.concat(" @ ")).concat(timestamp.toString());
        return str;
    }

}
