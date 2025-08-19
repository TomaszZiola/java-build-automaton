package io.github.tomaszziola.javabuildautomaton.project;

import static jakarta.persistence.EnumType.STRING;

import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {
  @Id @GeneratedValue private Long id;
  private String name;
  private String repositoryName;
  private String localPath;

  @Enumerated(STRING)
  private BuildTool buildTool;
}
