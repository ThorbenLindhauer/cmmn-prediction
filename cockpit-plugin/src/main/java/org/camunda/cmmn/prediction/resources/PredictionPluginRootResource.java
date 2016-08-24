package org.camunda.cmmn.prediction.resources;

import org.camunda.bpm.cockpit.plugin.resource.AbstractPluginRootResource;
import org.camunda.cmmn.prediction.PredictionPlugin;

import javax.ws.rs.Path;

/**
 * @author Askar Akhmerov
 */
@Path("plugin/" + PredictionPlugin.ID)
public class PredictionPluginRootResource extends AbstractPluginRootResource {

  public PredictionPluginRootResource() {
    super(PredictionPlugin.ID);
  }
}
