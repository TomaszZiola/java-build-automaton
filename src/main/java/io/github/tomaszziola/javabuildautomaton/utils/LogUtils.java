package io.github.tomaszziola.javabuildautomaton.utils;

public final class LogUtils {

  private LogUtils() {}

  public static boolean areLogsEquivalent(final String first, final String second) {
    if (first == null && second == null) {
      return true;
    }
    if (first == null || second == null) {
      return false;
    }
    final String firstNormalized = normalize(first);
    final String secondNormalized = normalize(second);
    return firstNormalized.equals(secondNormalized)
        || firstNormalized.contains(secondNormalized)
        || secondNormalized.contains(firstNormalized);
  }

  public static String normalize(final String toNormalize) {
    return toNormalize.replace("\r\n", "\n").replace('\r', '\n').strip();
  }
}
