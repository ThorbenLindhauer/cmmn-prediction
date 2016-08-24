package org.camunda.bpm.hackdays.prediction.model;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import org.camunda.bpm.hackdays.prediction.CmmnPredictionException;
import org.camunda.bpm.hackdays.prediction.ParsedPredictionPrior;
import org.camunda.bpm.hackdays.prediction.PredictionModelPrior;

import com.github.thorbenlindhauer.factor.DiscreteFactor;
import com.github.thorbenlindhauer.learning.distribution.DirichletDistribution;
import com.github.thorbenlindhauer.learning.prior.ConditionalDiscreteDistributionPrior;
import com.github.thorbenlindhauer.learning.prior.DirichletPriorInitializer;
import com.github.thorbenlindhauer.learning.prior.UniformDirichletPriorInitializer;
import com.github.thorbenlindhauer.network.DiscreteFactorBuilder;
import com.github.thorbenlindhauer.network.DiscreteModelBuilder;
import com.github.thorbenlindhauer.network.GraphicalModel;
import com.github.thorbenlindhauer.network.ModelBuilder;
import com.github.thorbenlindhauer.network.ScopeBuilder;
import com.github.thorbenlindhauer.variable.Scope;

import de.odysseus.el.ExpressionFactoryImpl;
import de.odysseus.el.util.SimpleContext;
import de.odysseus.el.util.SimpleResolver;

public class ParsedPredictionModel {

  protected String id;
  protected Map<String, DiscreteVariable> variables;
  protected Map<String, List<String>> dependencies;
  protected Map<String, ParsedPredictionPrior> priors;
  
  public ParsedPredictionModel(
      String id,
      Map<String, DiscreteVariable> variables, 
      Map<String, List<String>> dependencies) {
    this.variables = variables;
    this.dependencies = dependencies;
    this.priors = new HashMap<String, ParsedPredictionPrior>();
    
    for (ParsedPredictionPrior prior : initializePriors()) {
      this.priors.put(prior.getDescribedVariable(), prior);
    }
  }

  public static class DiscreteVariable {
    protected String name;
    protected Map<Integer, VariableValue> values;
    
    public Integer determineValue(Map<String, Object> variables) {
      // TODO: evaluate expressions here and return the one that matches
      
      // TODO: cache factory and expressions
      ExpressionFactory expressionFactory = new ExpressionFactoryImpl();
      
      SimpleContext juelContext = new SimpleContext();
      ELResolver resolver = new SimpleResolver();
      juelContext.setELResolver(resolver);
      juelContext.putContext(Map.class, variables);
      
      for (Map.Entry<Integer, VariableValue> value : values.entrySet()) {
        ValueExpression expression = 
            expressionFactory.createValueExpression(new SimpleContext(), value.getValue().getExpression(), Boolean.class);
        Object expressionValue = expression.getValue(juelContext);
        
        if (!(expressionValue instanceof Boolean)) {
          throw new CmmnPredictionException("Value expression must evaluate to boolean");
        }
        
        if ((Boolean) expressionValue == true) {
          return value.getKey();
        }
      }
      
      throw new CmmnPredictionException("Could not determine value of variable " + name + ". No expression matched");
    }
    
    public String getName() {
      return name;
    }
    
    public Map<Integer, VariableValue> getValues() {
      return values;
    }
  }
  
  public static class VariableValue {
    protected String name;
    protected String expression;
    
    public VariableValue(String name, String expression) {
      this.name = name;
      this.expression = expression;
    }

    public String getName() {
      return name;
    }
    public String getExpression() {
      return expression;
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
      scopeBuilder.discreteVariable(variable.name, variable.values.size());
    }
    
    ModelBuilder<DiscreteFactor, DiscreteFactorBuilder<DiscreteModelBuilder>> modelBuilder = scopeBuilder.discreteNetwork();
    
    return null;
  }
  
