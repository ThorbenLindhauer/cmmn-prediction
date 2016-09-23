package org.camunda.bpm.hackdays.prediction.plugin;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSession;
import org.camunda.bpm.engine.impl.history.handler.CompositeDbHistoryEventHandler;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.hackdays.prediction.CmmnPredictionService;
import org.camunda.commons.utils.EnsureUtil;

public class CmmnPredictionPlugin extends AbstractProcessEnginePlugin {

  protected static final String DATABASE_TYPE_H2 = "h2";
  protected static final String TABLE_PREDICTION_MODEL = "PREDICTION_MODEL";

  protected boolean createTables = true;
  
  protected CmmnPredictionService cmmnPredictionService;
  

  @Override
  public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    
    IntegratedDbSqlSessionFactory sqlSessionFactory = new IntegratedDbSqlSessionFactory(processEngineConfiguration.getDbSqlSessionFactory());
    processEngineConfiguration.getSessionFactories().put(sqlSessionFactory.getSessionType(), sqlSessionFactory);
    
    cmmnPredictionService = CmmnPredictionService.buildWithTxFactory(
        processEngineConfiguration.getDataSource(), 
        new EngineBasedTransactionFactory()
        );
    
    processEngineConfiguration.setHistoryEventHandler(
        new CompositeDbHistoryEventHandler(new UpdatePriorsHistoryEventHandler(cmmnPredictionService)));
    
    processEngineConfiguration.getDeployers().add(new PredictionModelDeployer(cmmnPredictionService));
    processEngineConfiguration.getDeploymentCache().getDeployers().add(new PredictionModelDeployer(cmmnPredictionService));
  }

  public void postProcessEngineBuild(ProcessEngine processEngine) {
    if (!createTables) {
      return;
    }
    
    ProcessEngineConfigurationImpl engineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    String databaseType = engineConfiguration.getDatabaseType();
    
    if (!DATABASE_TYPE_H2.equals(databaseType)) {
      throw new RuntimeException("Cannot create prediction tables in database of type " + databaseType + ". Only h2 supported");
    }
    
    engineConfiguration.getCommandExecutorSchemaOperations().execute(new Command<Void>(){

      public Void execute(CommandContext commandContext) {
        
        DbSqlSession dbSqlSession = commandContext.getDbSqlSession();
        
        if (!dbSqlSession.isTablePresent(TABLE_PREDICTION_MODEL)) {
          cmmnPredictionService.createDbTables();
        }
        
        return null;
      }
    });
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
