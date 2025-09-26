package io.github.tomaszziola.javabuildautomaton.buildsystem;

import java.io.File;

public record ValidationResult(boolean isValid, File workingDirectory) {}
