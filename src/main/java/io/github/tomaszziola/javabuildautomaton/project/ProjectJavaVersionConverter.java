package io.github.tomaszziola.javabuildautomaton.project;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ProjectJavaVersionConverter
    implements AttributeConverter<ProjectJavaVersion, Integer> {

  @Override
  public Integer convertToDatabaseColumn(ProjectJavaVersion attribute) {
    if (attribute == null) {
      return null;
    }
    return attribute.getVersionNumber();
  }

  @Override
  public ProjectJavaVersion convertToEntityAttribute(Integer dbData) {
    if (dbData == null) {
      return null;
    }
    for (ProjectJavaVersion version : ProjectJavaVersion.values()) {
      if (version.getVersionNumber() == dbData) {
        return version;
      }
    }
    throw new IllegalArgumentException("Not supported Java version: " + dbData);
  }
}
