package org.camunda.bpm.hackdays.prediction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.hackdays.prediction.model.ParsedPredictionModel;
import org.camunda.bpm.hackdays.prediction.model.ParsedPredictionModel.DiscreteVariable;
import org.camunda.bpm.hackdays.prediction.model.ParsedPredictionModel.ExpressionBasedVariable;
import org.camunda.bpm.hackdays.prediction.model.ParsedPredictionModel.VariableValue;

import com.github.thorbenlindhauer.factor.DiscreteFactor;
import com.github.thorbenlindhauer.inference.VariableEliminationInferencer;
import com.github.thorbenlindhauer.inference.variableelimination.MinFillEliminationStrategy;
import com.github.thorbenlindhauer.network.GraphicalModel;
import com.github.thorbenlindhauer.variable.Scope;

public class EstimateDistributionCmd implements Command<Map<String, Double>> {

  protected PredictionModel model;
  protected String variableName;
  protected Map<String, Integer> variableAssignment;
  protected Map<String, Object> expressionContext;
  
  public EstimateDistributionCmd(PredictionModel model, String variableName, Map<String, Integer> variableAssignment, Map<String, Object> expressionContext) {
    this.model = model;
    this.variableName = variableName;
    this.expressionContext = expressionContext;
    this.variableAssignment = variableAssignment;
  }

  public Map<String, Double> execute(CmmnPredictionService predictionService) {
    
    ParsedPredictionModel parsedModel = predictionService.parseModel(model);
    
    DiscreteVariable variable = parsedModel.getVariables().get(variableName);
    if (variable == null) {
      throw new CmmnPredictionException("Variable with name " + variableName + " not defined in model " + model.getName());
    }

    GraphicalModel<DiscreteFactor> graphicalModel = 
        parsedModel.toGraphicalModel(parsedModel.toPriors(model.getPriors()).values());
    
    VariableEliminationInferencer inferencer = new VariableEliminationInferencer(graphicalModel, new MinFillEliminationStrategy());
    
    Scope estimationScope = graphicalModel.getScope().subScope(variableName);
    
    Map<String, Integer> groundEvidence = groundEvidence(parsedModel, expressionContext);
    if (variableAssignment != null) {
      groundEvidence.putAll(variableAssignment);
    }
    
    List<String> evidencenVariables = new ArrayList<String>(groundEvidence.keySet());
    Scope evidenceScope = graphicalModel.getScope().subScope(evidencenVariables);
    int[] evidenceAssignment = new int[evidencenVariables.size()];
    
    int[] orderMapping = evidenceScope.createOrderMapping(evidencenVariables);
    
    for (int i = 0; i < evidencenVariables.size(); i++) {
      int targetIndex = orderMapping[i];
      evidenceAssignment[targetIndex] = groundEvidence.get(evidencenVariables.get(i));
    }
    
    Map<String, Double> probabilities = new HashMap<String, Double>();
    
    // TODO: this should be replaced by an inference API that provides the entire distribution with one call
    for (Map.Entry<Integer, VariableValue> variableValue : variable.getValues().entrySet()) {
      double valueProbability = inferencer.jointProbabilityConditionedOn(estimationScope, new int[]{variableValue.getKey()}, evidenceScope, evidenceAssignment);
      probabilities.put(variableValue.getValue().getName(), valueProbability);
    }
    
    return probabilities;
  } 
  
  protected Map<String, Integer> groundEvidence(ParsedPredictionModel model, Map<String, Object> expressionContext) {
    Map<String, ExpressionBasedVariable> variables = model.getExpressionBasedVariables();
    Map<String, Integer> groundValues = new HashMap<String, Integer>();
    
    for (Map.Entry<String, ExpressionBasedVariable> variable : variables.entrySet()) {
      
      Integer variableValue = null;
      try {
        variableValue = variable.getValue().determineExpressionValue(expressionContext);
      } catch (Exception e) {
        // ignore expressions that cannot be evaluated; this is most likely due to missing context which is ok; such
        // variables are not counted as evidence then
      }
      
      if (variableValue != null) {
        groundValues.put(variable.getKey(), variableValue);
      }
    }
    
    return groundValues;
  }

}
