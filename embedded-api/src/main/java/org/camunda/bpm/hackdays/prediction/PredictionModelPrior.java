package org.camunda.bpm.hackdays.prediction;

public class PredictionModelPrior {

  protected String modelId;
  protected String describedVariable;
  protected byte[] data;
  
  public String getModelId() {
    return modelId;
  }
  public void setModelId(String modelId) {
    this.modelId = modelId;
  }
  public String getDescribedVariable() {
    return describedVariable;
  }
  public void setDescribedVariable(String describedVariable) {
    this.describedVariable = describedVariable;
  }
  public byte[] getData() {
    return data;
  }
  public void setData(byte[] data) {
    this.data = data;
  }
}
