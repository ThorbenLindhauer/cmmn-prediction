package org.camunda.bpm.hackdays.prediction.plugin;

import javax.sql.DataSource;

import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.hackdays.prediction.CmmnPredictionService;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

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
   
    predictionService = CmmnPredictionService.buildWithTxFactory(dataSource, new EngineBasedTransactionFactory());
    predictionService.createDbTables();
  }

  @Override
  protected void finished(Description description) {

    predictionService.dropDbTables();
  }
  
  public CmmnPredictionService getPredictionService() {
    return predictionService;
  }
  
  public DataSource getDataSource() {
    return dataSource;
  }
}
