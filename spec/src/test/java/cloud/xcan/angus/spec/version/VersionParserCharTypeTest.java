package cloud.xcan.angus.spec.version;

import static cloud.xcan.angus.spec.version.VersionParser.CharType.DIGIT;
import static cloud.xcan.angus.spec.version.VersionParser.CharType.DOT;
import static cloud.xcan.angus.spec.version.VersionParser.CharType.EOI;
import static cloud.xcan.angus.spec.version.VersionParser.CharType.HYPHEN;
import static cloud.xcan.angus.spec.version.VersionParser.CharType.ILLEGAL;
import static cloud.xcan.angus.spec.version.VersionParser.CharType.LETTER;
import static cloud.xcan.angus.spec.version.VersionParser.CharType.PLUS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import cloud.xcan.angus.spec.version.VersionParser.CharType;
import org.junit.Test;

public class VersionParserCharTypeTest {

  @Test
  public void shouldBeMatchedByDigit() {
    assertTrue(DIGIT.isMatchedBy('0'));
    assertTrue(DIGIT.isMatchedBy('9'));
    assertFalse(DIGIT.isMatchedBy('a'));
    assertFalse(DIGIT.isMatchedBy('A'));
  }

  @Test
  public void shouldBeMatchedByLetter() {
    assertTrue(LETTER.isMatchedBy('a'));
    assertTrue(LETTER.isMatchedBy('A'));
    assertFalse(LETTER.isMatchedBy('0'));
    assertFalse(LETTER.isMatchedBy('9'));
  }

  @Test
  public void shouldBeMatchedByDot() {
    assertTrue(DOT.isMatchedBy('.'));
    assertFalse(DOT.isMatchedBy('-'));
    assertFalse(DOT.isMatchedBy('0'));
    assertFalse(DOT.isMatchedBy('9'));
  }

  @Test
  public void shouldBeMatchedByHyphen() {
    assertTrue(HYPHEN.isMatchedBy('-'));
    assertFalse(HYPHEN.isMatchedBy('+'));
    assertFalse(HYPHEN.isMatchedBy('a'));
    assertFalse(HYPHEN.isMatchedBy('0'));
  }

  @Test
  public void shouldBeMatchedByPlus() {
    assertTrue(PLUS.isMatchedBy('+'));
    assertFalse(PLUS.isMatchedBy('-'));
    assertFalse(PLUS.isMatchedBy('a'));
    assertFalse(PLUS.isMatchedBy('0'));
  }

  @Test
  public void shouldBeMatchedByEol() {
    assertTrue(EOI.isMatchedBy(null));
    assertFalse(EOI.isMatchedBy('-'));
    assertFalse(EOI.isMatchedBy('a'));
    assertFalse(EOI.isMatchedBy('0'));
  }

  @Test
  public void shouldBeMatchedByIllegal() {
    assertTrue(ILLEGAL.isMatchedBy('!'));
    assertFalse(ILLEGAL.isMatchedBy('-'));
    assertFalse(ILLEGAL.isMatchedBy('a'));
    assertFalse(ILLEGAL.isMatchedBy('0'));
  }

  @Test
  public void shouldReturnCharTypeForCharacter() {
    assertEquals(DIGIT, CharType.forCharacter('1'));
    assertEquals(LETTER, CharType.forCharacter('a'));
    assertEquals(DOT, CharType.forCharacter('.'));
    assertEquals(HYPHEN, CharType.forCharacter('-'));
    assertEquals(PLUS, CharType.forCharacter('+'));
    assertEquals(EOI, CharType.forCharacter(null));
    assertEquals(ILLEGAL, CharType.forCharacter('!'));
  }
}
