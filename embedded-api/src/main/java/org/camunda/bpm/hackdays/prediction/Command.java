package org.camunda.bpm.hackdays.prediction;

public interface Command<T> {

  T execute(CmmnPredictionService predictionService);
}
