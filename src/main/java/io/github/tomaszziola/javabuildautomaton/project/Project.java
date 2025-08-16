package io.github.tomaszziola.javabuildautomaton.project;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project{
    @Id
    @GeneratedValue
    Long id;
    String name;
    String repositoryFullName;
    String localPath;
}
