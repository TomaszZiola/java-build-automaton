package io.github.tomaszziola.javabuildautomaton.project;

import static io.github.tomaszziola.javabuildautomaton.project.ProjectJavaVersion.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProjectJavaVersionprojectJavaVersionConverterImplTest extends BaseUnit {

  @Test
  @DisplayName("Given null attribute, when converting to DB column, then return null")
  void convertToDatabaseColumnReturnsNullForNullAttribute() {
    assertThat(projectJavaVersionConverterImpl.convertToDatabaseColumn(null)).isNull();
  }

  @Test
  @DisplayName(
      "Given enum value, when converting to DB column, then return corresponding version number")
  void convertToDatabaseColumnReturnsVersionNumbers() {
    assertThat(projectJavaVersionConverterImpl.convertToDatabaseColumn(JAVA_17)).isEqualTo(17);
    assertThat(projectJavaVersionConverterImpl.convertToDatabaseColumn(JAVA_21)).isEqualTo(21);
    assertThat(projectJavaVersionConverterImpl.convertToDatabaseColumn(JAVA_25)).isEqualTo(25);
  }

  @Test
  @DisplayName("Given null DB value, when converting to entity attribute, then return null")
  void convertToEntityAttributeReturnsNullForNullDbValue() {
    assertThat(projectJavaVersionConverterImpl.convertToEntityAttribute(null)).isNull();
  }

  @Test
  @DisplayName(
      "Given supported DB values, when converting to entity attribute, then return matching enum")
  void convertToEntityAttributeReturnsMatchingEnum() {
    assertThat(projectJavaVersionConverterImpl.convertToEntityAttribute(17)).isEqualTo(JAVA_17);
    assertThat(projectJavaVersionConverterImpl.convertToEntityAttribute(21)).isEqualTo(JAVA_21);
    assertThat(projectJavaVersionConverterImpl.convertToEntityAttribute(25)).isEqualTo(JAVA_25);
  }

  @Test
  @DisplayName(
      "Given unsupported DB value, when converting to entity attribute, then throw IllegalArgumentException")
  void convertToEntityAttributeThrowsOnUnsupportedValue() {
    assertThatThrownBy(() -> projectJavaVersionConverterImpl.convertToEntityAttribute(99))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Not supported Java version: 99");
  }
}
