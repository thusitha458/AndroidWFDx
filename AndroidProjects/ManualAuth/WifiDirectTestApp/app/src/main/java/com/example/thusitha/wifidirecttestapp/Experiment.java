package com.example.thusitha.wifidirecttestapp;


import android.os.Handler;

import java.util.ArrayList;

public interface Experiment {

    void startExperiment ();
    void setParameters (String... parameters);
    boolean isRunning();
    Handler getHandler();

}
