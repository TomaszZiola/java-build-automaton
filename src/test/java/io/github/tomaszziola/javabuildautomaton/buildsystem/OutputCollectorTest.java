package io.github.tomaszziola.javabuildautomaton.buildsystem;

import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OutputCollectorTest {

  private OutputCollector outputCollector;

  @BeforeEach
  void setUp() {
    outputCollector = new OutputCollector();
  }

  @Test
  @DisplayName(
      "Given input stream with multiple lines, when collecting, then all lines are appended with line separators")
  void collectsMultipleLinesWithLineSeparators() throws IOException {
    // given
    final String input = "line1\nline2\nline3";
    final InputStream inputStream = new ByteArrayInputStream(input.getBytes(UTF_8));
    final StringBuilder target = new StringBuilder();

    // when
    outputCollector.collect(inputStream, target);

    // then
    final String expected =
        "line1" + lineSeparator() + "line2" + lineSeparator() + "line3" + lineSeparator();
    assertThat(target.toString()).isEqualTo(expected);
  }

  @Test
  @DisplayName(
      "Given input stream with single line, when collecting, then single line is appended with line separator")
  void collectsSingleLineWithLineSeparator() throws IOException {
    // given
    final String input = "single line";
    final InputStream inputStream = new ByteArrayInputStream(input.getBytes(UTF_8));
    final StringBuilder target = new StringBuilder();

    // when
    outputCollector.collect(inputStream, target);

    // then
    final String expected = "single line" + lineSeparator();
    assertThat(target.toString()).isEqualTo(expected);
  }

  @Test
  @DisplayName("Given empty input stream, when collecting, then target remains empty")
  void handlesEmptyInputStream() throws IOException {
    // given
    final InputStream inputStream = new ByteArrayInputStream(new byte[0]);
    final StringBuilder target = new StringBuilder();

    // when
    outputCollector.collect(inputStream, target);

    // then
    assertThat(target.toString()).isEmpty();
  }

  @Test
  @DisplayName(
      "Given input stream with only newlines, when collecting, then empty lines are preserved")
  void handlesOnlyNewlines() throws IOException {
    // given
    final String input = "\n\n\n";
    final InputStream inputStream = new ByteArrayInputStream(input.getBytes(UTF_8));
    final StringBuilder target = new StringBuilder();

    // when
    outputCollector.collect(inputStream, target);

    // then
    final String expected = lineSeparator() + lineSeparator() + lineSeparator();
    assertThat(target.toString()).isEqualTo(expected);
  }

  @Test
  @DisplayName(
      "Given input stream with mixed content, when collecting, then preserves exact content")
  void handlesMixedContent() throws IOException {
    // given
    final String input = "line1\n\nline3\n  spaces  \n";
    final InputStream inputStream = new ByteArrayInputStream(input.getBytes(UTF_8));
    final StringBuilder target = new StringBuilder();

    // when
    outputCollector.collect(inputStream, target);

    // then
    final String expected =
        "line1"
            + lineSeparator()
            + lineSeparator()
            + "line3"
            + lineSeparator()
            + "  spaces  "
            + lineSeparator();
    assertThat(target.toString()).isEqualTo(expected);
  }

  @Test
  @DisplayName(
      "Given input stream throws IOException, when collecting, then IOException is propagated")
  void propagatesIOException() throws IOException {
    // given
    final InputStream failingInputStream =
        new InputStream() {
          @Override
          public int read() throws IOException {
            throw new IOException("Stream read error");
          }
        };
    final StringBuilder target = new StringBuilder();

    // when & then
    try (failingInputStream) {
      assertThatThrownBy(() -> outputCollector.collect(failingInputStream, target))
          .isInstanceOf(IOException.class)
          .hasMessage("Stream read error");
    }
  }

  @Test
  @DisplayName(
      "Given input stream with UTF-8 characters, when collecting, then characters are preserved")
  void handlesUtf8Characters() throws IOException {
    // given
    final String input = "Hello 世界\nUTF-8 测试\nEmoji 🚀";
    final InputStream inputStream = new ByteArrayInputStream(input.getBytes(UTF_8));
    final StringBuilder target = new StringBuilder();

    // when
    outputCollector.collect(inputStream, target);

    // then
    final String expected =
        "Hello 世界" + lineSeparator() + "UTF-8 测试" + lineSeparator() + "Emoji 🚀" + lineSeparator();
    assertThat(target.toString()).isEqualTo(expected);
  }

  @Test
  @DisplayName("Given large input stream, when collecting, then all content is collected")
  void handlesLargeInput() throws IOException {
    // given
    final StringBuilder inputBuilder = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      inputBuilder.append("Line ").append(i).append('\n');
    }
    final String input = inputBuilder.toString();
    final InputStream inputStream = new ByteArrayInputStream(input.getBytes(UTF_8));
    final StringBuilder target = new StringBuilder();

    // when
    outputCollector.collect(inputStream, target);

    // then
    assertThat(target.toString()).hasLineCount(1000);
    assertThat(target.toString()).contains("Line 0" + lineSeparator());
    assertThat(target.toString()).contains("Line 999" + lineSeparator());
  }

  @Test
  @DisplayName("Given existing content in target, when collecting, then new content is appended")
  void appendsToExistingContent() throws IOException {
    // given
    final String input = "new content";
    final InputStream inputStream = new ByteArrayInputStream(input.getBytes(UTF_8));
    final StringBuilder target = new StringBuilder("existing content");

    // when
    outputCollector.collect(inputStream, target);

    // then
    final String expected = "existing content" + "new content" + lineSeparator();
    assertThat(target.toString()).isEqualTo(expected);
  }
}
