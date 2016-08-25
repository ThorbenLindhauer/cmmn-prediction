package org.camunda.bpm.hackdays.prediction.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ResourceBundleELResolver;
import javax.el.ValueExpression;

import org.camunda.bpm.hackdays.prediction.CmmnPredictionException;
import org.camunda.bpm.hackdays.prediction.PredictionModelPrior;
import org.camunda.bpm.hackdays.prediction.RootMapELResolver;

import com.github.thorbenlindhauer.factor.DiscreteFactor;
import com.github.thorbenlindhauer.learning.distribution.DirichletDistribution;
import com.github.thorbenlindhauer.learning.prior.ConditionalDiscreteDistributionPrior;
import com.github.thorbenlindhauer.learning.prior.DirichletPriorInitializer;
import com.github.thorbenlindhauer.learning.prior.UniformDirichletPriorInitializer;
import com.github.thorbenlindhauer.network.DiscreteModelBuilder;
import com.github.thorbenlindhauer.network.DiscreteModelBuilderImpl;
import com.github.thorbenlindhauer.network.GraphicalModel;
import com.github.thorbenlindhauer.network.ScopeBuilder;
import com.github.thorbenlindhauer.variable.Scope;

import de.odysseus.el.ExpressionFactoryImpl;
import de.odysseus.el.util.SimpleContext;
import de.odysseus.el.util.SimpleResolver;

public class ParsedPredictionModel {

  protected String id;
  protected Map<String, ExpressionBasedVariable> expressionBasedVariables;
  protected Map<String, DiscreteVariable> binaryVariables;
  protected Map<String, DiscreteVariable> variables;
  protected Map<String, List<String>> dependencies;
  
  public ParsedPredictionModel(
      String id,
      Map<String, DiscreteVariable> binaryVariables,
      Map<String, ExpressionBasedVariable> expressionBasedVariables,
      Map<String, List<String>> dependencies) {
    this.id = id;
    this.binaryVariables = binaryVariables;
    this.expressionBasedVariables = expressionBasedVariables;
    this.variables = new HashMap<String, ParsedPredictionModel.DiscreteVariable>();
    this.variables.putAll(expressionBasedVariables);
    this.variables.putAll(binaryVariables);
    this.dependencies = dependencies;
  }
  
  public static class ExpressionBasedVariable extends DiscreteVariable {
    protected static final ELResolver EL_RESOLVER = new CompositeELResolver() {
      {
        add(new RootMapELResolver());
        add(new ArrayELResolver(true));
        add(new ListELResolver(true));
        add(new MapELResolver(true));
        add(new ResourceBundleELResolver());
        add(new BeanELResolver(true));
      }
    };

