package io.github.tomaszziola.javabuildautomaton.project;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProjectJavaVersion {
  JAVA_17(17),
  JAVA_21(21),
  JAVA_25(25);

  private final int versionNumber;
}
