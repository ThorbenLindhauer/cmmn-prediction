package org.camunda.bpm.hackdays.prediction.plugin;

import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.hackdays.prediction.CmmnPredictionService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.RuleChain;

public class EndToEndTest {

  public ProcessEngineRule engineRule = new ProcessEngineRule("camunda.cfg.xml");
  
  public PredictionTestRule predictionRule = new PredictionTestRule(engineRule);
  
  @Rule
  public RuleChain rule = RuleChain.outerRule(engineRule).around(predictionRule);
  
  protected CaseService caseService;
  protected RepositoryService repositoryService;
  protected CmmnPredictionService predictionService;
  
  @Before
  public void setUp() {
    caseService = engineRule.getCaseService();
    repositoryService = engineRule.getRepositoryService();
    predictionService = predictionRule.getPredictionService();
  }
  
  // TODO: deploy case and model => assert that model priors are updated with every finished case instance
}
