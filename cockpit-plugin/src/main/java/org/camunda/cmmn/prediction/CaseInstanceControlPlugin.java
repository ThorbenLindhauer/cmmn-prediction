package org.camunda.cmmn.prediction;

import org.camunda.bpm.cockpit.plugin.spi.impl.AbstractCockpitPlugin;
import org.camunda.cmmn.prediction.resources.CaseInstanceControlPluginRootResource;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Askar Akhmerov
 */
public class CaseInstanceControlPlugin extends AbstractCockpitPlugin {
  public static final String ID = "case-instance-plugin";

  public String getId() {
    return ID;
  }


  @Override
  public Set<Class<?>> getResourceClasses() {
    Set<Class<?>> classes = new HashSet<Class<?>>();

    classes.add(CaseInstanceControlPluginRootResource.class);

    return classes;
  }
}
