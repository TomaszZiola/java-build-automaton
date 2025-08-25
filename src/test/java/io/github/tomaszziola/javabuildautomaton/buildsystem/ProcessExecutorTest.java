package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import java.io.ByteArrayInputStream;
import org.junit.jupiter.api.Test;

class ProcessExecutorTest extends BaseUnit {

  @Test
  void givenValidCommand_whenExecute_thenCapturesOutputAndSuccess() throws InterruptedException {
    // given
    when(process.getInputStream())
        .thenReturn(new ByteArrayInputStream("hello\nworld\n".getBytes()));
    when(process.waitFor()).thenReturn(0);

    // when
    final ExecutionResult result = processExecutorImpl.execute(workingDir, cmd);

    // then
    assertThat(result.isSuccess()).isTrue();
    assertThat(result.logs()).isEqualTo("hello\nworld\n");
  }

  @Test
  void givenNonZeroExitCommand_whenExecute_thenAppendsErrorAndFailure() throws Exception {
    // given
    when(process.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
    when(process.waitFor()).thenReturn(2);

    // when
    final ExecutionResult result = processExecutorImpl.execute(workingDir, cmd);

    // then
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.logs()).isEqualTo("[[ERROR]] Command failed: git pull");
  }

  @Test
  void givenNonExistingCommand_whenExecute_thenReturnsFailureWithMessage() {
    // given
    processExecutorImpl = new ProcessExecutor(new ProcessRunner(), new OutputCollector());

    // when
    final ExecutionResult result =
        processExecutorImpl.execute(workingDir, "__definitely_not_a_command__");

    // then
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.logs())
        .isEqualTo(
            "[[ERROR]] IO failure: Cannot run program \"__definitely_not_a_command__\" "
                + "(in directory \""
                + tempDir.getAbsolutePath()
                + "\"): error=2, "
                + "No such file or directory\n");
  }

  @Test
  void givenInterruptedProcess_whenExecute_thenReturnsFailureWithMessage() throws Exception {
    // given
    when(process.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
    when(process.waitFor()).thenThrow(new InterruptedException("stop"));

    when(processRunner.start(workingDir, "noop")).thenReturn(process);

    // when
    final ExecutionResult result = processExecutorImpl.execute(workingDir, "noop");

    // then
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.logs()).isEqualTo("[[ERROR]] Interrupted: stop\n");

    Thread.interrupted();
  }
}
