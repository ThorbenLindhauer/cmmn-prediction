package org.camunda.cmmn.prediction.resources;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.hackdays.prediction.CmmnPredictionService;
import org.camunda.bpm.model.cmmn.CmmnModelInstance;
import org.camunda.cmmn.prediction.dto.CasePredictionTO;

import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    Map<String, Object> variables = getProcessEngine().getCaseService().getVariables(caseInstanceId);
    String caseDefinitionId = getProcessEngine().getCaseService().createCaseExecutionQuery()
        .caseExecutionId(caseInstanceId).singleResult().getCaseDefinitionId();
    Map<String, Double> estimations = this.getPredictionService().estimate(caseDefinitionId, "bar", new HashMap<String, Integer>(), variables);
    for (Map.Entry<String,Double> estimation : estimations.entrySet()) {
      CasePredictionTO toAdd = new CasePredictionTO();
      toAdd.setActivityId(estimation.getKey());
      toAdd.setProbability(String.valueOf(estimation.getValue()));
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
