package io.github.tomaszziola.javabuildautomaton.buildsystem.entity;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.SEQUENCE;

import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus;
import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;

@Entity
@Getter
@Setter
@ToString(exclude = {"project", "logs"})
public class Build {

  @Id
  @SequenceGenerator(name = "build_sq", sequenceName = "build_sq", allocationSize = 1)
  @GeneratedValue(strategy = SEQUENCE, generator = "build_sq")
  private Long id;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @Enumerated(STRING)
  private BuildStatus status;

  @Column(columnDefinition = "TEXT")
  private String logs;

  @Column(name = "duration_ms")
  private Long durationMs;

  @Column(name = "failure_reason")
  private String failureReason;

  private Instant startTime;
  private Instant endTime;

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null) {
      return false;
    }

    Class<?> otherEffectiveClass =
        other instanceof HibernateProxy
            ? ((HibernateProxy) other).getHibernateLazyInitializer().getPersistentClass()
            : other.getClass();

    Class<?> thisEffectiveClass =
        this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
            : this.getClass();

    if (!thisEffectiveClass.equals(otherEffectiveClass)) {
      return false;
    }

    Build build = (Build) other;
    return this.id != null && this.id.equals(build.id);
  }

  @Override
  public int hashCode() {
    return (this instanceof HibernateProxy)
        ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }
}
