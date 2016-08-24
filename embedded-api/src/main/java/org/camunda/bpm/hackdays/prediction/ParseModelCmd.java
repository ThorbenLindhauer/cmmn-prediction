package org.camunda.bpm.hackdays.prediction;

import java.io.ByteArrayInputStream;

import org.camunda.bpm.hackdays.prediction.model.ParsedPredictionModel;
import org.camunda.bpm.hackdays.prediction.model.PredictionModelParser;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ParseModelCmd implements Command<ParsedPredictionModel> {

  protected PredictionModel model;

  public ParseModelCmd(PredictionModel model) {
    this.model = model;
  }
  
  public ParsedPredictionModel execute(CmmnPredictionService predictionService) {
    PredictionModelParser parser = new PredictionModelParser(new ObjectMapper());
    
    return parser.parse(new ByteArrayInputStream(model.getResource()));
  }
}
