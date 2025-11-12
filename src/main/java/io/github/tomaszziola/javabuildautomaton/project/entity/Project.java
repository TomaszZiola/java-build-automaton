package io.github.tomaszziola.javabuildautomaton.project.entity;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.SEQUENCE;

import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool;
import io.github.tomaszziola.javabuildautomaton.project.ProjectJavaVersion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.proxy.HibernateProxy;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Project {

  @Id
  @SequenceGenerator(name = "project_sq", sequenceName = "project_sq", allocationSize = 1)
  @GeneratedValue(strategy = SEQUENCE, generator = "project_sq")
  private Long id;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "build_tool")
  @Enumerated(STRING)
  private BuildTool buildTool;

  private String username;

  @Column(name = "repository_name")
  private String repositoryName;

  @Column(name = "repository_full_name")
  private String repositoryFullName;

  @Column(name = "repository_url")
  private String repositoryUrl;

  @Column(name = "java_version_major", nullable = false)
  private ProjectJavaVersion javaVersion;

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null) {
      return false;
    }

    Class<?> otherEffectiveClass;
    if (other instanceof HibernateProxy proxyOther) {
      otherEffectiveClass = proxyOther.getHibernateLazyInitializer().getPersistentClass();
    } else {
      otherEffectiveClass = other.getClass();
    }

    Class<?> thisEffectiveClass;
    if (this instanceof HibernateProxy proxyThis) {
      thisEffectiveClass = proxyThis.getHibernateLazyInitializer().getPersistentClass();
    } else {
      thisEffectiveClass = this.getClass();
    }

    if (!thisEffectiveClass.equals(otherEffectiveClass)) {
      return false;
    }

    Project entity = (Project) other;
    return this.id != null && this.id.equals(entity.id);
  }

  @Override
  public int hashCode() {
    if (this instanceof HibernateProxy proxy) {
      return proxy.getHibernateLazyInitializer().getPersistentClass().hashCode();
    } else {
      return getClass().hashCode();
    }
  }
}
