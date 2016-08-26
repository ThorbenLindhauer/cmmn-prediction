package org.camunda.cmmn.prediction.resources;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;
import org.camunda.bpm.engine.rest.dto.runtime.CaseExecutionDto;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * @author Askar Akhmerov
 */
public class CaseInstanceResource extends AbstractCockpitPluginResource {
  public CaseInstanceResource(String engineName) {
    super(engineName);
  }

  @POST
  @Path("{caseInstance}/close")
  public Response closeInstance(@PathParam("caseInstance") String caseInstance) {
    getProcessEngine().getCaseService().withCaseExecution(caseInstance).close();
    return Response.ok().build();
  }

  @POST
  @Path("{caseInstance}/activate")
  public Response activate(@PathParam("caseInstance") String caseInstance) {
    getProcessEngine().getCaseService().withCaseExecution(caseInstance).manualStart();
    return Response.ok().build();
  }

}
