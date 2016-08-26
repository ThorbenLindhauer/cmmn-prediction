package org.camunda.bpm.hackdays.prediction.plugin;

import java.util.List;

import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.deploy.Deployer;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;
import org.camunda.bpm.hackdays.prediction.CmmnPredictionService;
import org.camunda.bpm.hackdays.prediction.PredictionModel;
import org.camunda.bpm.hackdays.prediction.model.ParsedPredictionModel;

public class PredictionModelDeployer implements Deployer {

  protected CmmnPredictionService predictionService;
  
  public PredictionModelDeployer(CmmnPredictionService predictionService) {
    this.predictionService = predictionService;
  }

  public void deploy(DeploymentEntity deployment) {
    if (!deployment.isNew()) {
      // only generate dmn xml once when deploying for the first time
      return;
    }

    List<CaseDefinitionEntity> deployedCaseDefinitions = deployment.getDeployedArtifacts(CaseDefinitionEntity.class);
    
    if (deployedCaseDefinitions == null) {
      return;
    }
    
    for (CaseDefinitionEntity caseDefinition : deployedCaseDefinitions) {
      String predictionModelResourceName = caseDefinition.getResourceName() + ".json";
      ResourceEntity predictionModel = deployment.getResource(predictionModelResourceName);
      
      if (predictionModel != null) {
        persistPredictionModel(predictionModel, caseDefinition);
      }
    }
    
  }

  protected void persistPredictionModel(ResourceEntity predictionModelResource, CaseDefinitionEntity caseDefinition) {
    PredictionModel model = new PredictionModel();
    model.setId(Context.getProcessEngineConfiguration().getIdGenerator().getNextId());
    model.setName(caseDefinition.getId());
    model.setResource(predictionModelResource.getBytes());
    
    ParsedPredictionModel parsedModel = predictionService.parseModel(model);
    model.setPriors(parsedModel.generateRawPriors());
    
    predictionService.insertModel(model);
  }

}
