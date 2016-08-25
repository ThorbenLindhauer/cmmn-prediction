package org.camunda.bpm.hackdays.prediction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.hackdays.prediction.model.ParsedPredictionModel;
import org.camunda.bpm.hackdays.prediction.model.ParsedPredictionModel.DiscreteVariable;
import org.camunda.bpm.hackdays.prediction.model.ParsedPredictionModel.VariableValue;

import com.github.thorbenlindhauer.factor.DiscreteFactor;
import com.github.thorbenlindhauer.inference.VariableEliminationInferencer;
import com.github.thorbenlindhauer.inference.variableelimination.MinFillEliminationStrategy;
import com.github.thorbenlindhauer.network.GraphicalModel;
import com.github.thorbenlindhauer.variable.IndexMapper;
import com.github.thorbenlindhauer.variable.Scope;

public class EstimateDistributionCmd implements Command<Map<String, Double>> {

  protected String modelName;
  protected String variableName;
  protected Map<String, Object> expressionContext;
  
  public EstimateDistributionCmd(String modelName, String variableName, Map<String, Object> expressionContext) {
    this.modelName = modelName;
    this.variableName = variableName;
    this.expressionContext = expressionContext;
  }

  public Map<String, Double> execute(CmmnPredictionService predictionService) {
    PredictionModel model = predictionService.getModel(modelName);
    
    if (model == null) {
      throw new CmmnPredictionException("Model with name " + modelName + " does not exist");
    }
    
    ParsedPredictionModel parsedModel = predictionService.parseModel(model);
    DiscreteVariable variable = parsedModel.getVariables().get(variableName);
    if (variable == null) {
      throw new CmmnPredictionException("Variable with name " + variableName + " not defined in model " + modelName);
    }

    GraphicalModel<DiscreteFactor> graphicalModel = 
        parsedModel.toGraphicalModel(parsedModel.toPriors(model.getPriors()).values());
    
    VariableEliminationInferencer inferencer = new VariableEliminationInferencer(graphicalModel, new MinFillEliminationStrategy());
    
    Scope estimationScope = graphicalModel.getScope().subScope(variableName);
    
    Map<String, Integer> groundEvidence = groundEvidence(parsedModel, expressionContext);
    List<String> evidencenVariables = new ArrayList<String>(groundEvidence.keySet());
    Scope evidenceScope = graphicalModel.getScope().subScope(evidencenVariables);
    int[] evidenceAssignment = new int[evidencenVariables.size()];
    
    // TODO: map values to index via index coder mapping
    int[] orderMapping = evidenceScope.createOrderMapping(evidencenVariables);
    IndexMapper indexMapper = evidenceScope.getIndexCoder().getIndexMapper(orderMapping);
    
    for (int i = 0; i < evidencenVariables.size(); i++) {
      int targetIndex = indexMapper.mapIndex(i);
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
    Map<String, DiscreteVariable> variables = model.getVariables();
    Map<String, Integer> groundValues = new HashMap<String, Integer>();
    
    for (Map.Entry<String, DiscreteVariable> variable : variables.entrySet()) {
      Integer variableValue = null;
      try {
        variableValue = variable.getValue().determineValue(expressionContext);
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
