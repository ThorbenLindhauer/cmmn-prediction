package org.camunda.bpm.hackdays.prediction.plugin;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.history.handler.CompositeDbHistoryEventHandler;
import org.camunda.bpm.hackdays.prediction.CmmnPredictionService;
import org.camunda.commons.utils.EnsureUtil;

public class CmmnPredictionPlugin extends AbstractProcessEnginePlugin {

  protected boolean createTables = true;
  
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
    if (!createTables) {
      return;
    }
    
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
  
  public void setCreateTables(boolean createTables) {
    this.createTables = createTables;
  }
  
  public CmmnPredictionService getCmmnPredictionService() {
    return cmmnPredictionService;
  }
  
  /**
   * For a process engine that has this plugin registered, fetches the managed {@link CmmnPredictionService}.
   */
  public static CmmnPredictionService getPredictionService(ProcessEngine processEngine) {
    EnsureUtil.ensureNotNull("processEngine", processEngine);
    
    ProcessEngineConfigurationImpl engineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    
    for (ProcessEnginePlugin enginePlugin : engineConfiguration.getProcessEnginePlugins()) {
      if (enginePlugin instanceof CmmnPredictionPlugin) {
        return ((CmmnPredictionPlugin) enginePlugin).getCmmnPredictionService();
      }
    }
    
    throw new RuntimeException("Could not fetch CmmnPredictionService. CmmnPredictionPlugin not registerd with engine " + processEngine.getName());
  }
}
