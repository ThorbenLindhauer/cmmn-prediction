package org.camunda.cmmn.prediction;


import org.camunda.bpm.cockpit.plugin.spi.impl.AbstractCockpitPlugin;

/**
 * @author Askar Akhmerov
 */
public class PredictionPlugin extends AbstractCockpitPlugin {

  public static final String ID = "prediction-plugin";

  public String getId() {
    return ID;
  }
}
