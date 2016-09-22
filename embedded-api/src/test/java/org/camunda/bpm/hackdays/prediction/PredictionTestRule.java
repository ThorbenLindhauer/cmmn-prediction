package org.camunda.bpm.hackdays.prediction;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class PredictionTestRule extends TestWatcher {

  protected PooledDataSource dataSource;
  protected CmmnPredictionService predictionService;
  
  @Override
  protected void starting(Description description) {
    dataSource = new PooledDataSource(
        PredictionModelPersistenceTest.class.getClassLoader(), 
        "org.h2.Driver", 
        "jdbc:h2:mem:foo", 
        "", 
        "");
   
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
    
    dataSource.forceCloseAll();
  }
  
  public CmmnPredictionService getPredictionService() {
    return predictionService;
  }
  
  public DataSource getDataSource() {
    return dataSource;
  }
}
