package io.github.tomaszziola.javabuildautomaton.utils;

import static java.util.regex.Pattern.quote;
import static org.hamcrest.text.MatchesPattern.matchesPattern;

import org.hamcrest.Matcher;

public final class MyMatchers {
  private MyMatchers() {}

  public static Matcher<String> containsWholeWordOutsideUrls(final String word) {
    final var quote = quote(word);
    final var urlSegment = "[^\\s\"'>]*";
    final var notInUrlLookbehind = "(?<!https?://" + urlSegment + ")";
    final var regex = "(?s).*" + notInUrlLookbehind + "\\b" + quote + "\\b.*";
    return matchesPattern(regex);
  }
}
