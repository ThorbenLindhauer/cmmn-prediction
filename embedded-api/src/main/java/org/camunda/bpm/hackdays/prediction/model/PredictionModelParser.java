package org.camunda.bpm.hackdays.prediction.model;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.hackdays.prediction.CmmnPredictionException;
import org.camunda.bpm.hackdays.prediction.model.ParsedPredictionModel.DiscreteVariable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;;

public class PredictionModelParser {
  
  protected ObjectMapper objectMapper;
  
  public PredictionModelParser(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public ParsedPredictionModel parse(InputStream stream) {
    
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
      
      Map<String, String> expressions = new HashMap<String, String>();
      parsedVariable.valueExpressions = expressions;
      
      JsonNode categories = variable.get("categories");
      
      Iterator<JsonNode> categoriesIt = categories.elements();
      while (categoriesIt.hasNext()) {
        JsonNode category = categoriesIt.next();
        expressions.put(category.get("label").asText(), "${" + category.get("expression").asText() + "}");
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
    
    return new ParsedPredictionModel(parsedVariables, parsedDependencies);
  }
}
