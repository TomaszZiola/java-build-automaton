package io.github.tomaszziola.javabuildautomaton.buildsystem;

import java.io.File;
import java.io.IOException;
import org.springframework.stereotype.Service;

@Service
public final class ProcessRunner {

  public Process start(File workingDir, String... command) throws IOException {
    var processBuilder = new ProcessBuilder(command);
    processBuilder.directory(workingDir);
    processBuilder.redirectErrorStream(true);
    return processBuilder.start();
  }
}
