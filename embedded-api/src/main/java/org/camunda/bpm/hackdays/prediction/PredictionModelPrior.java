package org.camunda.bpm.hackdays.prediction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
  
  // TODO: could also cache this field
  public double[][] getPrior() {
    if (data == null) {
      return null;
    }
    
    ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
    
    try {
      ObjectInputStream objectStream = new ObjectInputStream(byteStream);
      return (double[][]) objectStream.readObject();
    } catch (Exception e) {
      throw new CmmnPredictionException("Could not read prior tables", e);
    }
  }
  public void setPrior(double[][] tables) {
    
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    
    try {
      ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
      objectStream.writeObject(tables);
    } catch (Exception e) {
      throw new CmmnPredictionException("could not write prior tables", e);
    }
    
    data = byteStream.toByteArray();
  }
  
}
