package org.camunda.cmmn.prediction;


import org.camunda.bpm.cockpit.plugin.spi.impl.AbstractCockpitPlugin;
import org.camunda.cmmn.prediction.resources.ControlPluginRootResource;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Askar Akhmerov
 */
public class ControlPlugin extends AbstractCockpitPlugin {

  public static final String ID = "control-plugin";

  public String getId() {
    return ID;
  }


  @Override
  public Set<Class<?>> getResourceClasses() {
    Set<Class<?>> classes = new HashSet<Class<?>>();

    classes.add(ControlPluginRootResource.class);

    return classes;
  }

}
