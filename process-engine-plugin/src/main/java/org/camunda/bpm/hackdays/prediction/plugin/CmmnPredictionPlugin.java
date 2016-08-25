package org.camunda.bpm.hackdays.prediction.plugin;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.handler.CompositeDbHistoryEventHandler;
import org.camunda.bpm.hackdays.prediction.CmmnPredictionService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class CmmnPredictionPlugin extends AbstractProcessEnginePlugin {

  protected CmmnPredictionService cmmnPredictionService;

  @Override
  public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    cmmnPredictionService = CmmnPredictionService.build(processEngineConfiguration.getDataSource());
    
    processEngineConfiguration.setHistoryEventHandler(
        new CompositeDbHistoryEventHandler(new UpdatePriorsHistoryEventHandler(cmmnPredictionService)));
    
    processEngineConfiguration.getDeployers().add(new PredictionModelDeployer(cmmnPredictionService));
    processEngineConfiguration.getDeploymentCache().getDeployers().add(new PredictionModelDeployer(cmmnPredictionService));
  }

  public void postProcessEngineBuild(ProcessEngine processEngine) {
    DataSource dataSource = processEngine.getProcessEngineConfiguration().getDataSource();
    Connection connection = null;
    try {
      connection = dataSource.getConnection();
      cmmnPredictionService.createDbTables(connection);
    } catch (Exception e) {
      //coz we can
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
}
