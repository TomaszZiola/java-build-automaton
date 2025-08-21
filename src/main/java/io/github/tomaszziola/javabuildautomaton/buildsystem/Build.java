package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static jakarta.persistence.EnumType.STRING;

import io.github.tomaszziola.javabuildautomaton.project.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Build {

  @Id @GeneratedValue private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @Enumerated(STRING)
  private BuildStatus status;

  private Instant startTime;

  private Instant endTime;

  @Lob
  @Column(columnDefinition = "TEXT")
  private String logs;
}
