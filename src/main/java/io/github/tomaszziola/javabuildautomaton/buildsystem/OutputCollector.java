package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.springframework.stereotype.Service;

@Service
public final class OutputCollector {

  public void collect(final InputStream inputStream, final StringBuilder target)
      throws IOException {
    try (var reader = new BufferedReader(new InputStreamReader(inputStream, UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        target.append(line).append(lineSeparator());
      }
    }
  }
}
