package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GitCommandRunnerTest extends BaseUnit {
  @Test
  void givenWorkingDirectory_whenPull_thenDelegatesToProcessExecutorAndPropagatesResult() {
    // when
    final var result = gitCommandRunnerImpl.pull(tempDir);

    // then
    verify(processExecutor).execute(tempDir, "git", "pull");
    assertThat(result).isSameAs(pullExecutionResult);
  }
}
