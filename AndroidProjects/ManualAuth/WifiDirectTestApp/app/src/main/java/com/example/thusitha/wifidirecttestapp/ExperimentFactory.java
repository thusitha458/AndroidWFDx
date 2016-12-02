package com.example.thusitha.wifidirecttestapp;


public class ExperimentFactory {

    private MessageManager messageManager;

    public ExperimentFactory(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    public Experiment getExperiment (ExperimentType experimentType) {
        switch (experimentType) {
            case EXPERIMENT_1:
                Experiment exp = Experiment1.getInstance();
                exp.setMessageManager(messageManager);
                return exp;
            default:
                return null;
        }
    }

}
