package io.github.tomaszziola.javabuildautomaton.workspace;

import jakarta.validation.constraints.NotNull;
import java.nio.file.Path;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "workspace")
public class WorkspaceProperties {
  @NotNull private Path baseDir;
}
