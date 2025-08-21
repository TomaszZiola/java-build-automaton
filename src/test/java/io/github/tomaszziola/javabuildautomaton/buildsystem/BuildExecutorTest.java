package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool.GRADLE;
import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool.MAVEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BuildExecutorTest extends BaseUnit {

  @Test
  void givenMaven_whenBuild_thenExecutesMvnCleanInstallAndPropagatesResult() {
    // when
    final var result = buildExecutorImpl.build(MAVEN, tempDir);

    // then
    verify(processExecutor).execute(tempDir, "mvn", "clean", "install");
    assertThat(result).isSameAs(pullExecutionResult);
  }

  @Test
  void givenGradle_whenBuild_thenExecutesGradleCleanBuildAndPropagatesResult() {
    // when
    final var result = buildExecutorImpl.build(GRADLE, tempDir);

    // then
    verify(processExecutor).execute(tempDir, "gradle", "clean", "build");
    assertThat(result).isSameAs(pullExecutionResult);
  }
}
