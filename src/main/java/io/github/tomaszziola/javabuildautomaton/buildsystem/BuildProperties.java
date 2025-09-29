package io.github.tomaszziola.javabuildautomaton.buildsystem;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Validated
@ConfigurationProperties(prefix = "build")
public class BuildProperties {

  @Setter
  @Min(1)
  private int maxParallel;

  private final QueueProps queue = new QueueProps();

  @Setter
  @Getter
  public static class QueueProps {
    @Min(1)
    private int capacity;
  }
}
