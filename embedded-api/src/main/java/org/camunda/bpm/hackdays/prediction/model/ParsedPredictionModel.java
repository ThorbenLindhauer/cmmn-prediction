package org.camunda.bpm.hackdays.prediction.model;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.hackdays.prediction.model.ParsedPredictionModel.DiscreteVariable;

import com.github.thorbenlindhauer.factor.DiscreteFactor;
import com.github.thorbenlindhauer.network.DiscreteFactorBuilder;
import com.github.thorbenlindhauer.network.DiscreteModelBuilder;
import com.github.thorbenlindhauer.network.GraphicalModel;
import com.github.thorbenlindhauer.network.ModelBuilder;
import com.github.thorbenlindhauer.network.ScopeBuilder;

public class ParsedPredictionModel {

  protected Map<String, DiscreteVariable> variables;
  protected Map<String, List<String>> dependencies;
  
  public ParsedPredictionModel(Map<String, DiscreteVariable> variables, Map<String, List<String>> dependencies) {
    this.variables = variables;
    this.dependencies = dependencies;
  }

  public static class DiscreteVariable {
    protected String name;
    protected Map<String, String> valueExpressions;
    
    public String determineValue() {
      // TODO: evaluate expressions here and return the one that matches
      
      return null;
    }
    
    public String getName() {
      return name;
    }
    
    public Map<String, String> getValueExpressions() {
      return valueExpressions;
    }
  }
  
  public Map<String, List<String>> getDependencies() {
    return dependencies;
  }
  
  public Map<String, DiscreteVariable> getVariables() {
    return variables;
  }
  
  public GraphicalModel<DiscreteFactor> toGraphicalModel() {
    // TODO: build graphical model here; Must take a list of prior distributions
    
    ScopeBuilder scopeBuilder = GraphicalModel.create();
    for (DiscreteVariable variable : variables.values()) {
      scopeBuilder.discreteVariable(variable.name, variable.valueExpressions.size());
    }
    
    ModelBuilder<DiscreteFactor, DiscreteFactorBuilder<DiscreteModelBuilder>> modelBuilder = scopeBuilder.discreteNetwork();
    
    return null;
  }
}
