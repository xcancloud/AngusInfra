package cloud.xcan.angus.spec.utils;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ObjectsUtilsTest {

  @Test
  public void distinctTest() {
    List<String> values = new ArrayList<>();
    values.add("123");
    values.add("123");
    values.add("235");
    List<String> noRepeat = ObjectUtils.distinct(values);
    assertEquals(noRepeat.size(), 2);
    assertEquals(noRepeat.stream().filter(s -> s.equals("123")).count(), 1);
  }

  @Test
  public void duplicateTest() {
    List<String> values = new ArrayList<>();
    values.add("123");
    values.add("123");
    values.add("235");
    List<String> noRepeat = ObjectUtils.duplicate(values);
    assertEquals(noRepeat.size(), 1);
    assertEquals(noRepeat.stream().filter(s -> s.equals("123")).count(), 1);
  }

  @Test
  public void isEmpty_NullCollection_ReturnsTrue() {
    assertTrue(ObjectUtils.isNullOrEmpty((Collection<?>) null));
  }

  @Test
  public void isEmpty_EmptyCollection_ReturnsTrue() {
    assertTrue(ObjectUtils.isNullOrEmpty(Collections.emptyList()));
  }

  @Test
  public void isEmpty_NonEmptyCollection_ReturnsFalse() {
    assertFalse(ObjectUtils.isNullOrEmpty(singletonList("something")));
  }

  @Test
  public void firstIfPresent_NullList_ReturnsNull() {
    List<String> list = null;
    assertThat(ObjectUtils.firstIfPresent(list)).isNull();
  }

  @Test
  public void firstIfPresent_EmptyList_ReturnsNull() {
    List<String> list = Collections.emptyList();
    assertThat(ObjectUtils.firstIfPresent(list)).isNull();
  }

  @Test
  public void firstIfPresent_SingleElementList_ReturnsOnlyElement() {
    assertThat(ObjectUtils.firstIfPresent(singletonList("foo"))).isEqualTo("foo");
  }

  @Test
  public void firstIfPresent_MultipleElementList_ReturnsFirstElement() {
    assertThat(ObjectUtils.firstIfPresent(Arrays.asList("foo", "bar", "baz"))).isEqualTo("foo");
  }

  @Test
  public void firstIfPresent_FirstElementNull_ReturnsNull() {
    assertThat(ObjectUtils.firstIfPresent(Arrays.asList(null, "bar", "baz"))).isNull();
  }

  @Test
  public void inverseMap_EmptyList_ReturnsNull() {
    Map<String, String> map = Collections.emptyMap();
    assertThat(ObjectUtils.inverseMap(map)).isEmpty();
  }

  @Test
  public void inverseMap_SingleElementList_InversesKeyAndValue() {
    assertThat(ObjectUtils.inverseMap(Collections.singletonMap("foo", "bar")).get("bar"))
        .isEqualTo("foo");
  }

  @Test
  public void inverseMap_MultipleElementList_InversesKeyAndValue() {
    Map<String, String> map = new HashMap<>();
    map.put("key1", "value1");
    map.put("key2", "value2");
    map.put("key3", "value3");
    Map<String, String> inverseMap = ObjectUtils.inverseMap(map);
    assertThat(inverseMap.get("value1")).isEqualTo("key1");
    assertThat(inverseMap.get("value2")).isEqualTo("key2");
    assertThat(inverseMap.get("value3")).isEqualTo("key3");
  }

  @Test
  public void unmodifiableMapOfListsIsUnmodifiable() {
    assertUnsupported(m -> m.clear());
    assertUnsupported(m -> m.compute(null, null));
    assertUnsupported(m -> m.computeIfAbsent(null, null));
    assertUnsupported(m -> m.computeIfPresent(null, null));
    assertUnsupported(m -> m.forEach((k, v) -> v.clear()));
    assertUnsupported(m -> m.get("foo").clear());
    assertUnsupported(m -> m.getOrDefault("", emptyList()).clear());
    assertUnsupported(m -> m.getOrDefault("foo", null).clear());
    assertUnsupported(m -> m.merge(null, null, null));
    assertUnsupported(m -> m.put(null, null));
    assertUnsupported(m -> m.putAll(null));
    assertUnsupported(m -> m.putIfAbsent(null, null));
    assertUnsupported(m -> m.remove(null));
    assertUnsupported(m -> m.remove(null, null));
    assertUnsupported(m -> m.replace(null, null));
    assertUnsupported(m -> m.replace(null, null, null));
    assertUnsupported(m -> m.replaceAll(null));
    assertUnsupported(m -> m.values().clear());

    assertUnsupported(m -> m.keySet().clear());
    assertUnsupported(m -> m.keySet().add(null));
    assertUnsupported(m -> m.keySet().addAll(null));
    assertUnsupported(m -> m.keySet().remove(null));
    assertUnsupported(m -> m.keySet().removeAll(null));
    assertUnsupported(m -> m.keySet().retainAll(null));

    assertUnsupported(m -> m.entrySet().clear());
    assertUnsupported(m -> m.entrySet().add(null));
    assertUnsupported(m -> m.entrySet().addAll(null));
    assertUnsupported(m -> m.entrySet().remove(null));
    assertUnsupported(m -> m.entrySet().removeAll(null));
    assertUnsupported(m -> m.entrySet().retainAll(null));
    assertUnsupported(m -> m.entrySet().iterator().next().setValue(emptyList()));

    assertUnsupported(m -> m.values().clear());
    assertUnsupported(m -> m.values().add(null));
    assertUnsupported(m -> m.values().addAll(null));
    assertUnsupported(m -> m.values().remove(null));
    assertUnsupported(m -> m.values().removeAll(null));
    assertUnsupported(m -> m.values().retainAll(null));

    assertUnsupported(m -> m.values().iterator().next().clear());

    assertUnsupported(m -> {
      Iterator<Entry<String, List<String>>> i = m.entrySet().iterator();
      i.next();
      i.remove();
    });

    assertUnsupported(m -> {
      Iterator<List<String>> i = m.values().iterator();
      i.next();
      i.remove();
    });

    assertUnsupported(m -> {
      Iterator<String> i = m.keySet().iterator();
      i.next();
      i.remove();
    });
  }

  @Test
  public void unmodifiableMapOfListsIsReadable() {
    assertSupported(m -> m.containsKey("foo"));
    assertSupported(m -> m.containsValue("foo"));
    assertSupported(m -> m.equals(null));
    assertSupported(m -> m.forEach((k, v) -> {
    }));
    assertSupported(m -> m.get("foo"));
    assertSupported(m -> m.getOrDefault("foo", null));
    assertSupported(m -> m.hashCode());
    assertSupported(m -> m.isEmpty());
    assertSupported(m -> m.keySet());
    assertSupported(m -> m.size());

    assertSupported(m -> m.keySet().contains(null));
    assertSupported(m -> m.keySet().containsAll(emptyList()));
    assertSupported(m -> m.keySet().equals(null));
    assertSupported(m -> m.keySet().hashCode());
    assertSupported(m -> m.keySet().isEmpty());
    assertSupported(m -> m.keySet().size());
    assertSupported(m -> m.keySet().spliterator());
    assertSupported(m -> m.keySet().toArray());
    assertSupported(m -> m.keySet().toArray(new String[0]));
    assertSupported(m -> m.keySet().stream());

    assertSupported(m -> m.entrySet().contains(null));
    assertSupported(m -> m.entrySet().containsAll(emptyList()));
    assertSupported(m -> m.entrySet().equals(null));
    assertSupported(m -> m.entrySet().hashCode());
    assertSupported(m -> m.entrySet().isEmpty());
    assertSupported(m -> m.entrySet().size());
    assertSupported(m -> m.entrySet().spliterator());
    assertSupported(m -> m.entrySet().toArray());
    assertSupported(m -> m.entrySet().toArray(new Entry[0]));
    assertSupported(m -> m.entrySet().stream());

    assertSupported(m -> m.values().contains(null));
    assertSupported(m -> m.values().containsAll(emptyList()));
    assertSupported(m -> m.values().equals(null));
    assertSupported(m -> m.values().hashCode());
    assertSupported(m -> m.values().isEmpty());
    assertSupported(m -> m.values().size());
    assertSupported(m -> m.values().spliterator());
    assertSupported(m -> m.values().toArray());
    assertSupported(m -> m.values().toArray(new Collection[0]));
    assertSupported(m -> m.values().stream());

    assertSupported(m -> m.entrySet().iterator().next());
    assertSupported(m -> m.entrySet().iterator().hasNext());
    assertSupported(m -> m.values().iterator().next());
    assertSupported(m -> m.values().iterator().hasNext());
    assertSupported(m -> m.keySet().iterator().next());
    assertSupported(m -> m.keySet().iterator().hasNext());
  }

  public void assertUnsupported(Consumer<Map<String, List<String>>> mutation) {
    Map<String, List<String>> map = new HashMap<>();
    map.put("foo", singletonList("bar"));

    assertThatThrownBy(() -> mutation.accept(ObjectUtils.unmodifiableMapOfLists(map)))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  public void assertSupported(Consumer<Map<String, List<String>>> mutation) {
    Map<String, List<String>> map = new HashMap<>();
    map.put("foo", singletonList("bar"));

    mutation.accept(map);
  }

  @Test
  public void uniqueIndex_noDuplicateIndices_correctlyIndexes() {
    Set<String> values = Stream.of("a", "ab", "abc")
        .collect(Collectors.toSet());
    Map<Integer, String> map = ObjectUtils.uniqueIndex(values, String::length);
    assertThat(map).hasSize(3)
        .containsEntry(1, "a")
        .containsEntry(2, "ab")
        .containsEntry(3, "abc");
  }

  @Test
  public void uniqueIndex_map_isModifiable() {
    Set<String> values = Stream.of("a", "ab", "abc")
        .collect(Collectors.toSet());
    Map<Integer, String> map = ObjectUtils.uniqueIndex(values, String::length);
    map.put(3, "bar");
    assertThat(map).containsEntry(3, "bar");
  }

  @Test
  public void uniqueIndex_duplicateIndices_throws() {
    Set<String> values = Stream.of("foo", "bar")
        .collect(Collectors.toSet());
    assertThatThrownBy(() -> ObjectUtils.uniqueIndex(values, String::length))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContainingAll("foo", "bar", "3");
  }

  @Test
  public void testCleanUrl() {
    String cleaned = ObjectUtils.cleanUrl("http://foo/bar/com/{v2}/fun");
    assertEquals(cleaned, "http://foo/bar/com/%7Bv2%7D/fun");

    cleaned = ObjectUtils.cleanUrl(
        "http://westus.dev.cognitive.microsoft.com/docs/services/563879b61984550e40cbbe8d/export?DocumentFormat=Swagger&ApiName=Face API - V1.0");
    assertEquals(cleaned,
        "http://westus.dev.cognitive.microsoft.com/docs/services/563879b61984550e40cbbe8d/export?DocumentFormat=Swagger&ApiName=Face%20API%20-%20V1.0");
  }

  @Test
  public void testReplace1() {
    org.junit.Assert.assertEquals("xyzdef", ObjectUtils.replaceFirst("abcdef", "abc", "xyz"));
  }

  @Test
  public void testReplace2() {
    org.junit.Assert.assertEquals("axyzdef", ObjectUtils.replaceFirst("abcdef", "bc", "xyz"));
  }

  @Test
  public void testReplace3() {
    org.junit.Assert.assertEquals("abcxyz", ObjectUtils.replaceFirst("abcdef", "def", "xyz"));
  }

  @Test
  public void testReplace4() {
    org.junit.Assert.assertEquals("abcdef", ObjectUtils.replaceFirst("abcdef", "bce", "xyz"));
  }

  @Test
  public void testReplace5() {
    org.junit.Assert.assertEquals("abcdef", ObjectUtils.replaceFirst("abcdef", "alt=\"\" ", ""));
  }

  @Test
  public void testReplace6() {
    org.junit.Assert.assertEquals("abcdef", ObjectUtils.replaceFirst("abcdef", "alt=\"\" ", ""));
  }

  @Test
  public void testReplace7() {
    org.junit.Assert.assertEquals("alt=\"\"",
        ObjectUtils.replaceFirst("alt=\"\"", "alt=\"\" ", ""));
  }

  @Test
  public void testReplace8() {
    org.junit.Assert.assertEquals("img src=xyz ",
        ObjectUtils.replaceFirst("img src=xyz alt=\"\" ", "alt=\"\" ", ""));
  }

  // Note: the split tests should agree as far as possible with CSVSaveService.csvSplitString()

  // Tests for split(String,String,boolean)
  @Test
  public void testSplitStringStringTrueWithTrailingSplitChars() {
    // Test ignore trailing split characters
    // Ignore adjacent delimiters
    MatcherAssert.assertThat("Ignore trailing split chars", ObjectUtils.split("a,bc,,", ",", true),
        CoreMatchers.equalTo(new String[]{"a", "bc"}));
  }

  @Test
  public void testSplitStringStringFalseWithTrailingSplitChars() {
    // Test ignore trailing split characters
    MatcherAssert.assertThat("Include the trailing split chars",
        ObjectUtils.split("a,bc,,", ",", false),
        CoreMatchers.equalTo(new String[]{"a", "bc", "", ""}));
  }

  @Test
  public void testSplitStringStringTrueWithLeadingSplitChars() {
    // Test leading split characters
    MatcherAssert.assertThat("Ignore leading split chars", ObjectUtils.split(",,a,bc", ",", true),
        CoreMatchers.equalTo(new String[]{"a", "bc"}));
  }

  @Test
  public void testSplitStringStringFalseWithLeadingSplitChars() {
    // Test leading split characters
    MatcherAssert.assertThat("Include leading split chars", ObjectUtils.split(",,a,bc", ",", false),
        CoreMatchers.equalTo(new String[]{"", "", "a", "bc"}));
  }

  @Test
  public void testSplit3() {
    String in = "a,bc,,"; // Test ignore trailing split characters
    String[] out = ObjectUtils.split(in, ",", true);// Ignore adjacent delimiters
    MatcherAssert.assertThat(out, CoreMatchers.equalTo(new String[]{"a", "bc"}));
    out = ObjectUtils.split(in, ",", false);
    MatcherAssert.assertThat(out, CoreMatchers.equalTo(new String[]{"a", "bc", "", ""}));
  }

  @Test
  public void testSplitStringStringTrueWithLeadingComplexSplitCharacters() {
    // Test leading split characters
    MatcherAssert.assertThat(ObjectUtils.split(" , ,a ,bc", " ,", true),
        CoreMatchers.equalTo(new String[]{"a", "bc"}));
  }

  @Test
  public void testSplitStringStringFalseWithLeadingComplexSplitCharacters() {
    // Test leading split characters
    MatcherAssert.assertThat(ObjectUtils.split(" , ,a ,bc", " ,", false),
        CoreMatchers.equalTo(new String[]{"", "", "a", "bc"}));
  }

  @Test
  public void testSplitStringStringTrueTruncate() throws Exception {
    MatcherAssert.assertThat(ObjectUtils.split("a;,b;,;,;,d;,e;,;,f", ";,", true),
        CoreMatchers.equalTo(new String[]{"a", "b", "d", "e", "f"}));
  }

  @Test
  public void testSplitStringStringFalseTruncate() throws Exception {
    MatcherAssert.assertThat(ObjectUtils.split("a;,b;,;,;,d;,e;,;,f", ";,", false),
        CoreMatchers.equalTo(new String[]{"a", "b", "", "", "d", "e", "", "f"}));
  }

  @Test
  public void testSplitStringStringTrueDoubledSplitChar() throws Exception {
    MatcherAssert.assertThat(ObjectUtils.split("a;;b;;;;;;d;;e;;;;f", ";;", true),
        CoreMatchers.equalTo(new String[]{"a", "b", "d", "e", "f"}));
  }

  @Test
  public void testSplitStringStringFalseDoubledSplitChar() throws Exception {
    MatcherAssert.assertThat(ObjectUtils.split("a;;b;;;;;;d;;e;;;;f", ";;", false),
        CoreMatchers.equalTo(new String[]{"a", "b", "", "", "d", "e", "", "f"}));
  }

  // Empty string
  @Test
  public void testEmpty() {
    String[] out = ObjectUtils.split("", ",", false);
    org.junit.Assert.assertEquals(0, out.length);
  }

  // Tests for split(String,String,String)
  @Test
  public void testSplitSSSSingleDelimiterWithDefaultValue() {
    // Test non-empty parameters
    MatcherAssert.assertThat(ObjectUtils.split("a,bc,,", ",", "?"),
        CoreMatchers.equalTo(new String[]{"a", "bc", "?", "?"}));
  }

  @Test
  public void testSplitSSSSingleDelimiterWithEmptyValue() {
    // Empty default
    MatcherAssert.assertThat(ObjectUtils.split("a,bc,,", ",", ""),
        CoreMatchers.equalTo(new String[]{"a", "bc", "", ""}));
  }

  @Test
  public void testSplitSSSEmptyDelimiter() {
    String in = "a,bc,,"; // Empty delimiter
    MatcherAssert.assertThat(ObjectUtils.split(in, "", "?"),
        CoreMatchers.equalTo(new String[]{in}));
  }

  @Test
  public void testSplitSSSMultipleDelimCharsWithDefaultValue() {
    // Multiple delimiters
    MatcherAssert.assertThat(ObjectUtils.split("a,b;c,,", ",;", "?"),
        CoreMatchers.equalTo(new String[]{"a", "b", "c", "?", "?"}));
  }

  @Test
  public void testSplitSSSMultipleDelimCharsWithEmptyValue() {
    // Multiple delimiters
    MatcherAssert.assertThat(ObjectUtils.split("a,b;c,,", ",;", ""),
        CoreMatchers.equalTo(new String[]{"a", "b", "c", "", ""}));
  }

  @Test
  public void testSplitSSSSameDelimiterAsDefaultValue() {
    MatcherAssert.assertThat(ObjectUtils.split("a,bc,,", ",", ","),
        CoreMatchers.equalTo(new String[]{"a", "bc", ",", ","}));
  }

  @Test
  public void testSplitNullStringString() {
    Assertions.assertThrows(
        NullPointerException.class,
        () -> ObjectUtils.split(null, ",", "?"));
  }

  @Test
  public void testSplitStringNullString() {
    Assertions.assertThrows(
        NullPointerException.class,
        () -> ObjectUtils.split("a,bc,,", null, "?"));
  }

  @Test
  public void testSplitStringStringNullWithSingleDelimiter() {
    MatcherAssert.assertThat(ObjectUtils.split("a,bc,,", ",", null),
        CoreMatchers.equalTo(new String[]{"a", "bc"}));
  }

  @Test
  public void testSplitStringStringNullWithMultipleDelimiter() {
    MatcherAssert.assertThat(ObjectUtils.split("a,;bc,;,", ",;", null),
        CoreMatchers.equalTo(new String[]{"a", "bc"}));
  }

  @Test
  public void testSplitSSSWithEmptyInput() {
    String[] out = ObjectUtils.split("", ",", "x");
    org.junit.Assert.assertEquals(0, out.length);
  }

  @Test
  public void testSplitSSSWithEmptyDelimiter() {
    final String in = "a,;bc,;,";
    MatcherAssert.assertThat(ObjectUtils.split(in, "", "x"),
        CoreMatchers.equalTo(new String[]{in}));
  }

  @Test
  public void testreplaceAllChars() {
    org.junit.Assert.assertEquals("", ObjectUtils.replaceAllChars("", ' ', "+"));
    org.junit.Assert.assertEquals("source", ObjectUtils.replaceAllChars("source", ' ', "+"));
    org.junit.Assert.assertEquals("so+rce", ObjectUtils.replaceAllChars("source", 'u', "+"));
    org.junit.Assert.assertEquals("+so+urc+", ObjectUtils.replaceAllChars("esoeurce", 'e', "+"));
    org.junit.Assert.assertEquals("AZAZsoAZurcAZ",
        ObjectUtils.replaceAllChars("eesoeurce", 'e', "AZ"));
    org.junit.Assert.assertEquals("A+B++C+", ObjectUtils.replaceAllChars("A B  C ", ' ', "+"));
    org.junit.Assert.assertEquals("A%20B%20%20C%20",
        ObjectUtils.replaceAllChars("A B  C ", ' ', "%20"));
  }

  @Test
  public void testTrim() {
    org.junit.Assert.assertEquals("", ObjectUtils.trim("", " ;"));
    org.junit.Assert.assertEquals("", ObjectUtils.trim(" ", " ;"));
    org.junit.Assert.assertEquals("", ObjectUtils.trim("; ", " ;"));
    org.junit.Assert.assertEquals("", ObjectUtils.trim(";;", " ;"));
    org.junit.Assert.assertEquals("", ObjectUtils.trim("  ", " ;"));
    org.junit.Assert.assertEquals("abc", ObjectUtils.trim("abc ;", " ;"));
  }

  @Test
  public void testGetByteArraySlice() throws Exception {
    org.junit.Assert.assertArrayEquals(new byte[]{1, 2},
        ObjectUtils.getByteArraySlice(new byte[]{0, 1, 2, 3}, 1, 2));
  }

  @Test
  public void testbaToHexString() {
    org.junit.Assert.assertEquals("", ObjectUtils.baToHexString(new byte[]{}));
    org.junit.Assert.assertEquals("00", ObjectUtils.baToHexString(new byte[]{0}));
    org.junit.Assert.assertEquals("0f107f8081ff",
        ObjectUtils.baToHexString(new byte[]{15, 16, 127, -128, -127, -1}));
  }

  @Test
  public void testBaToHexStringSeparator() {
    org.junit.Assert.assertEquals("", ObjectUtils.baToHexString(new byte[]{}, '-'));
    org.junit.Assert.assertEquals("00", ObjectUtils.baToHexString(new byte[]{0}, '-'));
    org.junit.Assert.assertEquals("0f-10-7f-80-81-ff",
        ObjectUtils.baToHexString(new byte[]{15, 16, 127, -128, -127, -1}, '-'));
  }

  @Test
  public void testbaToByte() throws Exception {
    assertEqualsArray(new byte[]{}, ObjectUtils.baToHexBytes(new byte[]{}));
    assertEqualsArray(new byte[]{'0', '0'}, ObjectUtils.baToHexBytes(new byte[]{0}));
    assertEqualsArray("0f107f8081ff".getBytes(StandardCharsets.UTF_8),
        ObjectUtils.baToHexBytes(new byte[]{15, 16, 127, -128, -127, -1}));
  }

  private void assertEqualsArray(byte[] expected, byte[] actual) {
    org.junit.Assert.assertEquals("arrays must be same length", expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      org.junit.Assert.assertEquals("values must be the same for index: " + i, expected[i],
          actual[i]);
    }
  }

  @Test
  public void testNullifyIfEmptyTrimmed() {
    org.junit.Assert.assertNull(ObjectUtils.nullifyIfEmptyTrimmed(null));
    org.junit.Assert.assertNull(ObjectUtils.nullifyIfEmptyTrimmed("\u0001"));
    org.junit.Assert.assertEquals("1234", ObjectUtils.nullifyIfEmptyTrimmed("1234"));
  }

  @Test
  public void testIsBlank() {
    org.junit.Assert.assertTrue(ObjectUtils.isBlank(""));
    org.junit.Assert.assertTrue(ObjectUtils.isBlank(null));
    org.junit.Assert.assertTrue(ObjectUtils.isBlank("    "));
    org.junit.Assert.assertFalse(ObjectUtils.isBlank(" zdazd dzd "));
  }

  @Test
  public void testRightAlign() {
    StringBuilder in = new StringBuilder("AZE");
    org.junit.Assert.assertEquals("   AZE", ObjectUtils.rightAlign(in, 6).toString());
    in = new StringBuilder("AZERTY");
    org.junit.Assert.assertEquals("AZERTY", ObjectUtils.rightAlign(in, 6).toString());
    in = new StringBuilder("baulpismuth");
    org.junit.Assert.assertEquals("baulpismuth", ObjectUtils.rightAlign(in, 6).toString());
    in = new StringBuilder("A");
    org.junit.Assert.assertEquals("       A", ObjectUtils.rightAlign(in, 8).toString());
    org.junit.Assert.assertEquals("                                 foo",
        ObjectUtils.rightAlign(new StringBuilder("foo"), 39).toString());
  }

  @Test
  public void testLeftAlign() {
    org.junit.Assert.assertEquals("foo  ",
        ObjectUtils.leftAlign(new StringBuilder("foo"), 5).toString());
    org.junit.Assert.assertEquals("foo",
        ObjectUtils.leftAlign(new StringBuilder("foo"), 2).toString());
    org.junit.Assert.assertEquals("foo                                 ",
        ObjectUtils.leftAlign(new StringBuilder("foo"), 39).toString());
  }

  @Test
  public void testBooleanToSTRING() {
    org.junit.Assert.assertEquals("TRUE", ObjectUtils.booleanToSTRING(true));
    org.junit.Assert.assertEquals("FALSE", ObjectUtils.booleanToSTRING(false));
  }

  @Test
  public void testReplaceAllWithRegexWithSearchValueContainedInReplaceValue() {
    // Bug 61054
    org.junit.Assert.assertArrayEquals(new Object[]{"abcd", 1},
        ObjectUtils.replaceAllWithRegex("abc", "abc", "abcd", true));
  }

  @Test
  public void testReplaceAllWithRegex() {
    org.junit.Assert.assertArrayEquals(new Object[]{"toto", 0},
        ObjectUtils.replaceAllWithRegex("toto", "ti", "ta", true));
    org.junit.Assert.assertArrayEquals(new Object[]{"toto", 0},
        ObjectUtils.replaceAllWithRegex("toto", "TO", "TI", true));
    org.junit.Assert.assertArrayEquals(new Object[]{"TITI", 2},
        ObjectUtils.replaceAllWithRegex("toto", "TO", "TI", false));
    org.junit.Assert.assertArrayEquals(new Object[]{"TITI", 2},
        ObjectUtils.replaceAllWithRegex("toto", "to", "TI", true));
    org.junit.Assert.assertArrayEquals(new Object[]{"TITIti", 2},
        ObjectUtils.replaceAllWithRegex("tototi", "to", "TI", true));
    org.junit.Assert.assertArrayEquals(new Object[]{"TOTIti", 1},
        ObjectUtils.replaceAllWithRegex("TOtoti", "to", "TI", true));
    org.junit.Assert.assertArrayEquals(new Object[]{"TOTI", 1},
        ObjectUtils.replaceAllWithRegex("TOtoti", "to.*", "TI", true));
    org.junit.Assert.assertArrayEquals(new Object[]{"TOTI", 1},
        ObjectUtils.replaceAllWithRegex("TOtoti", "to.*ti", "TI", true));
    org.junit.Assert.assertArrayEquals(new Object[]{"TOTITITITIaTITITIti", 7},
        ObjectUtils.replaceAllWithRegex("TO1232a123ti", "[0-9]", "TI", true));
    org.junit.Assert.assertArrayEquals(new Object[]{"TOTIaTIti", 2},
        ObjectUtils.replaceAllWithRegex("TO1232a123ti", "[0-9]+", "TI", true));

    org.junit.Assert.assertArrayEquals(new Object[]{"TO${var}2a${var}ti", 2},
        ObjectUtils.replaceAllWithRegex("TO1232a123ti", "123", "${var}", true));

    org.junit.Assert.assertArrayEquals(new Object[]{"TO${var}2a${var}ti${var2}", 2},
        ObjectUtils.replaceAllWithRegex("TO1232a123ti${var2}", "123", "${var}", true));
  }

  @Test
  public void testReplaceValueWithNullValue() {
    MatcherAssert.assertThat(ObjectUtils.replaceValue(null, null, false, null, null),
        CoreMatchers.is(0));
  }

  @Test
  public void testReplaceValueWithValidValueAndValidSetter() {
    Holder h = new Holder();
    MatcherAssert.assertThat(
        ObjectUtils.replaceValue("\\d+", "${port}", true, "80", s -> h.value = s),
        CoreMatchers.is(1));
    MatcherAssert.assertThat(h.value, CoreMatchers.is("${port}"));
  }

  private static class Holder {

    String value;
  }

  @Test
  public void testReplaceValueWithNullSetterThatGetsCalled() {
    Assertions.assertThrows(
        NullPointerException.class,
        () -> ObjectUtils.replaceValue("\\d+", "${port}", true, "80", null));
  }

  @Test
  public void testUnsplit() {
    org.junit.Assert.assertEquals("", ObjectUtils.unsplit(new Object[]{null, null}, 0));
    org.junit.Assert.assertEquals("11", ObjectUtils.unsplit(new Object[]{null, 1}, 1));
    org.junit.Assert.assertEquals("-26738698", ObjectUtils.unsplit(new Object[]{-26_738_698}, 1));
  }

}
