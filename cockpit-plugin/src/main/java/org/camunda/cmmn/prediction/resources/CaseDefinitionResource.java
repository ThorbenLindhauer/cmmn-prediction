package org.camunda.cmmn.prediction.resources;

import org.camunda.bpm.cockpit.plugin.resource.AbstractPluginResource;
import org.camunda.cmmn.prediction.dto.CasePredictionTO;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Askar Akhmerov
 */
public class CaseDefinitionResource extends AbstractPluginResource {
  public CaseDefinitionResource(String engineName) {
    super(engineName);
  }

  @GET
  @Path("{caseDefinition}")
  public List<CasePredictionTO> getProcessInstanceCounts(@PathParam("caseDefinition") String caseDefinition) {
    System.out.println("CASE DEF [" + caseDefinition + "]");
    return queryPredictions(caseDefinition);
  }

  private List<CasePredictionTO> queryPredictions(String caseDefinition) {
    List<CasePredictionTO> result = new ArrayList<CasePredictionTO>();
    CasePredictionTO to = new CasePredictionTO();
    to.setActivityId("test");
    to.setProbability("30");
    result.add(to);
    return result;
  }
}
