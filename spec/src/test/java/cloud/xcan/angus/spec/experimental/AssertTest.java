package cloud.xcan.angus.spec.experimental;

import static cloud.xcan.angus.spec.experimental.Assert.assertNoNullElements;
import static cloud.xcan.angus.spec.experimental.Assert.assertNotBlank;
import static cloud.xcan.angus.spec.experimental.Assert.assertNotEmpty;
import static cloud.xcan.angus.spec.experimental.Assert.assertNotNull;
import static cloud.xcan.angus.spec.experimental.Assert.assertParamNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AssertTest {

  @Rule
  public ExpectedException expected = ExpectedException.none();

  //-----------------------------------------------------------------------
  @Test
  public void testIsTrue2() {
    Assert.assertTrue(true, "Error");
    try {
      Assert.assertTrue(false, "Error");
      fail("Expecting IllegalArgumentException");
    } catch (final IllegalArgumentException ex) {
      assertEquals("Error", ex.getMessage());
    }
  }

  //-----------------------------------------------------------------------
  @Test
  public void testIsTrue3() {
    Assert.assertTrue(true, "Error", 6);
    try {
      Assert.assertTrue(false, "Error", 6);
      fail("Expecting IllegalArgumentException");
    } catch (final IllegalArgumentException ex) {
      assertEquals("Error", ex.getMessage());
    }
  }

  //-----------------------------------------------------------------------
  @Test
  public void testIsTrue4() {
    Assert.assertTrue(true, "Error", 7);
    try {
      Assert.assertTrue(false, "Error", 7);
      fail("Expecting IllegalArgumentException");
    } catch (final IllegalArgumentException ex) {
      assertEquals("Error", ex.getMessage());
    }
  }

  //-----------------------------------------------------------------------
  @Test
  public void testIsTrue5() {
    Assert.assertTrue(true, "Error", 7.4d);
    try {
      Assert.assertTrue(false, "Error", 7.4d);
      fail("Expecting IllegalArgumentException");
    } catch (final IllegalArgumentException ex) {
      assertEquals("Error", ex.getMessage());
    }
  }

  //-----------------------------------------------------------------------
  @SuppressWarnings("unused")
  @Test
  public void testNotNull2() {
    assertNotNull(new Object(), "Error");
    try {
      assertNotNull(null, "Error");
      fail("Expecting IllegalArgumentException");
    } catch (final IllegalArgumentException ex) {
      assertEquals("Error", ex.getMessage());
    }
  }

  //-----------------------------------------------------------------------
  @Test
  public void testNotEmptyArray2() {
    try {
      assertNotEmpty(new Object[]{null}, "Error");
      fail("Expecting IllegalArgumentException");
    } catch (final IllegalArgumentException ex) {
      assertEquals("Error", ex.getMessage());
    }
    try {
      assertNotEmpty((Object[]) null, "Error");
      fail("Expecting IllegalArgumentException");
    } catch (final IllegalArgumentException ex) {
      assertEquals("Error", ex.getMessage());
    }
    try {
      assertNotEmpty(new Object[0], "Error");
      fail("Expecting IllegalArgumentException");
    } catch (final IllegalArgumentException ex) {
      assertEquals("Error", ex.getMessage());
    }
  }

  //-----------------------------------------------------------------------
  @Test
  public void testNotEmptyCollection2() {
    final Collection<Integer> coll = new ArrayList<>();
    try {
      assertNotEmpty((Collection<?>) null, "Error");
      fail("Expecting IllegalArgumentException");
    } catch (final IllegalArgumentException ex) {
      assertEquals("Error", ex.getMessage());
    }
    try {
      assertNotEmpty(coll, "Error");
      fail("Expecting IllegalArgumentException");
    } catch (final IllegalArgumentException ex) {
      assertEquals("Error", ex.getMessage());
    }
    coll.add(8);
    assertNotEmpty(coll, "Error");
  }

  //-----------------------------------------------------------------------
  @Test
  public void testNotEmptyMap2() {
    final Map<String, Integer> map = new HashMap<>();
    try {
      assertNotEmpty((Map<?, ?>) null, "Error");
      fail("Expecting IllegalArgumentException");
    } catch (final IllegalArgumentException ex) {
      assertEquals("Error", ex.getMessage());
    }
    try {
      assertNotEmpty(map, "Error");
      fail("Expecting IllegalArgumentException");
    } catch (final IllegalArgumentException ex) {
      assertEquals("Error", ex.getMessage());
    }
    map.put("ll", 8);
    assertNotEmpty(map, "Error");
  }

  //-----------------------------------------------------------------------
  @Test
  public void testNotEmptyString2() {
    assertNotEmpty("a", "Error");
    try {
      assertNotEmpty((String) null, "Error");
      fail("Expecting NullPointerException");
    } catch (final NullPointerException ex) {
      assertEquals("Error", ex.getMessage());
    }
    try {
      assertNotEmpty("", "Error");
      fail("Expecting IllegalArgumentException");
    } catch (final IllegalArgumentException ex) {
      assertEquals("Error", ex.getMessage());
    }

    final String str = "Hi";
    final String testStr = assertNotEmpty(str, "Message");
    assertSame(str, testStr);
  }

  //-----------------------------------------------------------------------
  @Test
  public void testNotBlankMsgNullStringShouldThrow() {
    //given
    final String string = null;

    try {
      //when
      assertNotBlank(string, "Message");
      fail("Expecting NullPointerException");
    } catch (final NullPointerException e) {
      //then
      assertEquals("Message", e.getMessage());
    }
  }

  //-----------------------------------------------------------------------
  @Test
  public void testNotBlankMsgBlankStringShouldThrow() {
    //given
    final String string = " \n \t \r \n ";

    try {
      //when
      assertNotBlank(string, "Message");
      fail("Expecting IllegalArgumentException");
    } catch (final IllegalArgumentException e) {
      //then
      assertEquals("Message", e.getMessage());
    }
  }

  //-----------------------------------------------------------------------
  @Test
  public void testNotBlankMsgBlankStringWithWhitespacesShouldThrow() {
    //given
    final String string = "   ";

    try {
      //when
      assertNotBlank(string, "Message");
      fail("Expecting IllegalArgumentException");
    } catch (final IllegalArgumentException e) {
      //then
      assertEquals("Message", e.getMessage());
    }
  }

  //-----------------------------------------------------------------------
  @Test
  public void testNotBlankMsgEmptyStringShouldThrow() {
    //given
    final String string = "";

    try {
      //when
      assertNotBlank(string, "Message");
      fail("Expecting IllegalArgumentException");
    } catch (final IllegalArgumentException e) {
      //then
      assertEquals("Message", e.getMessage());
    }
  }

  //-----------------------------------------------------------------------
  @Test
  public void testNotBlankMsgNotBlankStringShouldNotThrow() {
    //given
    final String string = "abc";

    //when
    assertNotBlank(string, "Message");

    //then should not throw
  }

  //-----------------------------------------------------------------------
  @Test
  public void testNotBlankMsgNotBlankStringWithWhitespacesShouldNotThrow() {
    //given
    final String string = "  abc   ";

    //when
    assertNotBlank(string, "Message");

    //then should not throw
  }

  //-----------------------------------------------------------------------
  @Test
  public void testNotBlankMsgNotBlankStringWithNewlinesShouldNotThrow() {
    //given
    final String string = " \n \t abc \r \n ";

    //when
    assertNotBlank(string, "Message");

    //then should not throw
  }

  //-----------------------------------------------------------------------
  @Test
  public void testNoNullElementsArray2() {
    String[] array = new String[]{"a", "b"};
    assertNoNullElements(array, "Error");
    assertNoNullElements((Object[]) null, "Error");
    array[1] = null;
    try {
      assertNoNullElements(array, "Error");
      fail("Expecting IllegalArgumentException");
    } catch (final IllegalArgumentException ex) {
      assertEquals("Error", ex.getMessage());
    }
  }

  //-----------------------------------------------------------------------
  @Test
  public void testNoNullElementsCollection2() {
    final List<String> coll = new ArrayList<>();
    coll.add("a");
    coll.add("b");
    assertNoNullElements(coll, "Error");
    assertNoNullElements((Collection<?>) null, "Error");
    coll.set(1, null);
    try {
      assertNoNullElements(coll, "Error");
      fail("Expecting IllegalArgumentException");
    } catch (final IllegalArgumentException ex) {
      assertEquals("Error", ex.getMessage());
    }
  }

  @Test
  public void testInclusiveBetween_withMessage() {
    Assert.inclusiveBetween("a", "c", "b", "Error");
    try {
      Assert.inclusiveBetween("0", "5", "6", "Error");
      fail("Expecting IllegalArgumentException");
    } catch (final IllegalArgumentException e) {
      assertEquals("Error", e.getMessage());
    }
  }

  @Test
  public void testInclusiveBetweenLong_withMessage() {
    Assert.inclusiveBetween(0, 2, 1, "Error");
    Assert.inclusiveBetween(0, 2, 2, "Error");
    try {
      Assert.inclusiveBetween(0, 5, 6, "Error");
      fail("Expecting IllegalArgumentException");
    } catch (final IllegalArgumentException e) {
      assertEquals("Error", e.getMessage());
    }
  }

  @Test
  public void testInclusiveBetweenDouble_withMessage() {
    Assert.inclusiveBetween(0.1, 2.1, 1.1, "Error");
    Assert.inclusiveBetween(0.1, 2.1, 2.1, "Error");
    try {
      Assert.inclusiveBetween(0.1, 5.1, 6.1, "Error");
      fail("Expecting IllegalArgumentException");
    } catch (final IllegalArgumentException e) {
      assertEquals("Error", e.getMessage());
    }
  }

  @Test
  public void testExclusiveBetween_withMessage() {
    Assert.exclusiveBetween("a", "c", "b", "Error");
    try {
      Assert.exclusiveBetween("0", "5", "6", "Error");
      fail("Expecting IllegalArgumentException");
    } catch (final IllegalArgumentException e) {
      assertEquals("Error", e.getMessage());
    }
    try {
      Assert.exclusiveBetween("0", "5", "5", "Error");
      fail("Expecting IllegalArgumentException");
    } catch (final IllegalArgumentException e) {
      assertEquals("Error", e.getMessage());
    }
  }

  @Test
  public void testExclusiveBetweenLong_withMessage() {
    Assert.exclusiveBetween(0, 2, 1, "Error");
    try {
      Assert.exclusiveBetween(0, 5, 6, "Error");
      fail("Expecting IllegalArgumentException");
    } catch (final IllegalArgumentException e) {
      assertEquals("Error", e.getMessage());
    }
    try {
      Assert.exclusiveBetween(0, 5, 5, "Error");
      fail("Expecting IllegalArgumentException");
    } catch (final IllegalArgumentException e) {
      assertEquals("Error", e.getMessage());
    }
  }

  @Test
  public void testExclusiveBetweenDouble_withMessage() {
    Assert.exclusiveBetween(0.1, 2.1, 1.1, "Error");
    try {
      Assert.exclusiveBetween(0.1, 5.1, 6.1, "Error");
      fail("Expecting IllegalArgumentException");
    } catch (final IllegalArgumentException e) {
      assertEquals("Error", e.getMessage());
    }
    try {
      Assert.exclusiveBetween(0.1, 5.1, 5.1, "Error");
      fail("Expecting IllegalArgumentException");
    } catch (final IllegalArgumentException e) {
      assertEquals("Error", e.getMessage());
    }
  }

  @Test
  public void testIsInstanceOf_withMessage() {
    Assert.assertInstanceOf(String.class, "hi", "Error");
    Assert.assertInstanceOf(Integer.class, 1, "Error");
    try {
      Assert.assertInstanceOf(List.class, "hi", "Error");
      fail("Expecting IllegalArgumentException");
    } catch (final IllegalArgumentException e) {
      assertEquals("Error: java.lang.String", e.getMessage());
    }
  }

  @Test
  public void testIsInstanceOf_withMessageArgs() {
    Assert.assertInstanceOf(String.class, "hi", "Error %s=%s", "Name", "Value");
    Assert.assertInstanceOf(Integer.class, 1, "Error %s=%s", "Name", "Value");
    try {
      Assert.assertInstanceOf(List.class, "hi", "Error %s=%s", "Name", "Value");
      fail("Expecting IllegalArgumentException");
    } catch (final IllegalArgumentException e) {
      assertEquals("Error Name=Value", e.getMessage());
    }
    try {
      Assert.assertInstanceOf(List.class, "hi", "Error %s=%s", List.class, "Value");
      fail("Expecting IllegalArgumentException");
    } catch (final IllegalArgumentException e) {
      assertEquals("Error interface java.util.List=Value", e.getMessage());
    }
    try {
      Assert.assertInstanceOf(List.class, "hi", "Error %s=%s", List.class, null);
      fail("Expecting IllegalArgumentException");
    } catch (final IllegalArgumentException e) {
      assertEquals("Error interface java.util.List=null", e.getMessage());
    }
  }

  @Test
  public void testIsAssignable_withMessage() {
    Assert.assertAssignableFrom(CharSequence.class, String.class, "Error");
    Assert.assertAssignableFrom(AbstractList.class, ArrayList.class, "Error");
    try {
      Assert.assertAssignableFrom(List.class, String.class, "Error");
      fail("Expecting IllegalArgumentException");
    } catch (final IllegalArgumentException e) {
      assertEquals("Error", e.getMessage());
    }
  }

  @Test
  public void paramNotNull_NullParam_ThrowsException() {
    try {
      assertParamNotNull(null, "someField");
    } catch (NullPointerException e) {
      assertEquals(e.getMessage(), "someField must not be null.");
    }
  }

  @Test
  public void paramNotNull_NonNullParam_ReturnsObject() {
    assertEquals("foo", assertParamNotNull("foo", "someField"));
  }

  @Test
  public void getOrDefault_ParamNotNull_ReturnsParam() {
    assertEquals("foo", Assert.getOrDefault("foo", () -> "bar"));
  }

  @Test
  public void getOrDefault_ParamNull_ReturnsDefaultValue() {
    assertEquals("bar", Assert.getOrDefault(null, () -> "bar"));
  }

  @Test(expected = NullPointerException.class)
  public void getOrDefault_DefaultValueNull_ThrowsException() {
    Assert.getOrDefault("bar", null);
  }

  @Test
  public void mutuallyExclusive_AllNull_DoesNotThrow() {
    Assert.mutuallyExclusive("error", null, null, null);
  }

  @Test
  public void mutuallyExclusive_OnlyOneProvided_DoesNotThrow() {
    Assert.mutuallyExclusive("error", null, "foo", null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void mutuallyExclusive_MultipleProvided_DoesNotThrow() {
    Assert.mutuallyExclusive("error", null, "foo", "bar");
  }

  @Test
  public void isPositiveOrNullInteger_null_returnsNull() {
    assertNull(Assert.assertPositiveOrNull((Integer) null, "foo"));
  }

  @Test
  public void isPositiveOrNullInteger_positive_returnsInteger() {
    Integer num = 42;
    assertEquals(num, Assert.assertPositiveOrNull(num, "foo"));
  }

  @Test
  public void isPositiveOrNullInteger_zero_throws() {
    expected.expect(IllegalArgumentException.class);
    expected.expectMessage("foo");
    Assert.assertPositiveOrNull(0, "foo");
  }

  @Test
  public void isPositiveOrNullInteger_negative_throws() {
    expected.expect(IllegalArgumentException.class);
    expected.expectMessage("foo");
    Assert.assertPositiveOrNull(-1, "foo");
  }

  @Test
  public void isPositiveOrNullLong_null_returnsNull() {
    assertNull(Assert.assertPositiveOrNull((Long) null, "foo"));
  }

  @Test
  public void isPositiveOrNullLong_positive_returnsInteger() {
    Long num = 42L;
    assertEquals(num, Assert.assertPositiveOrNull(num, "foo"));
  }

  @Test
  public void isPositiveOrNullLong_zero_throws() {
    expected.expect(IllegalArgumentException.class);
    expected.expectMessage("foo");
    Assert.assertPositiveOrNull(0L, "foo");
  }

  @Test
  public void isPositiveOrNullLong_negative_throws() {
    expected.expect(IllegalArgumentException.class);
    expected.expectMessage("foo");
    Assert.assertPositiveOrNull(-1L, "foo");
  }

  @Test
  public void isPositiveOrNullDouble_null_returnsNull() {
    assertNull(Assert.assertPositiveOrNull((Double) null, "foo"));
  }

  @Test
  public void isPositiveOrNullDouble_positive_returnsInteger() {
    Double num = 100.0;
    assertEquals(num, Assert.assertPositiveOrNull(num, "foo"));
  }

  @Test
  public void isPositiveOrNullDouble_zero_throws() {
    expected.expect(IllegalArgumentException.class);
    expected.expectMessage("foo");
    Assert.assertPositiveOrNull(0.0, "foo");
  }

  @Test
  public void isPositiveOrNullDouble_negative_throws() {
    expected.expect(IllegalArgumentException.class);
    expected.expectMessage("foo");
    Assert.assertPositiveOrNull(-1.0, "foo");
  }

  @Test
  public void isNull_notNull_shouldThrow() {
    expected.expect(IllegalArgumentException.class);
    expected.expectMessage("not null");
    Assert.assertNull("string", "not null");
  }

  @Test
  public void isNull_null_shouldPass() {
    Assert.assertNull(null, "not null");
  }
}
