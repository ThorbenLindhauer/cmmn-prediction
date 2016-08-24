package org.camunda.bpm.hackdays.prediction;

import java.util.List;

public class PredictionModel {

  protected String id;
  protected String name;
  protected byte[] resource;

  protected List<PredictionModelPrior> priors;
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public byte[] getResource() {
    return resource;
  }
  public void setResource(byte[] resource) {
    this.resource = resource;
  }
  public List<PredictionModelPrior> getPriors() {
    return priors;
  }
  public void setPriors(List<PredictionModelPrior> priors) {
    this.priors = priors;
  }
}
