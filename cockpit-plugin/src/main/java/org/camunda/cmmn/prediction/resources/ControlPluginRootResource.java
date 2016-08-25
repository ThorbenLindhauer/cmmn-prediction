package org.camunda.cmmn.prediction.resources;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginRootResource;
import org.camunda.cmmn.prediction.ControlPlugin;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * @author Askar Akhmerov
 */
@Path("plugin/" + ControlPlugin.ID)
public class ControlPluginRootResource extends AbstractCockpitPluginRootResource {

  public ControlPluginRootResource() {
    super(ControlPlugin.ID);
  }

  @Path("{engine}/start")
  public CaseDefinitionResource getProcessInstanceResource(@PathParam("engine") String engine) {
    return subResource(new CaseDefinitionResource(engine), engine);
  }

}
