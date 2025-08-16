package io.github.tomaszziola.javabuildautomaton;

import io.github.tomaszziola.javabuildautomaton.project.Project;
import io.github.tomaszziola.javabuildautomaton.project.ProjectRepository;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner commandLineRunner(ProjectRepository repository) {
        return args -> {
            Project testProject = new Project();
            testProject.setName("test-project-from-db");
            testProject.setRepositoryFullName("TomaszZiola/test");
            testProject.setLocalPath("/Users/Tomasz/Documents/test");

            repository.save(testProject);

            System.out.println(">>> Dodano testowy projekt do bazy danych");
        };
    }
}
