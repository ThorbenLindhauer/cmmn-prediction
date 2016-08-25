package org.camunda.cmmn.prediction.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;
import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.hackdays.prediction.CmmnPredictionException;
import org.camunda.bpm.hackdays.prediction.CmmnPredictionService;
import org.camunda.bpm.hackdays.prediction.PredictionModel;
import org.camunda.bpm.hackdays.prediction.model.ParsedPredictionModel;
import org.camunda.bpm.hackdays.prediction.model.PredictionModelParser;
import org.camunda.cmmn.prediction.dto.CasePredictionTO;

/**
 * @author Askar Akhmerov
 */
public class PredictionResource extends AbstractCockpitPluginResource {
  private static CmmnPredictionService predictionService;

  public PredictionResource(String engineName) {
    super(engineName);
  }

  @GET
  @Path("{caseInstanceId}")
  public List<CasePredictionTO> startDefinition(@PathParam("caseInstanceId") String caseInstanceId) {
    System.out.println("CASE ID [" + caseInstanceId + "]");
    return this.queryPredictions(caseInstanceId);
  }

  protected List<CasePredictionTO> queryPredictions(String caseInstanceId) {
    List<CasePredictionTO> result = new ArrayList<CasePredictionTO>();
    CaseService caseService = getProcessEngine().getCaseService();
    
    Map<String, Object> variables = caseService.getVariables(caseInstanceId);
    String caseDefinitionId = caseService.createCaseExecutionQuery()
        .caseExecutionId(caseInstanceId).singleResult().getCaseDefinitionId();
    
    List<CaseExecution> activeExecutions = caseService.createCaseExecutionQuery().active().list();
    List<CaseExecution> enabledExecutions = caseService.createCaseExecutionQuery().enabled().list();
    
    Set<String> planItemIds = new HashSet<String>();
    for (CaseExecution execution : activeExecutions) {
      planItemIds.add(execution.getActivityId());
    }
    for (CaseExecution execution : enabledExecutions) {
      planItemIds.add(execution.getActivityId());
    }
    
    PredictionModel model = this.getPredictionService().getModel(caseDefinitionId);
    
    if (model == null) {
      throw new RuntimeException("Model with name " + caseDefinitionId + " does not exist");
    }
    
    ParsedPredictionModel parsedModel = this.getPredictionService().parseModel(model);
    
    for (String planItemId : planItemIds) {
      if (!parsedModel.getVariables().containsKey(planItemId)) {
        // ignore activities not defined in model
        continue;
      }
      
      Map<String, Double> estimations = this.getPredictionService().estimate(model, planItemId, new HashMap<String, Integer>(), variables);

      CasePredictionTO toAdd = new CasePredictionTO();
      toAdd.setActivityId(planItemId);
      toAdd.setProbability(estimations.get(PredictionModelParser.VARIABLE_TYPE_BINARY_TRUE));
      result.add(toAdd);
    }

    return result;
  }

  protected CmmnPredictionService getPredictionService() {
    if (predictionService == null) {
      DataSource dataSource = this.getProcessEngine().getProcessEngineConfiguration().getDataSource();
      predictionService = CmmnPredictionService.build(dataSource);
    }
    return predictionService;
  }
}
 