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
import org.camunda.bpm.hackdays.prediction.model.ParsedPredictionModel.ExpressionBasedVariable;
import org.camunda.bpm.hackdays.prediction.model.ParsedPredictionModel.VariableValue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;;

public class PredictionModelParser {
  
  public static final String VARIABLE_TYPE_CUSTOM = "custom";
  public static final String VARIABLE_TYPE_BINARY = "binary";

  public static final String VARIABLE_TYPE_BINARY_FALSE = "false";
  public static final String VARIABLE_TYPE_BINARY_TRUE = "true";
  
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
    
    
    Map<String, DiscreteVariable> binaryVariables = new HashMap<String, DiscreteVariable>();
    Map<String, ExpressionBasedVariable> expressionBasedVariables = new HashMap<String, ExpressionBasedVariable>();
    
    Iterator<String> variableNamesIt = variables.fieldNames();
    while (variableNamesIt.hasNext()) {
      String variableName = variableNamesIt.next();
      JsonNode variable = variables.get(variableName);

      JsonNode variableType = variable.get("type");
      if (variableType == null) {
        throw new CmmnPredictionException("Variable attribute 'type' required");
      }
      
      if (VARIABLE_TYPE_CUSTOM.equals(variableType.asText())) {
        ExpressionBasedVariable parsedVariable = parseCustomVariableValues(variableName, variable);
        expressionBasedVariables.put(variableName, parsedVariable);
      }
      else if (VARIABLE_TYPE_BINARY.equals(variableType.asText())) {
        DiscreteVariable parsedVariable = parseBinaryVariableValues(variableName);
        binaryVariables.put(variableName, parsedVariable);
      }
      else {
        throw new CmmnPredictionException("Unsupported variable type: " + variableType);
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

    Set<String> remainingVariables = new HashSet<String>(expressionBasedVariables.keySet());
    remainingVariables.addAll(binaryVariables.keySet());
    remainingVariables.removeAll(parsedDependencies.keySet());
    
    for (String variable : remainingVariables) {
      parsedDependencies.put(variable, Collections.<String>emptyList());
    }
    
    return new ParsedPredictionModel(modelId, binaryVariables, expressionBasedVariables, parsedDependencies);
  }

  protected ExpressionBasedVariable parseCustomVariableValues(String variableName, JsonNode variable) {

    ExpressionBasedVariable parsedVariable = new ExpressionBasedVariable();
    parsedVariable.name = variableName;
    
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
  
    return parsedVariable;
  }

  protected DiscreteVariable parseBinaryVariableValues(String variableName) {
    DiscreteVariable parsedVariable = new DiscreteVariable();
    parsedVariable.name = variableName;
    parsedVariable.values = new HashMap<Integer, ParsedPredictionModel.VariableValue>();

    VariableValue falseValue = new VariableValue(VARIABLE_TYPE_BINARY_FALSE, null);
    parsedVariable.values.put(0, falseValue);
    VariableValue trueValue = new VariableValue(VARIABLE_TYPE_BINARY_TRUE, null);
    parsedVariable.values.put(1, trueValue);
  
    return parsedVariable;
    
  }
  
}
