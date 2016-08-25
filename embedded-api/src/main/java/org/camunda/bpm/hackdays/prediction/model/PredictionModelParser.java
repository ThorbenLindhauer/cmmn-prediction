package org.camunda.bpm.hackdays.prediction.model;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.hackdays.prediction.CmmnPredictionException;
import org.camunda.bpm.hackdays.prediction.model.ParsedPredictionModel.DiscreteVariable;
import org.camunda.bpm.hackdays.prediction.model.ParsedPredictionModel.VariableValue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;;

public class PredictionModelParser {
  
  protected ObjectMapper objectMapper;
  
  public PredictionModelParser(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public ParsedPredictionModel parse(String modelId, InputStream stream) {
    
    JsonNode jsonNode;
    try {
      jsonNode = objectMapper.readTree(stream);
    } catch (Exception e) {
      throw new CmmnPredictionException("Could not parse prediction model", e);
    }
    
    JsonNode variables = jsonNode.get("variables");
    
    Iterator<String> variableNamesIt = variables.fieldNames();
    
    Map<String, DiscreteVariable> parsedVariables = new HashMap<String, ParsedPredictionModel.DiscreteVariable>();
    
    while (variableNamesIt.hasNext()) {
      String variableName = variableNamesIt.next();
      DiscreteVariable parsedVariable = new DiscreteVariable();
      parsedVariable.name = variableName;
      parsedVariables.put(variableName, parsedVariable);
      
      JsonNode variable = variables.get(variableName);
      
      Map<Integer, VariableValue> values = new HashMap<Integer, VariableValue>();
      parsedVariable.values = values;
      
      JsonNode categories = variable.get("categories");
      
      Iterator<JsonNode> categoriesIt = categories.elements();
      int i = 0;
      while (categoriesIt.hasNext()) {
        JsonNode category = categoriesIt.next();
        
        VariableValue value = new VariableValue(
            category.get("label").asText(), 
            "${" + category.get("expression").asText() + "}");
        values.put(i, value);
        i++;
      }
    }
    
    JsonNode factors = jsonNode.get("dependencies");
    
    Iterator<String> dependencyVariableNamesIt = factors.fieldNames();
    
    Map<String, List<String>> parsedDependencies = new HashMap<String, List<String>>();
    
    while (dependencyVariableNamesIt.hasNext()) {
      String variableName = dependencyVariableNamesIt.next();
      Iterator<JsonNode> parentVariableNames = factors.get(variableName).elements();
      
      List<String> parentVariables = new ArrayList<String>();
      parsedDependencies.put(variableName, parentVariables);
      
      while (parentVariableNames.hasNext()) {
        String parentVariableName = parentVariableNames.next().asText();
        parentVariables.add(parentVariableName);
      }
    }
    
    Set<String> remainingVariables = new HashSet<String>(parsedVariables.keySet());
    remainingVariables.removeAll(parsedDependencies.keySet());
    
    for (String variable : remainingVariables) {
      parsedDependencies.put(variable, Collections.<String>emptyList());
    }
    
    return new ParsedPredictionModel(modelId, parsedVariables, parsedDependencies);
  }
}
