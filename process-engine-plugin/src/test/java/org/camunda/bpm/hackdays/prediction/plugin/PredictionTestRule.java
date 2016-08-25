package org.camunda.bpm.hackdays.prediction.plugin;

import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.hackdays.prediction.CmmnPredictionService;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class PredictionTestRule extends TestWatcher {

  protected DataSource dataSource;
  protected CmmnPredictionService predictionService;
  
  protected ProcessEngineRule engineRule;
  
  public PredictionTestRule(ProcessEngineRule engineRule) {
    this.engineRule = engineRule;
  }
  
  @Override
  protected void starting(Description description) {
    this.dataSource = engineRule.getProcessEngineConfiguration().getDataSource();
   
    predictionService = CmmnPredictionService.build(dataSource);
    
    Connection connection = null;
    try {
      connection = dataSource.getConnection();
      predictionService.createDbTables(connection);
    } catch (Exception e) {
      throw new RuntimeException("Could not create tables", e);
    } finally {
      if (connection != null) {
        try {
          connection.close();
        } catch (SQLException e) {
          throw new RuntimeException("could not close connection", e);
        }
      }
    }
  }

  @Override
  protected void finished(Description description) {

    Connection connection = null;
    try {
      connection = dataSource.getConnection();
      predictionService.dropDbTables(connection);
    } catch (Exception e) {
      throw new RuntimeException("Could not create tables", e);
    } finally {
      if (connection != null) {
        try {
          connection.close();
        } catch (SQLException e) {
          throw new RuntimeException("could not close connection", e);
        }
      }
    }
  }
  
  public CmmnPredictionService getPredictionService() {
    return predictionService;
  }
  
  public DataSource getDataSource() {
    return dataSource;
  }
}
