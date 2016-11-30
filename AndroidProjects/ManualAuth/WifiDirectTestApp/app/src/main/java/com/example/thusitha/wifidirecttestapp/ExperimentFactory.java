package com.example.thusitha.wifidirecttestapp;


public class ExperimentFactory {

    public enum Experiments {
        EXPERIMENT_1
    }

    private MessageHandler messageHandler;

    public ExperimentFactory(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    public Experiment getExperiment (Experiments experiment) {
        switch (experiment) {
            case EXPERIMENT_1:
                Experiment exp = Experiment1.getInstance();
                exp.setMessageHandler(messageHandler);
                return exp;
            default:
                return null;
        }
    }

}
