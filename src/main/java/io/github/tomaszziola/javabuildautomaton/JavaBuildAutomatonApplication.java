package io.github.tomaszziola.javabuildautomaton;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class JavaBuildAutomatonApplication {

  public static void main(final String[] args) {
    SpringApplication.run(JavaBuildAutomatonApplication.class, args);
  }
}
