package org.camunda.bpm.hackdays.prediction;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.hackdays.prediction.model.ParsedPredictionModel;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class PredictionModelPersistenceTest {

  // TODO: this does currently not detect conflicting updates, since it does not use
  // the engine's command executor infrastructure. Could use that if we need it
  
  protected CmmnPredictionService predictionService;
  
  @Rule
  public PredictionTestRule rule = new PredictionTestRule();
  
  @Before
  public void initService() {
    predictionService = rule.getPredictionService();
  }
  
//  @Before
//  public void setUp() throws SQLException
//  {
//
//    PooledDataSource pooledDataSource = new PooledDataSource(
//          PredictionModelPersistenceTest.class.getClassLoader(), 
//          "org.h2.Driver", 
//          "jdbc:h2:mem:foo", 
//          "", 
//          "");
//     
//    predictionService = CmmnPredictionService.build(pooledDataSource);
//    
//    predictionService.createDbTables(pooledDataSource.getConnection());
//    pooledDataSource.getConnection().close();
//  }
//  
//  @After
//  public void tearDown() {
//    
//  }
//  
  @Test
  public void shouldPersistModel() {
    // given
    PredictionModel model = new PredictionModel();
    model.setId("1");
    model.setName("foo");
    byte[] resource = "foo".getBytes(StandardCharsets.UTF_8);
    model.setResource(resource);
    
    // when
    predictionService.insertModel(model);
    
    // then
    PredictionModel returnedModel = predictionService.getModel("foo");
    assertThat(returnedModel.getId()).isEqualTo("1");
    assertThat(returnedModel.getName()).isEqualTo("foo");
    assertThat(returnedModel.getResource()).containsExactly(resource);
  }
  
  @Test
  public void shouldPersistModelWithPriors() {
    // given
    PredictionModel model = new PredictionModel();
    model.setId("1");
    model.setName("foo");
    model.setResource(IoUtil.readInputStream(PredictionModelPrior.class.getClassLoader().getResourceAsStream("model.json"), "model"));
    
    ParsedPredictionModel parsedPredictionModel = predictionService.parseModel(model);
    model.setPriors(parsedPredictionModel.generateRawPriors());
    
    // when
    predictionService.insertModel(model);
    
    // then
    PredictionModel returnedModel = predictionService.getModel("foo");
    assertThat(returnedModel.getPriors()).hasSize(2);
    Set<String> describedVariables = new HashSet<String>();
    
    for (PredictionModelPrior prior : returnedModel.getPriors()) {
      describedVariables.add(prior.getDescribedVariable());
    }
    
    assertThat(describedVariables).contains("foo", "bar");
    
    for (PredictionModelPrior prior : returnedModel.getPriors()) {
      assertThat(prior.getData()).isNull();
    }
  }
  
  @Test
  public void shouldUpdateModelWithPriors() {
    // given
    PredictionModel model = new PredictionModel();
    model.setId("1");
    model.setName("foo");
    model.setResource(IoUtil.readInputStream(PredictionModelPrior.class.getClassLoader().getResourceAsStream("model.json"), "model"));
    
    ParsedPredictionModel parsedPredictionModel = predictionService.parseModel(model);
    model.setPriors(parsedPredictionModel.generateRawPriors());
    predictionService.insertModel(model);
    
    // when
    for (PredictionModelPrior prior : model.getPriors()) {
      prior.setPrior(new double[][]{{1}, {2}});
    }
    predictionService.updateModel(model);
    
    // then
    PredictionModel returnedModel = predictionService.getModel("foo");
    assertThat(returnedModel.getPriors()).hasSize(2);
    Set<String> describedVariables = new HashSet<String>();
    
    for (PredictionModelPrior prior : returnedModel.getPriors()) {
      describedVariables.add(prior.getDescribedVariable());
    }
    
    assertThat(describedVariables).contains("foo", "bar");
    
    for (PredictionModelPrior prior : returnedModel.getPriors()) {
      double[][] priorTable = prior.getPrior();
      assertThat(priorTable).isNotNull();
      assertThat(priorTable.length).isEqualTo(2);
      assertThat(priorTable[0].length).isEqualTo(1);
      assertThat(priorTable[0][0]).isEqualTo(1);
      assertThat(priorTable[1].length).isEqualTo(1);
      assertThat(priorTable[1][0]).isEqualTo(2);
    }
  }
}
