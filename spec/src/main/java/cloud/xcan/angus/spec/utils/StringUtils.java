package cloud.xcan.angus.spec.utils;

import static cloud.xcan.angus.spec.SpecConstant.CHAR_CARRIAGE_RETURN;
import static cloud.xcan.angus.spec.SpecConstant.CHAR_FORM_FEED;
import static cloud.xcan.angus.spec.SpecConstant.CHAR_NEW_LINE;
import static cloud.xcan.angus.spec.SpecConstant.CHAR_SPACE;
import static cloud.xcan.angus.spec.SpecConstant.CHAR_TAB;
import static cloud.xcan.angus.spec.SpecConstant.CHAR_VERTICAL_TAB;
import static cloud.xcan.angus.spec.SpecConstant.DEFAULT_ENCODING;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class StringUtils extends org.apache.commons.lang3.StringUtils {

  public static final int CR = 13; // <US-ASCII CR, carriage return (13)>
  public static final int LF = 10; // <US-ASCII LF, linefeed (10)>
  public static final int SP = 32; // <US-ASCII SP, space (32)>
  public static final int HT = 9;  // <US-ASCII HT, horizontal-tab (9)>

  public static final Pattern CAMEL_CASE_PATTERN = Pattern
      .compile("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])");

  public static final Pattern NUMERIC_PATTERN = Pattern
      .compile("-?\\d+(\\.\\d+)?");

  public static final String LINE_SEPARATOR = System.lineSeparator();


  private StringUtils() { /* no instance */ }

  public static boolean isNumeric(String str) {
    return NUMERIC_PATTERN.matcher(str).matches();
  }

  public static Integer toInteger(StringBuilder value) {
    return Integer.parseInt(value.toString());
  }

  public static String toString(StringBuilder value) {
    return value.toString();
  }

  public static Boolean toBoolean(StringBuilder value) {
    return Boolean.valueOf(value.toString());
  }

  public static String fromInteger(Integer value) {
    return Integer.toString(value);
  }

  public static String fromLong(Long value) {
    return Long.toString(value);
  }

  public static String fromShort(Short value) {
    return Short.toString(value);
  }

  public static String fromString(String value) {
    return value;
  }

  public static String fromBoolean(Boolean value) {
    return Boolean.toString(value);
  }

  public static String fromBigInteger(BigInteger value) {
    return value.toString();
  }

  public static String fromBigDecimal(BigDecimal value) {
    return value.toString();
  }

  public static BigInteger toBigInteger(String s) {
    return new BigInteger(s);
  }

  public static BigDecimal toBigDecimal(String s) {
    return new BigDecimal(s);
  }

  public static String fromFloat(Float value) {
    return Float.toString(value);
  }

  /**
   * Returns the string representation of the specified double.
   *
   * @param d The double to represent as a string.
   * @return The string representation of the specified double.
   */
  public static String fromDouble(Double d) {
    return Double.toString(d);
  }

  /**
   * Returns the string representation of the specified Byte.
   *
   * @param b The Byte to represent as a string.
   * @return The string representation of the specified Byte.
   */
  public static String fromByte(Byte b) {
    return Byte.toString(b);
  }

  public static String replace(String originalString, String partToMatch, String replacement) {
    StringBuilder buffer = new StringBuilder(originalString.length());
    buffer.append(originalString);

    int indexOf = buffer.indexOf(partToMatch);
    while (indexOf != -1) {
      buffer = buffer.replace(indexOf, indexOf + partToMatch.length(), replacement);
      indexOf = buffer.indexOf(partToMatch, indexOf + replacement.length());
    }

    return buffer.toString();
  }

  /**
   * Joins the strings in parts with joiner between each string
   *
   * @param joiner the string to insert between the strings in parts
   * @param parts  the parts to join
   */
  public static String join(String joiner, String... parts) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < parts.length; i++) {
      builder.append(parts[i]);
      if (i < parts.length - 1) {
        builder.append(joiner);
      }
    }
    return builder.toString();
  }

  /**
   * A null-safe trim method. If the input string is null, returns null; otherwise returns a trimmed
   * version of the input.
   */
  public static String trim(String value) {
    if (value == null) {
      return null;
    }
    return value.trim();
  }

  /**
   * @return true if the given value is either null or the empty string
   */
  public static boolean isNullOrEmpty(String value) {
    return value == null || value.isEmpty();
  }

  /**
   * @return true if the given value is either null or the empty string
   */
  public static boolean isNotNullOrEmpty(String value) {
    return !isNullOrEmpty(value);
  }

  /**
   * @return true if the given value is non-null and non-empty
   */
  public static boolean hasValue(String str) {
    return !isNullOrEmpty(str);
  }

  /**
   * Tests a char to see if is it whitespace. This method considers the same characters to be white
   * space as the Pattern class does when matching \s
   *
   * @param ch the character to be tested
   * @return true if the character is white  space, false otherwise.
   */
  private static boolean isWhiteSpace(final char ch) {
    if (ch == CHAR_SPACE) {
      return true;
    }
    if (ch == CHAR_TAB) {
      return true;
    }
    if (ch == CHAR_NEW_LINE) {
      return true;
    }
    if (ch == CHAR_VERTICAL_TAB) {
      return true;
    }
    if (ch == CHAR_CARRIAGE_RETURN) {
      return true;
    }
    if (ch == CHAR_FORM_FEED) {
      return true;
    }
    return false;
  }

  public static String removeSpace(final String source) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < source.length(); i++) {
      char ch = source.charAt(i);
      if (isWhiteSpace(ch)) {
        continue;
      }
      sb.append(ch);
    }
    return sb.toString();
  }

  /**
   * This method appends a string to a string builder and collapses contiguous white space is a
   * single space.
   * <p>
   * This is equivalent to: destination.append(source.replaceAll("\\s+", " ")) but does not create a
   * Pattern object that needs to compile the match string; it also prevents us from having to make
   * a Matcher object as well.
   */
  public static void appendCompactedString(final StringBuilder destination, final String source) {
    boolean previousIsWhiteSpace = false;
    int length = source.length();

    for (int i = 0; i < length; i++) {
      char ch = source.charAt(i);
      if (isWhiteSpace(ch)) {
        if (previousIsWhiteSpace) {
          continue;
        }
        destination.append(CHAR_SPACE);
        previousIsWhiteSpace = true;
      } else {
        destination.append(ch);
        previousIsWhiteSpace = false;
      }
    }
  }

  /**
   * Performs a case-insensitive comparison and returns true if the data begins with the given
   * sequence.
   */
  public static boolean beginsWithIgnoreCase(final String data, final String seq) {
    return data.regionMatches(true, 0, seq, 0, seq.length());
  }

  /**
   * Searches a string for the first occurrence of a character specified by a list of characters.
   *
   * @param s            The string to search.
   * @param charsToMatch A list of characters to search the string for.
   * @return The character that was first matched in the string or null if none of the characters
   * were found.
   */
  public static Character findFirstOccurrence(String s, char... charsToMatch) {
    int lowestIndex = Integer.MAX_VALUE;

    for (char toMatch : charsToMatch) {
      int currentIndex = s.indexOf(toMatch);
      if (currentIndex != -1 && currentIndex < lowestIndex) {
        lowestIndex = currentIndex;
      }
    }

    return lowestIndex == Integer.MAX_VALUE ? null : s.charAt(lowestIndex);
  }

  /**
   * @param v The string to escape.
   * @return An escaped JSON string.
   */
  public static String escapeJson(String v) {
    // @Nullable
    if (v == null) {
      return "";
    }
    int length = v.length();
    if (length == 0) {
      return v;
    }

    int afterReplacement = 0;
    StringBuilder builder = null;
    for (int i = 0; i < length; i++) {
      char c = v.charAt(i);
      String replacement;
      if (c < 0x80) {
        replacement = REPLACEMENT_CHARS[c];
        if (replacement == null) {
          continue;
        }
      } else if (c == '\u2028') {
        replacement = U2028;
      } else if (c == '\u2029') {
        replacement = U2029;
      } else {
        continue;
      }
      // write characters between the last replacement and now
      if (afterReplacement < i) {
        if (builder == null) {
          builder = new StringBuilder(length);
        }
        builder.append(v, afterReplacement, i);
      }
      if (builder == null) {
        builder = new StringBuilder(length);
      }
      builder.append(replacement);
      afterReplacement = i + 1;
    }
    if (builder == null) {
      return v; // then we didn't escape anything
    }

    if (afterReplacement < length) {
      builder.append(v, afterReplacement, length);
    }
    return builder.toString();
  }

  /*
   * From RFC 7159, "All Unicode characters may be placed within the
   * quotation marks except for the characters that must be escaped:
   * quotation mark, reverse solidus, and the control characters
   * (U+0000 through U+001F)."
   *
   * We also escape '\u2028' and '\u2029', which JavaScript interprets as
   * newline characters. This prevents eval() from failing with a syntax
   * error. https://github.com/google/gson/issues/341
   */
  private static final String[] REPLACEMENT_CHARS;

  static {
    REPLACEMENT_CHARS = new String[128];
    for (int i = 0; i <= 0x1f; i++) {
      REPLACEMENT_CHARS[i] = String.format("\\u%04x", (int) i);
    }
    REPLACEMENT_CHARS['"'] = "\\\"";
    REPLACEMENT_CHARS['\\'] = "\\\\";
    REPLACEMENT_CHARS['\t'] = "\\t";
    REPLACEMENT_CHARS['\b'] = "\\b";
    REPLACEMENT_CHARS['\n'] = "\\n";
    REPLACEMENT_CHARS['\r'] = "\\r";
    REPLACEMENT_CHARS['\f'] = "\\f";
  }

  private static final String U2028 = "\\u2028";
  private static final String U2029 = "\\u2029";

  /**
   * Hump to underline
   */
  public static String camelToUnder(String str) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      if (Character.isUpperCase(c)) {
        sb.append('_').append(Character.toLowerCase(c));
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  /**
   * Underline to hump
   */
  public static String underToCamel(String str) {
    StringBuilder sb = new StringBuilder();
    String[] words = str.split("_");
    for (int i = 0; i < words.length; i++) {
      String word = words[i];
      if (!word.isEmpty()) {
        if (i > 0) {
          sb.append(Character.toUpperCase(word.charAt(0)));
        } else {
          sb.append(Character.toLowerCase(word.charAt(0)));
        }
        sb.append(word.substring(1));
      }
    }
    return sb.toString();
  }

  /**
   * Underline to hump
   */
  public static String underToUpperCamel(String str) {
    return underToUpperCamel(str, false);
  }

  /**
   * Underline to hump
   */
  public static String underToUpperCamel(String str, boolean strict) {
    StringBuilder sb = new StringBuilder();
    String[] words = str.split("_");
    for (String word : words) {
      if (!word.isEmpty()) {
        sb.append(Character.toUpperCase(word.charAt(0)));
        sb.append(strict ? word.substring(1).toLowerCase() : word.substring(1));
      }
    }
    return sb.toString();
  }

  public static List<String> camelSplit(String input) {
    Matcher matcher = CAMEL_CASE_PATTERN.matcher(input);
    StringBuilder sb = new StringBuilder();

    while (matcher.find()) {
      matcher.appendReplacement(sb, " " + matcher.group());
    }
    matcher.appendTail(sb);
    return Arrays.stream(sb.toString().split(" ")).map(String::toLowerCase)
        .collect(Collectors.toList());
  }

  /**
   * Join the provided {@code elements} separated by the {@code delimiter}.
   *
   * @param delimiter delimiter
   * @param elements  elements to join
   * @return the {link @String} result obtained from joining all elements
   */
  public static String join(CharSequence delimiter, CharSequence... elements) {
    StringBuilder builder = new StringBuilder();
    boolean first = true;
    for (CharSequence element : elements) {
      if (first) {
        first = false;
      } else {
        builder.append(delimiter);
      }
      builder.append(element);
    }
    return builder.toString();
  }

  /**
   * Format the string. Replace "{}" with %s and format the string using
   * {@link String#format(String, Object...)}.
   */
  public static String format(String str, Object... args) {
    str = str.replaceAll("\\{}", "%s");

    return String.format(str, args);
  }

  /**
   * <p>Adds a substring only if the source string does not already start with the substring,
   * otherwise returns the source string.</p>
   * <p/>
   * <p>A {@code null} source string will return {@code null}.
   * An empty ("") source string will return the empty string. A {@code null} search string will
   * return the source string.</p>
   * <p/>
   * <pre>
   * StringUtils.addStart(null, *)      = *
   * StringUtils.addStart("", *)        = *
   * StringUtils.addStart(*, null)      = *
   * StringUtils.addStart("domain.com", "www.")  = "www.domain.com"
   * StringUtils.addStart("abc123", "abc")    = "abc123"
   * </pre>
   *
   * @param str the source String to search, may be null
   * @param add the String to search for and add, may be null
   * @return the substring with the string added if required
   */
  public static String addStart(String str, String add) {
    if (isNullOrEmpty(add)) {
      return str;
    }
    if (isNullOrEmpty(str)) {
      return add;
    }
    if (!str.startsWith(add)) {
      return add + str;
    }
    return str;
  }

  public static String[] tokenizeToStringArray(String str, String delimiters) {
    return tokenizeToStringArray(str, delimiters, true, true);
  }

  public static String[] tokenizeToStringArray(String str, String delimiters, boolean trimTokens,
      boolean ignoreEmptyTokens) {
    if (str == null) {
      return null;
    }

    StringTokenizer st = new StringTokenizer(str, delimiters);
    List<String> tokens = new ArrayList<String>();
    while (st.hasMoreTokens()) {
      String token = st.nextToken();
      if (trimTokens) {
        token = token.trim();
      }
      if (!ignoreEmptyTokens || !token.isEmpty()) {
        tokens.add(token);
      }
    }
    return toStringArray(tokens);
  }

  public static String[] toStringArray(Collection<String> collection) {
    if (collection == null) {
      return null;
    }
    return collection.toArray(new String[0]);
  }

  public static boolean hasText(String str) {
    return (hasLength(str) && containsText(str));
  }

  public static boolean containsText(CharSequence str) {
    int strLen = str.length();
    for (int i = 0; i < strLen; i++) {
      if (!Character.isWhitespace(str.charAt(i))) {
        return true;
      }
    }
    return false;
  }

  public static boolean containsText(String src, CharSequence like) {
    if (isNull(src) || isNull(like)) {
      return false;
    }
    return src.contains(like);
  }

  public static boolean containsText(String src, CharSequence... likes) {
    if (isNull(src) || isNull(likes)) {
      return false;
    }
    for (CharSequence like : likes) {
      if (!containsText(src, like)) {
        return false;
      }
    }
    return true;
  }

  public static boolean hasLength(String str) {
    return (str != null && !str.isEmpty());
  }

  public static boolean isWhitespace(final char ch) {
    return ch == SP || ch == HT || ch == CR || ch == LF;
  }

  public static Set<String> getUrlParameterValues(String url, String name) {
    return getUrlParameterValues(url, name, null);
  }

  public static Set<String> getUrlParameterValues(String url, String parameter,
      String defaultValue) {
    try {
      url = URLDecoder.decode(url, DEFAULT_ENCODING);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    Set<String> values = new HashSet<>();
    if (url.indexOf('?') != -1) {
      final String contents = url.substring(url.indexOf('?') + 1);
      String[] keyValues = contents.split("&");
      for (String keyValue : keyValues) {
        String key = keyValue.substring(0, keyValue.indexOf("="));
        String value = keyValue.substring(keyValue.indexOf("=") + 1);
        if (key.equals(parameter)) {
          values.add(value);
        }
      }
    }
    if (ObjectUtils.isEmpty(values) && isNotEmpty(defaultValue)) {
      values.add(defaultValue);
    }
    return values;
  }

  public static Map<String, Set<String>> getUrlParameters(String url) {
    try {
      url = URLDecoder.decode(url, DEFAULT_ENCODING);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    Map<String, Set<String>> values = new HashMap<>();
    if (url.indexOf('?') != -1) {
      final String contents = url.substring(url.indexOf('?') + 1);
      String[] keyValues = contents.split("&");
      for (String keyValue : keyValues) {
        String key = keyValue.substring(0, keyValue.indexOf("="));
        String value = keyValue.substring(keyValue.indexOf("=") + 1);
        if (values.containsKey(key)) {
          values.get(key).add(value);
        } else {
          values.put(key, Set.of(value));
        }
      }
    }
    return values;
  }
}
