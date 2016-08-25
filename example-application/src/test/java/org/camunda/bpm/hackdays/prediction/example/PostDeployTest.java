package org.camunda.bpm.hackdays.prediction.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.camunda.bpm.engine.history.HistoricCaseInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

public class PostDeployTest {

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule();
  
  protected String deploymentId;
  
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
}
