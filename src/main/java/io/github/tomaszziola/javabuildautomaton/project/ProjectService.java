package io.github.tomaszziola.javabuildautomaton.project;

import io.github.tomaszziola.javabuildautomaton.ApiResponse;
import io.github.tomaszziola.javabuildautomaton.webhook.dto.GitHubWebhookPayload;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public ApiResponse handleProjectLookup(GitHubWebhookPayload payload) {
        String repositoryFullName = payload.repositoryInfo().fullName();

        Optional<Project> projectOptional = projectRepository.findByRepositoryFullName(repositoryFullName);

        if (projectOptional.isPresent()) {
            Project foundProject = projectOptional.get();
            String message = "Project found in the database: " + foundProject.getName();
            System.out.println(">>> " + message);
            return new ApiResponse("success", message);
        } else {
            String message = "Project not found for repository: " + repositoryFullName;
            System.out.println(">>> " + message);
            return new ApiResponse("not_found", message);
        }
    }
}