    /**
     * May return null if no matching value could be determined (e.g. because this variable does not support expressions)
     */
    public Integer determineExpressionValue(Map<String, Object> variables) {
      
      // TODO: cache factory and expressions
      ExpressionFactory expressionFactory = new ExpressionFactoryImpl();
      
      SimpleContext juelContext = new SimpleContext();
      juelContext.setELResolver(EL_RESOLVER);
      juelContext.putContext(Map.class, variables);
      
      for (Map.Entry<Integer, VariableValue> value : values.entrySet()) {
        String expressionString = value.getValue().getExpression();
        
        ValueExpression expression = 
            expressionFactory.createValueExpression(new SimpleContext(), expressionString, Boolean.class);
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
    
  }

  public static class DiscreteVariable {
    
    protected String name;
    protected Map<Integer, VariableValue> values;

    
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
  
  public Map<String, ExpressionBasedVariable> getExpressionBasedVariables() {
    return expressionBasedVariables;
  }
  
  public Map<String, DiscreteVariable> getBinaryVariables() {
    return binaryVariables;
  }
  
  public GraphicalModel<DiscreteFactor> toGraphicalModel(Collection<ConditionalDiscreteDistributionPrior> modelPriors) {
    DiscreteModelBuilder modelBuilder = new DiscreteModelBuilderImpl(toScope());
    
    for (ConditionalDiscreteDistributionPrior modelPrior : modelPriors) {
      modelBuilder.factor().scope(modelPrior.getScope().getVariableIds()).basedOnTable(modelPrior.toCanonicalValueVector());
    }
    
    return modelBuilder.build();
  }
  
  public Scope toScope() {
    
    ScopeBuilder scopeBuilder = GraphicalModel.create();
    for (DiscreteVariable variable : variables.values()) {
      scopeBuilder.discreteVariable(variable.name, variable.values.size());
    }
    
    return scopeBuilder.buildScope();
  }
  
  public List<PredictionModelPrior> generateRawPriors() {
    List<PredictionModelPrior> priors = new ArrayList<PredictionModelPrior>();
    
    for (Entry<String, List<String>> dependency : dependencies.entrySet()) {
      PredictionModelPrior rawPrior = new PredictionModelPrior();
      
      rawPrior.setModelId(id);
      rawPrior.setDescribedVariable(dependency.getKey());
      
      priors.add(rawPrior);
      
    }
    
    return priors;
  }
  
  public Map<String, ConditionalDiscreteDistributionPrior> toPriors(List<PredictionModelPrior> priors) {
    
    Scope scope = toScope();

    Map<String, ConditionalDiscreteDistributionPrior> graphicalModelPriors = new HashMap<String, ConditionalDiscreteDistributionPrior>();
    
    for (PredictionModelPrior prior : priors) {
      String describedVariable = prior.getDescribedVariable();
      List<String> dependencies = this.dependencies.get(describedVariable);
      Scope conditioningScope = scope.subScope(dependencies);
      Scope distributionScope = conditioningScope.union(scope.subScope(describedVariable));
      
      DirichletPriorInitializer priorInitializer = new PersistentPriorInitializer(prior.getPrior());
      
      if (prior.getPrior() == null) {
        priorInitializer = new UniformDirichletPriorInitializer();
      }
      
      ConditionalDiscreteDistributionPrior modelPrior = new ConditionalDiscreteDistributionPrior(
          distributionScope,
          conditioningScope,
          priorInitializer);
      
      graphicalModelPriors.put(describedVariable, modelPrior);
    }
    
    return graphicalModelPriors;
  }
  
  public void updatePriors(Collection<ConditionalDiscreteDistributionPrior> graphicalModelPriors, Collection<PredictionModelPrior> persistentPriors) {
    Map<String, PredictionModelPrior> persistentPriorMap = new HashMap<String, PredictionModelPrior>();
    for (PredictionModelPrior prior : persistentPriors) {
      persistentPriorMap.put(prior.getDescribedVariable(), prior);
    }
    
    for (ConditionalDiscreteDistributionPrior modelPrior : graphicalModelPriors) {
      Scope describedScope = modelPrior.getDescribedScope();
      String describedVariable = describedScope.getVariables().iterator().next().getId(); // assuming there is exactly one variable
      PredictionModelPrior persistentPrior = persistentPriorMap.get(describedVariable);
      DirichletDistribution[] conditionalAssignmentPriors = modelPrior.getPriors();
      
      double[][] tables = new double[conditionalAssignmentPriors.length][];
      
      for (int i = 0; i < conditionalAssignmentPriors.length; i++) {
        tables[i] = conditionalAssignmentPriors[i].getParameters();
      }
      
      persistentPrior.setPrior(tables);
    }
  }
  
  public static class PersistentPriorInitializer implements DirichletPriorInitializer {

    protected double[][] tables;
    
    public PersistentPriorInitializer(double[][] tables) {
      this.tables = tables;
    }

    public void initialize(DirichletDistribution prior, Scope describingScope, Scope conditioningScope, int[] conditioningAssignment) {
      int priorIndex = conditioningScope.getIndexCoder().getIndexForAssignment(conditioningAssignment);
      double[] table = tables[priorIndex];
      prior.setParameters(table);
    }
    
  }
  
}
