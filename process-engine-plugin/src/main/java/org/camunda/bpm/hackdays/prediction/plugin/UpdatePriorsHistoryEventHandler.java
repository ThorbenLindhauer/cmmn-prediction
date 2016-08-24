package org.camunda.bpm.hackdays.prediction.plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.cfg.TransactionListener;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.history.event.HistoricCaseInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.hackdays.prediction.CmmnPredictionException;
import org.camunda.bpm.hackdays.prediction.CmmnPredictionService;
import org.camunda.bpm.hackdays.prediction.PredictionModel;
import org.camunda.bpm.hackdays.prediction.model.ParsedPredictionModel;
import org.camunda.bpm.hackdays.prediction.model.ParsedPredictionModel.DiscreteVariable;
import org.camunda.bpm.hackdays.prediction.model.ParsedPredictionModel.VariableValue;

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
      .addTransactionListener(TransactionState.COMMITTED, new TransactionListener() {
        
        public void execute(CommandContext commandContext) {
          // 2. retrieve all the values for the variables and evaluate expressions
          // TODO: we could cache the parsed models
          ParsedPredictionModel parsedModel = predictionService.parseModel(predictionModel);
          Map<String, Integer> groundVariableValues = new HashMap<String, Integer>();
          
          Map<String, DiscreteVariable> variables = parsedModel.getVariables();

          List<HistoricVariableInstance> caseInstanceVariables = commandContext.getHistoricVariableInstanceManager().findHistoricVariableInstancesByCaseInstanceId(caseInstanceId);
          
          Map<String, Object> historicValues = new HashMap<String, Object>();
          for (HistoricVariableInstance variable : caseInstanceVariables) {
            // TODO: this does not make a context switch, so is not going to work with custom objects
            historicValues.put(variable.getName(), variable.getValue());
          }
          
          for (DiscreteVariable variable : variables.values()) {
            groundVariableValues.put(variable.getName(), variable.determineValue(historicValues));
          }

          // TODO: 
          // 3. Get all priors
          predictionMod
          
          // 4. for each prior, collect variables and update accordingly
        }
      });
    
  }

  public void handleEvents(List<HistoryEvent> historyEvents) {
    for (HistoryEvent event : historyEvents) {
      handleEvent(event);
    }
    
  }

}
