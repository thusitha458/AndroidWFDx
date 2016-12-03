package com.example.thusitha.wifidirecttestapp.threadMessaging;

import android.os.Handler;

public interface InterThreadMessenger {
    void registerHandler(Handler handler);
    void unregisterHandler(Handler handler);
}

