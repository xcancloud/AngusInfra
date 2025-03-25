package cloud.xcan.angus.spec.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariableReplacer {

  /**
   * Regular expressions match ${VARIABLE_NAME}
   */
  private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^\\}]+)\\}");

  /**
   * Replace variables in a string
   *
   * @param input     Input string
   * @param variables Variable mapping
   * @return The string after replacement
   */
  public static String replaceVariables(String input, Map<String, Object> variables) {
    Matcher matcher = VARIABLE_PATTERN.matcher(input);
    StringBuilder result = new StringBuilder();
    while (matcher.find()) {
      String variableName = matcher.group(1);
      Object replacement = variables.get(variableName);
      if (replacement != null) {
        matcher.appendReplacement(result, replacement.toString());
      }
    }
    matcher.appendTail(result);
    return result.toString();
  }


  /**
   * Replace variables in text file
   *
   * @param inputFile  Input file path
   * @param outputFile Output file path
   * @param variables  Variable mapping
   * @throws IOException throws IOException if an I/O error occurs
   */
  public static void replaceVariables(String inputFile, String outputFile,
      Map<String, Object> variables) throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
      String line;
      while ((line = reader.readLine()) != null) {
        writer.write(replaceVariablesInLine(line, variables));
        writer.newLine();
      }
    }
  }

  /**
   * Replace variables in a single line of text
   *
   * @param line      The line where variables need to be replaced
   * @param variables Variable mapping
   * @return The line after replacement
   */
  private static String replaceVariablesInLine(String line, Map<String, Object> variables) {
    return replaceVariables(line, variables);
  }

}
