package org.camunda.bpm.hackdays.prediction;

public class ParsedPredictionPrior {

  protected String modelId;
  protected String describedVariable;
  protected double[][] priorTables;
  
  public ParsedPredictionPrior(String modelId, String describedVariable) {
    this.modelId = modelId;
    this.describedVariable = describedVariable;
  }
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
  public double[][] getPriorTables() {
    return priorTables;
  }
  public void setPriorTables(double[][] priorTables) {
    this.priorTables = priorTables;
  }
}
