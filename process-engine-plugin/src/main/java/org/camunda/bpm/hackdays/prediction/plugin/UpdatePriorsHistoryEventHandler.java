package org.camunda.bpm.hackdays.prediction.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.history.HistoricCaseActivityInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.HistoricCaseActivityInstanceQueryImpl;
import org.camunda.bpm.engine.impl.cfg.TransactionListener;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.history.event.HistoricCaseInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.hackdays.prediction.CmmnPredictionService;
import org.camunda.bpm.hackdays.prediction.PredictionModel;
import org.camunda.bpm.hackdays.prediction.model.ParsedPredictionModel;
import org.camunda.bpm.hackdays.prediction.model.ParsedPredictionModel.ExpressionBasedVariable;
import org.camunda.bpm.model.cmmn.CmmnModelInstance;
import org.camunda.bpm.model.cmmn.instance.PlanItem;
import org.camunda.bpm.model.cmmn.instance.Task;

import com.github.thorbenlindhauer.learning.prior.ConditionalDiscreteDistributionPrior;

public class UpdatePriorsHistoryEventHandler implements HistoryEventHandler {

  protected CmmnPredictionService predictionService;
  
  public UpdatePriorsHistoryEventHandler(CmmnPredictionService predictionService) {
    this.predictionService = predictionService;
  }
  
  public void handleEvent(HistoryEvent historyEvent) {
    if (historyEvent instanceof HistoricCaseInstanceEventEntity) {
      handleHistoricCaseInstanceEvent((HistoricCaseInstanceEventEntity) historyEvent);
    }
  }

  private void handleHistoricCaseInstanceEvent(HistoricCaseInstanceEventEntity historyEvent) {
    
    if (!historyEvent.isClosed()) {
      return;
    }
    
    // 1. find corresponding prediction model
    String caseDefinitionId = historyEvent.getCaseDefinitionId();
    final String caseInstanceId = historyEvent.getCaseInstanceId();

    // convention:
    // * name of prediction model corresponds to id of case definition
    final PredictionModel predictionModel = predictionService.getModel(caseDefinitionId);
    if (predictionModel == null) {
      return;
    }
    
    CommandContext commandContext = Context.getCommandContext();
    commandContext.getTransactionContext()
      .addTransactionListener(TransactionState.COMMITTED, 
          new UpdatePriorsTransactionListener(predictionService, predictionModel, caseInstanceId, caseDefinitionId));
    
  }

  public void handleEvents(List<HistoryEvent> historyEvents) {
    for (HistoryEvent event : historyEvents) {
      handleEvent(event);
    }
  }
  
  public static class UpdatePriorsTransactionListener implements TransactionListener {
    
    protected CmmnPredictionService predictionService;
    protected PredictionModel predictionModel;
    protected String caseInstanceId;
    protected String caseDefinitionId;
    
    public UpdatePriorsTransactionListener(CmmnPredictionService predictionService, PredictionModel predictionModel, String caseInstanceId,
        String caseDefinitionId) {
      this.predictionService = predictionService;
      this.predictionModel = predictionModel;
      this.caseInstanceId = caseInstanceId;
      this.caseDefinitionId = caseDefinitionId;
    }

    public void execute(CommandContext commandContext) {
      // 2. retrieve all the values for the variables and evaluate expressions
      // TODO: we could cache the parsed models
      ParsedPredictionModel parsedModel = predictionService.parseModel(predictionModel);
      Map<String, Integer> groundVariableValues = new HashMap<String, Integer>();
      
      Map<String, ExpressionBasedVariable> variables = parsedModel.getExpressionBasedVariables();

      List<HistoricVariableInstance> caseInstanceVariables = commandContext.getHistoricVariableInstanceManager().findHistoricVariableInstancesByCaseInstanceId(caseInstanceId);
      
      Map<String, Object> historicValues = new HashMap<String, Object>();
      for (HistoricVariableInstance variable : caseInstanceVariables) {
        // TODO: this does not make a context switch, so is not going to work with custom objects
        historicValues.put(variable.getName(), variable.getValue());
      }
      
      for (ExpressionBasedVariable variable : variables.values()) {
        groundVariableValues.put(variable.getName(), variable.determineExpressionValue(historicValues));
      }
      
      // 2.1 determine binary variable values based on historic case activity instances
      List<HistoricCaseActivityInstance> historicActivityInstances = 
          new HistoricCaseActivityInstanceQueryImpl().caseInstanceId(caseInstanceId).list();
      
      Set<String> performedActivityIds = new HashSet<String>();
      for (HistoricCaseActivityInstance historicInstance : historicActivityInstances) {
        if (historicInstance.isCompleted()) {
          performedActivityIds.add(historicInstance.getCaseActivityId());
        }
      }
      
      List<String> caseActivityIds = getPlanItemIds(caseDefinitionId);
      for (String activityId : caseActivityIds) {
        if (performedActivityIds.contains(activityId)) {
          groundVariableValues.put(activityId, 1); // TODO: remove magic number
        }
        else {
          groundVariableValues.put(activityId, 0);
        }
      }
      
      // TODO: 
      // 3. Get all priors
      Map<String, ConditionalDiscreteDistributionPrior> modelPriors = parsedModel.toPriors(predictionModel.getPriors());
      
      // 4. for each prior, collect variables and update accordingly
      for (Map.Entry<String, ConditionalDiscreteDistributionPrior> priorEntry : modelPriors.entrySet()) {
        String describedVariable = priorEntry.getKey();
        ConditionalDiscreteDistributionPrior prior = priorEntry.getValue();
        
        int describedValue = groundVariableValues.get(describedVariable);
        
        String[] conditioningVariables = prior.getConditioningScope().getVariableIds();
        int[] conditioningAssignment = new int[conditioningVariables.length];
        
        for (int i = 0; i < conditioningVariables.length; i++) {
          conditioningAssignment[i] = groundVariableValues.get(conditioningVariables[i]);
        }
        
        prior.submitEvidence(conditioningAssignment, describedValue);
      }
      
      // 5. update and persist model
      parsedModel.updatePriors(modelPriors.values(), predictionModel.getPriors());
      
      predictionService.updateModel(predictionModel);
    }
    
    protected List<String> getPlanItemIds(String caseDefinitionId) {
      DeploymentCache deploymentCache = Context.getProcessEngineConfiguration().getDeploymentCache();
      CmmnModelInstance modelInstance = deploymentCache.findCmmnModelInstanceForCaseDefinition(caseDefinitionId);
      Collection<PlanItem> planItems = modelInstance.getModelElementsByType(PlanItem.class);
      
      List<String> ids = new ArrayList<String>();
      for (PlanItem planItem : planItems) {
        ids.add(planItem.getId());
      }
      
      return ids;
    }
  }

}
