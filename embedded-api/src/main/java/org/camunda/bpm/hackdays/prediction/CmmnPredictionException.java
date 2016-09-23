package org.camunda.bpm.hackdays.prediction;

public class CmmnPredictionException extends RuntimeException {
  
  private static final long serialVersionUID = 1L;

  public CmmnPredictionException(String message) {
    super(message);
  }
  
  public CmmnPredictionException(String message, Exception cause) {
    super(message, cause);
  }
}
