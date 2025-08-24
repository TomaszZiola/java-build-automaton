package io.github.tomaszziola.javabuildautomaton.project.entity;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.SEQUENCE;

import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Project {

  @Id
  @SequenceGenerator(name = "project_sq", sequenceName = "project_sq", allocationSize = 1)
  @GeneratedValue(strategy = SEQUENCE, generator = "project_sq")
  private Long id;
  private String name;
  private String repositoryName;
  private String localPath;

  @Enumerated(STRING)
  private BuildTool buildTool;

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

    final Project entity = (Project) other;
    return this.id != null && this.id.equals(entity.id);
  }

  @Override
  public int hashCode() {
    return (this instanceof HibernateProxy)
        ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }
}
