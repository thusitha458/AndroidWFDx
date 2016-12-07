package com.example.thusitha.wifidirecttestapp.experiments;


import com.example.thusitha.wifidirecttestapp.logging.FileLoggerFactory;
import com.example.thusitha.wifidirecttestapp.logging.LoggerType;

public class PacketDelayExperiment extends PeriodicSender {

    private static PacketDelayExperiment instance = new PacketDelayExperiment();

    private PacketDelayExperiment () {
    }

    public static PacketDelayExperiment getInstance () {
        return instance;
    }

    @Override
    protected void setFileLogger () {
        fileLogger = (new FileLoggerFactory()).getFileLogger(LoggerType.LOGGER_1);

        String fileName = "pd-"
                .concat(String.valueOf(messageSize).concat("-"))
                .concat(String.valueOf(periodMS).concat("-"))
                .concat(String.valueOf(distanceM).concat("-"));
        if (isSender) {
            fileName = fileName.concat("s.txt");
        } else {
            fileName = fileName.concat("r.txt");
        }

        fileLogger.createLogFile(fileName);
    }

}
