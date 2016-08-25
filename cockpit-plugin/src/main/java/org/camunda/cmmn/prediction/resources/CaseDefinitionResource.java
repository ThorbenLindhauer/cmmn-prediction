package org.camunda.cmmn.prediction.resources;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;
import org.camunda.bpm.hackdays.prediction.CmmnPredictionService;
import org.camunda.cmmn.prediction.dto.CasePredictionTO;

import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Askar Akhmerov
 */
public class CaseDefinitionResource extends AbstractCockpitPluginResource {
  private CmmnPredictionService predictionService;

  public CaseDefinitionResource(String engineName) {
    super(engineName);
  }

  @GET
  @Path("{caseDefinition}")
  public List<CasePredictionTO> getPredictions(@PathParam("caseDefinition") String caseDefinition) {
    System.out.println("CASE DEF [" + caseDefinition + "]");
    return this.queryPredictions(caseDefinition);
  }

  protected List<CasePredictionTO> queryPredictions(String caseDefinition) {
    List<CasePredictionTO> result = new ArrayList<CasePredictionTO>();
    Map<String, Double> estimations = this.getPredictionService().estimate(caseDefinition, "bar", new HashMap<String, Integer>(), new HashMap<String, Object>());
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
