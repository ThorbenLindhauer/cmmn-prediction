package org.camunda.cmmn.prediction;

import org.camunda.bpm.cockpit.plugin.spi.impl.AbstractCockpitPlugin;
import org.camunda.cmmn.prediction.resources.ControlPluginRootResource;
import org.camunda.cmmn.prediction.resources.PredictionPluginRootResource;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Askar Akhmerov
 */
public class PredictionPlugin extends AbstractCockpitPlugin {
  public static final String ID = "prediction-plugin";

  public String getId() {
    return ID;
  }


  @Override
  public Set<Class<?>> getResourceClasses() {
    Set<Class<?>> classes = new HashSet<Class<?>>();

    classes.add(PredictionPluginRootResource.class);

    return classes;
  }
}
