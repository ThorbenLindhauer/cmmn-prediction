package org.camunda.cmmn.prediction.dto;

/**
 * @author Askar Akhmerov
 */
public class CasePredictionTO {

  private String activityId;
  private Double probability;


  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public Double getProbability() {
    return probability;
  }

  public void setProbability(Double probability) {
    this.probability = probability;
  }
}
