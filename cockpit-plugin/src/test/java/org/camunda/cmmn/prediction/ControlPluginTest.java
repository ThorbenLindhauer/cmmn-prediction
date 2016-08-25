package org.camunda.cmmn.prediction;

import org.camunda.bpm.cockpit.Cockpit;
import org.camunda.bpm.cockpit.plugin.test.AbstractCockpitPluginTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Askar Akhmerov
 */
public class ControlPluginTest extends AbstractCockpitPluginTest {

  @Test
  public void testPluginDiscovery() {
    ControlPlugin samplePlugin = (ControlPlugin) Cockpit.getRuntimeDelegate().getPluginRegistry().getPlugin("control-plugin");

    Assert.assertNotNull(samplePlugin);
  }

}