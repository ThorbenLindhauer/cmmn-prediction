package org.camunda.bpm.hackdays.prediction;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.hackdays.prediction.model.ParsedPredictionModel;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.thorbenlindhauer.factor.DiscreteFactor;
import com.github.thorbenlindhauer.inference.VariableEliminationInferencer;
import com.github.thorbenlindhauer.inference.variableelimination.MinFillEliminationStrategy;
import com.github.thorbenlindhauer.learning.prior.ConditionalDiscreteDistributionPrior;
import com.github.thorbenlindhauer.network.GraphicalModel;
import com.github.thorbenlindhauer.variable.Scope;

public class GraphicalModelTest {

  @Rule
  public PredictionTestRule rule = new PredictionTestRule();
  
  protected CmmnPredictionService predictionService;
  
  @Before
  public void setUp() {
    predictionService = rule.getPredictionService();
  }
  
  @Test
  public void shouldCreateGraphicalModel() {
    // given
    PredictionModel model = new PredictionModel();
    model.setId("1");
    model.setName("foo");
    model.setResource(IoUtil.readInputStream(PredictionModelPrior.class.getClassLoader().getResourceAsStream("model.json"), "model"));
    
    ParsedPredictionModel parsedPredictionModel = predictionService.parseModel(model);
    
    Map<String, ConditionalDiscreteDistributionPrior> modelPriors = parsedPredictionModel.toPriors(parsedPredictionModel.generateRawPriors());
    modelPriors.get("foo").submitEvidence(new int[0], 1);
    modelPriors.get("bar").submitEvidence(new int[1], 0);
    
    // when
    GraphicalModel<DiscreteFactor> graphicalModel = parsedPredictionModel.toGraphicalModel(modelPriors.values());
    
    // then
    VariableEliminationInferencer inferencer = new VariableEliminationInferencer(graphicalModel, new MinFillEliminationStrategy());
    
    Scope barScope = graphicalModel.getScope().subScope("bar");
    double value0Probability = inferencer.jointProbability(barScope, new int[]{0});
    double value1Probability = inferencer.jointProbability(barScope, new int[]{1});
    
    assertThat(value0Probability).isGreaterThan(value1Probability);
  }
}
