package org.camunda.bpm.hackdays.prediction.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.history.HistoricCaseInstance;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.hackdays.prediction.CmmnPredictionService;
import org.camunda.bpm.hackdays.prediction.PredictionModel;
import org.camunda.bpm.hackdays.prediction.model.PredictionModelParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class PostDeployTest {

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule();
  
  
  protected CmmnPredictionService predictionService;
  protected String deploymentId;
  
  @Before
  public void initPredictionService() {
    predictionService = CmmnPredictionService.build(engineRule.getProcessEngineConfiguration().getDataSource());
  }
  
  @After
  public void tearDown() {
    if (deploymentId != null) {
      engineRule.getRepositoryService().deleteDeployment(deploymentId, true);
    }
  }
  
  @Test
  public void shouldCreateInstanceOnPostDeploy() {
    deploymentId = engineRule.getRepositoryService()
        .createDeployment()
        .addClasspathResource("models/case.cmmn")
        .deploy()
        .getId();
    
    ExampleProcessApplication processApplication = new ExampleProcessApplication();
    
    // when
    processApplication.simulateCaseInstances(engineRule.getProcessEngine());
    
    // then
    List<HistoricCaseInstance> historicCaseInstances = engineRule.getHistoryService().createHistoricCaseInstanceQuery().list();
    assertThat(historicCaseInstances.size()).isEqualTo(9);
    for (HistoricCaseInstance caseInstance : historicCaseInstances) {
      assertThat(caseInstance.isClosed()).isTrue();
    }
  }
  
  @Test
  public void shouldEstimateProbabilityWithIncompleteData() {
    // given
    deploymentId = engineRule.getRepositoryService()
        .createDeployment()
        .addClasspathResource("models/case.cmmn")
        .addClasspathResource("models/case.cmmn.json")
        .deploy()
        .getId();
    
    CaseDefinition caseDefinition = engineRule.getRepositoryService().createCaseDefinitionQuery().singleResult();
    
    ExampleProcessApplication processApplication = new ExampleProcessApplication();
    processApplication.simulateCaseInstances(engineRule.getProcessEngine());
    
    // when
    Map<String, Object> evidence = new HashMap<String, Object>();
    evidence.put("boot_size", "large");
//    evidence.put("price", 30000);
    
    PredictionModel model = predictionService.getModel(caseDefinition.getId());
    Map<String, Double> estimate = predictionService.estimate(model, "PlanItem_Estimate_Value", Collections.<String, Integer>emptyMap(), evidence);
    
    // then
    Double doProbability = estimate.get(PredictionModelParser.VARIABLE_TYPE_BINARY_TRUE);
    Double doNotProbability = estimate.get(PredictionModelParser.VARIABLE_TYPE_BINARY_FALSE);
    
    System.out.println(doProbability);
    System.out.println(doNotProbability);
    assertThat(doProbability).isGreaterThan(doNotProbability);
  }
}
