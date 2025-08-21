package io.github.tomaszziola.javabuildautomaton.buildsystem.entity;

import static jakarta.persistence.EnumType.STRING;

import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus;
import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
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
import org.hibernate.proxy.HibernateProxy;

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

  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    }
    if (other == null) {
      return false;
    }

    final Class<?> otherEffectiveClass =
        other instanceof HibernateProxy
            ? ((HibernateProxy) other).getHibernateLazyInitializer().getPersistentClass()
            : other.getClass();

    final Class<?> thisEffectiveClass =
        this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
            : this.getClass();

    if (!thisEffectiveClass.equals(otherEffectiveClass)) {
      return false;
    }

    final Build build = (Build) other;
    return this.id != null && this.id.equals(build.id);
  }

  @Override
  public int hashCode() {
    return (this instanceof HibernateProxy)
        ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }
}
