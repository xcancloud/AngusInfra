package cloud.xcan.sdf.spec.utils;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNull;
import static java.lang.String.format;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RegexpUtils {

  private RegexpUtils() {
    throw new UnsupportedOperationException(
        "RegexpUtils is a utility class - don't instantiate it!");
  }

  private static final Map<String, Pattern> COMPILED_PATTERNS = new ConcurrentHashMap<>();

  public static Pattern getPattern(String regexp) {
    try {
      Pattern pattern = COMPILED_PATTERNS.get(regexp);
      if (isNull(pattern)) {
        pattern = Pattern.compile(regexp);
        COMPILED_PATTERNS.put(regexp, pattern);
      }
      return pattern;
    } catch (Exception e) {
      throw new RuntimeException(format("Error when getting a pattern [%s] from cache", regexp), e);
    }
  }

  public static boolean matches(String stringToCheck, String regexp) {
    return doGetMatcher(stringToCheck, regexp).matches();
  }

  public static Matcher getMatcher(String stringToCheck, String regexp) {
    return doGetMatcher(stringToCheck, regexp);
  }

  private static Matcher doGetMatcher(String stringToCheck, String regexp) {
    Pattern pattern = getPattern(regexp);
    return pattern.matcher(stringToCheck);
  }


  /**
   * Tests if a String matches another String with a wildcard pattern.
   *
   * @param text    The String to test
   * @param pattern The String containing a wildcard pattern where ? represents a single character
   *                and * represents any number of characters. If the first character of the pattern
   *                is a carat (^) the test is performed against the remaining characters and the
   *                result of the test is the opposite.
   * @return True if the String matches or if the first character is ^ and the remainder of the
   * String does not match.
   */
  public static boolean wildcardMatch(String text, String pattern) {
    if (pattern.length() > 0 && pattern.charAt(0) == '^') {
      return !wildcardMatch(text, pattern.substring(1));
    }
    return text.matches(pattern.replace("?", ".?").replace("*", ".*?"));
  }

}