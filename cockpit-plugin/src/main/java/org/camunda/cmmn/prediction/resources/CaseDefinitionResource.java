package org.camunda.cmmn.prediction.resources;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;
import org.camunda.bpm.engine.rest.dto.runtime.CaseExecutionDto;
import org.camunda.bpm.engine.runtime.CaseInstance;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * @author Askar Akhmerov
 */
public class CaseDefinitionResource extends AbstractCockpitPluginResource {


  public CaseDefinitionResource(String engineName) {
    super(engineName);
  }

  @POST
  @Path("{caseDefinition}")
  public CaseExecutionDto startDefinition(@PathParam("caseDefinition") String caseDefinition) {
    return CaseExecutionDto.fromCaseExecution(getProcessEngine().getCaseService().withCaseDefinition(caseDefinition).create());
  }


}
