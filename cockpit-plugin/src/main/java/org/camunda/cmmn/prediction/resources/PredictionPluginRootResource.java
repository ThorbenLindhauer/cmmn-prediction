package org.camunda.cmmn.prediction.resources;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginRootResource;
import org.camunda.bpm.cockpit.plugin.resource.AbstractPluginRootResource;
import org.camunda.cmmn.prediction.PredictionPlugin;
import org.camunda.cmmn.prediction.dto.CasePredictionTO;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Askar Akhmerov
 */
@Path("plugin/" + PredictionPlugin.ID)
public class PredictionPluginRootResource extends AbstractCockpitPluginRootResource {

  public PredictionPluginRootResource() {
    super(PredictionPlugin.ID);
  }

  @Path("{engine}/predictions")
  public CaseDefinitionResource getProcessInstanceResource(@PathParam("engine") String engine) {
    return subResource(new CaseDefinitionResource(engine), engine);
  }

}
