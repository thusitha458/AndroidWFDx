package com.example.thusitha.wifidirecttestapp;

import android.os.Handler;

public interface Experiment {

    void startExperiment ();
    void setParameters (String... parameters);
    boolean isRunning();
    Handler getHandler();
    void setMessageManager(MessageManager messageManager);

}
