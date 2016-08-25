package org.camunda.bpm.hackdays.prediction.plugin;

import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.handler.CompositeDbHistoryEventHandler;
import org.camunda.bpm.hackdays.prediction.CmmnPredictionService;

public class CmmnPredictionPlugin extends AbstractProcessEnginePlugin {

  @Override
  public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    CmmnPredictionService cmmnPredictionService = CmmnPredictionService.build(processEngineConfiguration.getDataSource());
    
    processEngineConfiguration.setHistoryEventHandler(
        new CompositeDbHistoryEventHandler(new UpdatePriorsHistoryEventHandler(cmmnPredictionService)));
    
    processEngineConfiguration.getDeployers().add(new PredictionModelDeployer(cmmnPredictionService));
  }
}