  public Scope toScope() {
    
    ScopeBuilder scopeBuilder = GraphicalModel.create();
    for (DiscreteVariable variable : variables.values()) {
      scopeBuilder.discreteVariable(variable.name, variable.values.size());
    }
    
    return scopeBuilder.buildScope();
  }
  
  public List<PredictionModelPrior> toRawPriors() {
    List<PredictionModelPrior> priors = new ArrayList<PredictionModelPrior>();
    
    for (ParsedPredictionPrior parsedPrior : this.priors.values()) {
      PredictionModelPrior rawPrior = new PredictionModelPrior();
      
      rawPrior.setModelId(id);
      rawPrior.setDescribedVariable(parsedPrior.getDescribedVariable());

      byte[] data = null;
      
      if (parsedPrior.getPriorTables() != null) {
        ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
        try {
          ObjectOutputStream outStream = new ObjectOutputStream(byteOutStream);
          outStream.writeObject(parsedPrior.getPriorTables());
          data = byteOutStream.toByteArray();
        } catch (Exception e) {
          throw new CmmnPredictionException("Cannot serialize prior distribution", e);
        }
      }
      
      rawPrior.setData(data);
    }
    
    return priors;
    
  }
  
  public List<ParsedPredictionPrior> initializePriors() {
    List<ParsedPredictionPrior> priors = new ArrayList<ParsedPredictionPrior>();
    
    for (Map.Entry<String, List<String>> dependency : dependencies.entrySet()) {
      String describedVariable = dependency.getKey();
      
      ParsedPredictionPrior prior = new ParsedPredictionPrior(id, describedVariable);
      priors.add(prior);
    }
    
    return priors;
  }
  
  public List<ConditionalDiscreteDistributionPrior> toPriors() {
    
    Scope scope = toScope();

    List<ConditionalDiscreteDistributionPrior> priors = new ArrayList<ConditionalDiscreteDistributionPrior>();
    
    for (ParsedPredictionPrior parsedPrior : this.priors.values()) {
      String describedVariable = parsedPrior.getDescribedVariable();
      List<String> dependencies = this.dependencies.get(describedVariable);
      Scope conditioningScope = scope.subScope(dependencies);
      Scope distributionScope = conditioningScope.union(scope.subScope(describedVariable));
      
      DirichletPriorInitializer priorInitializer = new PersistentPriorInitializer();
      
      if (parsedPrior.getPriorTables() == null) {
        priorInitializer = new UniformDirichletPriorInitializer();
      }
      
      ConditionalDiscreteDistributionPrior modelPrior = new ConditionalDiscreteDistributionPrior(
          distributionScope,
          conditioningScope,
          priorInitializer);
      priors.add(modelPrior);
    }
    
    return priors;
  }
  
  public void updatePriors(List<ConditionalDiscreteDistributionPrior> priors) {
    for (ConditionalDiscreteDistributionPrior prior : priors) {
      ParsedPredictionPrior parsedPrior = this.priors.get(prior.getDescribedScope().getVariables().iterator().next().getId()); // assuming there is exactly one variable
      DirichletDistribution[] conditionalAssignmentPriors = prior.getPriors();
      double[][] tables = new double[prior.getDescribedScope().getNumDistinctValues()][conditionalAssignmentPriors.length];
      
      for (int i = 0; i < conditionalAssignmentPriors.length; i++) {
        tables[i] = conditionalAssignmentPriors[i].getParameters();
      }
      
      parsedPrior.setPriorTables(tables);
    }
  }
  
  public class PersistentPriorInitializer implements DirichletPriorInitializer {

    public void initialize(DirichletDistribution prior, Scope describingScope, Scope conditioningScope, int[] conditioningAssignment) {
      int priorIndex = conditioningScope.getIndexCoder().getIndexForAssignment(conditioningAssignment);
      ParsedPredictionPrior parsedPredictionPrior = 
         priors.get(describingScope.getDiscreteVariables().iterator().next().getId()); // assuming describing scope has exactly one variable
      double[] table = parsedPredictionPrior.getPriorTables()[priorIndex];
      prior.setParameters(table);
    }
    
  }
  
}
