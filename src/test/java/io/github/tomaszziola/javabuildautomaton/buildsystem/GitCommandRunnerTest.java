package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GitCommandRunnerTest extends BaseUnit {
  @Test
  @DisplayName(
      "Given working directory, when pulling, then delegate to process executor and propagate result")
  void delegatesToProcessExecutorWhenPulling() {
    // when
    var result = gitCommandRunnerImpl.pull(tempDir);

    // then
    verify(processExecutor).execute(tempDir, "git", "pull");
    assertThat(result).isSameAs(pullExecutionResult);
  }

  @Test
  @DisplayName(
      "Given repository and target dir, when cloning, then delegate to process executor and propagate result")
  void delegatesToProcessExecutorWhenCloning() {
    // when
    var result = gitCommandRunnerImpl.clone(project.getRepositoryUrl(), tempDir);

    // then
    verify(processExecutor).execute(tempDir, "git", "clone", project.getRepositoryUrl(), ".");
    assertThat(result).isSameAs(cloneExecutionResult);
  }
}
