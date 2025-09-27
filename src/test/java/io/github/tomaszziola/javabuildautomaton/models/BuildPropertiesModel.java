package io.github.tomaszziola.javabuildautomaton.models;

import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildProperties;

public class BuildPropertiesModel {

  public static BuildProperties basic() {
    final BuildProperties buildProperties = new BuildProperties();
    buildProperties.setMaxParallel(2);
    buildProperties.getQueue().setCapacity(3);
    return buildProperties;
  }
}
