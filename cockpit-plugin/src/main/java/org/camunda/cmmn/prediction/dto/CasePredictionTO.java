package org.camunda.cmmn.prediction.dto;

/**
 * @author Askar Akhmerov
 */
public class CasePredictionTO {

  private String activityId;
  private String probability;


  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public String getProbability() {
    return probability;
  }

  public void setProbability(String probability) {
    this.probability = probability;
  }
}
