package org.camunda.cmmn.prediction.resources;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginRootResource;
import org.camunda.cmmn.prediction.ControlPlugin;
import org.camunda.cmmn.prediction.PredictionPlugin;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * @author Askar Akhmerov
 */
@Path("plugin/" + PredictionPlugin.ID)
public class PredictionPluginRootResource extends AbstractCockpitPluginRootResource {

  public PredictionPluginRootResource() {
    super(PredictionPlugin.ID);
  }

  @Path("{engine}/predictions")
  public PredictionResource getProcessInstanceResource(@PathParam("engine") String engine) {
    return subResource(new PredictionResource(engine), engine);
  }
}
