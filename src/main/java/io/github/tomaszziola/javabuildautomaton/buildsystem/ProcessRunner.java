package io.github.tomaszziola.javabuildautomaton.buildsystem;

import java.io.File;
import java.io.IOException;
import org.springframework.stereotype.Service;

@Service
public final class ProcessRunner {

  public Process start(final File workingDir, final String... command) throws IOException {
    final var processBuilder = new ProcessBuilder(command);
    processBuilder.directory(workingDir);
    processBuilder.redirectErrorStream(true);
    return processBuilder.start();
  }
}
