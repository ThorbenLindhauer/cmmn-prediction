package org.camunda.bpm.hackdays.prediction.example;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.camunda.bpm.application.PostDeploy;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.ServletProcessApplication;
import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.task.Task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ProcessApplication
public class ExampleProcessApplication extends ServletProcessApplication {

  
  @PostDeploy
  public void simulateCaseInstances(ProcessEngine engine) {
    CaseService caseService = engine.getCaseService();
    if (caseService.createCaseInstanceQuery().count() > 0) {
      // start instances only once
      return;
    }
    
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode cases;
    try {
      cases = objectMapper.readTree(ExampleProcessApplication.class.getClassLoader().getResourceAsStream("data/case.cmmn.examples.json"));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
    CaseDefinition caseDefinition = engine.getRepositoryService()
        .createCaseDefinitionQuery().caseDefinitionKey("CarEvaluation").latestVersion().singleResult();
    
    Iterator<JsonNode> caseSpecs = cases.elements();
    while (caseSpecs.hasNext()) {
      simulateCase(caseService, engine.getTaskService(), caseDefinition, caseSpecs.next());
    }
  }
  
  protected void simulateCase(CaseService caseService, TaskService taskService, CaseDefinition caseDefinition, JsonNode caseInput) {
    JsonNode variablesNode = caseInput.get("vars");
    int doors = variablesNode.get("doors").asInt();
    int price = variablesNode.get("price").asInt();
    String bootSize = variablesNode.get("boot_size").asText();
    
    Set<String> performedItems = new HashSet<String>();
    Iterator<JsonNode> planItemsIt = caseInput.get("activities").elements();
    while(planItemsIt.hasNext()) {
      performedItems.add(planItemsIt.next().asText());
    }

    CaseInstance caseInstance = caseService.withCaseDefinition(caseDefinition.getId())
      .setVariable("doors", doors)
      .setVariable("price", price)
      .setVariable("boot_size", bootSize)
      .create();
    
    for (String planItem : performedItems) {
      CaseExecution caseExecution = caseService.createCaseExecutionQuery().activityId(planItem).singleResult();
      caseService.withCaseExecution(caseExecution.getId()).manualStart();      
      caseService.withCaseExecution(caseExecution.getId()).complete();
    }
    
    CaseInstance updatedCaseInstance = caseService.createCaseInstanceQuery().caseInstanceId(caseInstance.getId()).singleResult();
    
    if (!updatedCaseInstance.isCompleted()) {
      caseService.withCaseExecution(updatedCaseInstance.getId()).complete();
    }
    
    caseService.withCaseExecution(updatedCaseInstance.getId()).close();
    
    
  }
}
