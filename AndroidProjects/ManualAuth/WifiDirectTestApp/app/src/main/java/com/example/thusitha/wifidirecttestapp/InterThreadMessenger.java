package com.example.thusitha.wifidirecttestapp;

import android.os.Handler;

public interface InterThreadMessenger {
    void registerHandler(Handler handler);
    void unregisterHandler(Handler handler);
}

