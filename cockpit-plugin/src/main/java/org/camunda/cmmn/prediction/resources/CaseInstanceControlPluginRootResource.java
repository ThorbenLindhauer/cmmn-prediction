package org.camunda.cmmn.prediction.resources;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginRootResource;
import org.camunda.cmmn.prediction.CaseInstanceControlPlugin;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * @author Askar Akhmerov
 */
@Path("plugin/" + CaseInstanceControlPlugin.ID)
public class CaseInstanceControlPluginRootResource extends AbstractCockpitPluginRootResource {

  public CaseInstanceControlPluginRootResource() {
    super(CaseInstanceControlPlugin.ID);
  }

  @Path("{engine}/caseInstanceControl")
  public CaseInstanceResource getProcessInstanceResource(@PathParam("engine") String engine) {
    return subResource(new CaseInstanceResource(engine), engine);
  }
}
