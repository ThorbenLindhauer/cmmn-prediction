package org.camunda.bpm.hackdays.prediction.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.hackdays.prediction.CmmnPredictionService;
import org.camunda.bpm.hackdays.prediction.PredictionModel;
import org.camunda.bpm.hackdays.prediction.model.ParsedPredictionModel;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import com.github.thorbenlindhauer.factor.DiscreteFactor;
import com.github.thorbenlindhauer.inference.VariableEliminationInferencer;
import com.github.thorbenlindhauer.inference.variableelimination.MinFillEliminationStrategy;
import com.github.thorbenlindhauer.network.GraphicalModel;
import com.github.thorbenlindhauer.variable.Scope;

public class UpdatePriorsHistoryEventHandlerTest {

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
  
  @Test
  @Deployment(resources = "twoTasksCase.cmmn")
  public void shouldUpdatePriorsBasedOnHistory() {
    // given
    CaseDefinition caseDefinition = repositoryService.createCaseDefinitionQuery().singleResult();
    
    PredictionModel model = new PredictionModel();
    model.setId("1");
    model.setName(caseDefinition.getId());
    model.setResource(IoUtil.readInputStream(
        UpdatePriorsHistoryEventHandlerTest.class.getClassLoader().getResourceAsStream("model.json"),
        "model"));
    ParsedPredictionModel parsedPredictionModel = predictionService.parseModel(model);
    
    model.setPriors(parsedPredictionModel.generateRawPriors());
    
    predictionService.insertModel(model);
    
    CaseInstance caseInstance = caseService.withCaseDefinition(caseDefinition.getId()).create();
    caseService.withCaseExecution(caseInstance.getId()).setVariable("intVal", 75).execute();
    
    // when
    caseService.withCaseExecution(caseInstance.getId()).complete();
    caseService.withCaseExecution(caseInstance.getId()).close();
    
    // then
    PredictionModel updatedModel = predictionService.getModel(caseDefinition.getId());
    
    ParsedPredictionModel updatedParsedModel = predictionService.parseModel(updatedModel);
    GraphicalModel<DiscreteFactor> graphicalModel = updatedParsedModel.toGraphicalModel(updatedParsedModel.toPriors(updatedModel.getPriors()).values());
    
    VariableEliminationInferencer inferencer = new VariableEliminationInferencer(graphicalModel, new MinFillEliminationStrategy());
    
    Scope barScope = graphicalModel.getScope().subScope("bar");
    double value0Probability = inferencer.jointProbability(barScope, new int[]{0});
    double value1Probability = inferencer.jointProbability(barScope, new int[]{1});
    
    assertThat(value1Probability).isGreaterThan(value0Probability);
    
  }
  
  
}
