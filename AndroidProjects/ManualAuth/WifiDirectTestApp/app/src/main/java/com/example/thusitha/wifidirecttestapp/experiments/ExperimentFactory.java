package com.example.thusitha.wifidirecttestapp.experiments;

import com.example.thusitha.wifidirecttestapp.wfdMessaging.MessageManager;

public class ExperimentFactory {

    private MessageManager messageManager;

    public ExperimentFactory(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    public Experiment getExperiment (ExperimentType experimentType) {
        Experiment exp;
        switch (experimentType) {
            case THROUGHPUT:
                exp = ThroughputExperiment.getInstance();
                exp.setMessageManager(messageManager);
                return exp;
            case PACKET_LOSS:
                exp = PacketLossExperiment.getInstance();
                exp.setMessageManager(messageManager);
                return exp;
            case PACKET_DELAY:
                exp = PacketDelayExperiment.getInstance();
                exp.setMessageManager(messageManager);
                return exp;
            default:
                return null;
        }
    }

}
