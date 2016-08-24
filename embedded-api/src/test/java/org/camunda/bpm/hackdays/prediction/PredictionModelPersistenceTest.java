package org.camunda.bpm.hackdays.prediction;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.junit.Before;
import org.junit.Test;

public class PredictionModelPersistenceTest {

  // TODO: this does currently not detect conflicting updates, since it does not use
  // the engine's command executor infrastructure. Could use that if we need it
  
  protected CmmnPredictionService predictionService;
  
  @Before
  public void setUp() throws SQLException
  {

    PooledDataSource pooledDataSource = new PooledDataSource(
          PredictionModelPersistenceTest.class.getClassLoader(), 
          "org.h2.Driver", 
          "jdbc:h2:mem:foo", 
          "", 
          "");
     
    predictionService = CmmnPredictionService.build(pooledDataSource);
    
    predictionService.createDbTables(pooledDataSource.getConnection());
    pooledDataSource.getConnection().close();
  }
  
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
}
