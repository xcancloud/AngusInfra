package cloud.xcan.angus.spec.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VariableReplacer {

  /**
   * Regular expressions match ${VARIABLE_NAME}
   */
  private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^\\}]+)\\}");

  private VariableReplacer() {
  }

  /**
   * Replace variables in a string
   *
   * @param input     Input string
   * @param variables Variable mapping
   * @return The string after replacement
   */
  public static String replaceVariables(String input, Map<String, Object> variables) {
    if (input == null || variables == null || variables.isEmpty()) {
      return input;
    }
    Matcher matcher = VARIABLE_PATTERN.matcher(input);
    StringBuilder result = new StringBuilder();
    while (matcher.find()) {
      String variableName = matcher.group(1);
      Object replacement = variables.get(variableName);
      String repl = replacement != null
          ? Matcher.quoteReplacement(replacement.toString())
          : Matcher.quoteReplacement(matcher.group(0));
      matcher.appendReplacement(result, repl);
    }
    matcher.appendTail(result);
    return result.toString();
  }

  /**
   * Replace variables in text file (UTF-8).
   *
   * @param inputFile  Input file path
   * @param outputFile Output file path
   * @param variables  Variable mapping
   * @throws IOException throws IOException if an I/O error occurs
   */
  public static void replaceVariables(String inputFile, String outputFile,
      Map<String, Object> variables) throws IOException {
    Path in = Path.of(inputFile);
    Path out = Path.of(outputFile);
    try (BufferedReader reader = Files.newBufferedReader(in, StandardCharsets.UTF_8);
        BufferedWriter writer = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
      String line;
      while ((line = reader.readLine()) != null) {
        writer.write(replaceVariablesInLine(line, variables));
        writer.newLine();
      }
    }
  }

  private static String replaceVariablesInLine(String line, Map<String, Object> variables) {
    return replaceVariables(line, variables);
  }
}
