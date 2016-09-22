package org.camunda.bpm.hackdays.prediction;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.hackdays.prediction.model.ParsedPredictionModel;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.thorbenlindhauer.learning.prior.ConditionalDiscreteDistributionPrior;

public class GraphicalModelPriorsTest {

  @Rule
  public PredictionTestRule rule = new PredictionTestRule();
  
  protected CmmnPredictionService predictionService;
  
  @Before
  public void setUp() {
    predictionService = rule.getPredictionService();
  }
  
  @Test
  public void shouldGenerateInitialUniformPriors() {
    // given
    PredictionModel model = new PredictionModel();
    model.setId("1");
    model.setName("foo");
    model.setResource(IoUtil.readInputStream(PredictionModelPrior.class.getClassLoader().getResourceAsStream("model.json")));
    
    ParsedPredictionModel parsedPredictionModel = predictionService.parseModel(model);
    
    // when
    Map<String, ConditionalDiscreteDistributionPrior> modelPriors 
      = parsedPredictionModel.toPriors(parsedPredictionModel.generateRawPriors());
    
    // then
    assertThat(modelPriors).hasSize(2);
    ConditionalDiscreteDistributionPrior fooPrior = modelPriors.get("foo");
    assertThat(fooPrior.getDescribedScope().getVariableIds()).containsExactly("foo");
    assertThat(fooPrior.getPriors()).hasSize(1);

    ConditionalDiscreteDistributionPrior barPrior = modelPriors.get("bar");
    assertThat(barPrior.getDescribedScope().getVariableIds()).containsExactly("bar");
    assertThat(barPrior.getPriors()).hasSize(2); // two conditional assignments
  }
  
  @Test
  public void shouldUpdatePriors() {
    // given
    PredictionModel model = new PredictionModel();
    model.setId("1");
    model.setName("foo");
    model.setResource(IoUtil.readInputStream(PredictionModelPrior.class.getClassLoader().getResourceAsStream("model2.json")));
    
    ParsedPredictionModel parsedPredictionModel = predictionService.parseModel(model);
    List<PredictionModelPrior> persistentPriors = parsedPredictionModel.generateRawPriors();
    Map<String, ConditionalDiscreteDistributionPrior> modelPriors 
      = parsedPredictionModel.toPriors(persistentPriors);
    
    ConditionalDiscreteDistributionPrior distributionPrior = modelPriors.get("foo");
    distributionPrior.submitEvidence(new int[0], 0);
    
    // when
    parsedPredictionModel.updatePriors(modelPriors.values(), persistentPriors);
    
    // then
    double[][] table = persistentPriors.get(0).getPrior();
    assertThat(table.length).isEqualTo(1);
    assertThat(table[0].length).isEqualTo(2);
    assertThat(table[0][0]).isEqualTo(2);
    assertThat(table[0][1]).isEqualTo(1);
    
  }
}
