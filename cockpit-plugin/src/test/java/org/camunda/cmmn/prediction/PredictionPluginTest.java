package org.camunda.cmmn.prediction;

import org.camunda.bpm.cockpit.Cockpit;
import org.camunda.bpm.cockpit.plugin.test.AbstractCockpitPluginTest;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Askar Akhmerov
 */
public class PredictionPluginTest extends AbstractCockpitPluginTest {

  @Test
  public void testPluginDiscovery() {
    PredictionPlugin samplePlugin = (PredictionPlugin) Cockpit.getRuntimeDelegate().getPluginRegistry().getPlugin("prediction-plugin");

    Assert.assertNotNull(samplePlugin);
  }

}