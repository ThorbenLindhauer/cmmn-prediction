package org.camunda.bpm.hackdays.prediction;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Map;

import org.assertj.core.data.Offset;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.hackdays.prediction.model.ParsedPredictionModel;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ProbabilityEstimationTest {

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
    model.setPriors(parsedPredictionModel.generateRawPriors());
    
    predictionService.insertModel(model);
    
    // when
    Map<String, Double> estimate = predictionService.estimate("foo", "bar", Collections.<String, Object>singletonMap("intVal", 75));
    
    // then
    assertThat(estimate.size()).isEqualTo(2);
    assertThat(estimate.get("barCat1")).isEqualTo(0.0d, Offset.offset(0.0001d));
    assertThat(estimate.get("barCat2")).isEqualTo(1.0d, Offset.offset(0.0001d));
  }

}
