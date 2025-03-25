package cloud.xcan.angus.spec.version.expr;

import static cloud.xcan.angus.spec.version.expr.Lexer.Token.Type.AND;
import static cloud.xcan.angus.spec.version.expr.Lexer.Token.Type.DOT;
import static cloud.xcan.angus.spec.version.expr.Lexer.Token.Type.EQUAL;
import static cloud.xcan.angus.spec.version.expr.Lexer.Token.Type.GREATER;
import static cloud.xcan.angus.spec.version.expr.Lexer.Token.Type.HYPHEN;
import static cloud.xcan.angus.spec.version.expr.Lexer.Token.Type.NOT_EQUAL;
import static cloud.xcan.angus.spec.version.expr.Lexer.Token.Type.NUMERIC;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import cloud.xcan.angus.spec.version.expr.Lexer.Token;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class LexerTokenTest {

  public static class EqualsHttpMethodTest {

    @Test
    public void shouldBeReflexive() {
      Token token = new Token(NUMERIC, "1", 0);
      assertEquals(token, token);
    }

    @Test
    public void shouldBeSymmetric() {
      Token t1 = new Token(EQUAL, "=", 0);
      Token t2 = new Token(EQUAL, "=", 0);
      assertEquals(t1, t2);
      assertEquals(t2, t1);
    }

    @Test
    public void shouldBeTransitive() {
      Token t1 = new Token(GREATER, ">", 0);
      Token t2 = new Token(GREATER, ">", 0);
      Token t3 = new Token(GREATER, ">", 0);
      assertEquals(t1, t2);
      assertEquals(t2, t3);
      assertEquals(t1, t3);
    }

    @Test
    public void shouldBeConsistent() {
      Token t1 = new Token(HYPHEN, "-", 0);
      Token t2 = new Token(HYPHEN, "-", 0);
      assertEquals(t1, t2);
      assertEquals(t1, t2);
      assertEquals(t1, t2);
    }

    @Test
    public void shouldReturnFalseIfOtherVersionIsOfDifferentType() {
      Token t1 = new Token(DOT, ".", 0);
      assertFalse(t1.equals(new String(".")));
    }

    @Test
    public void shouldReturnFalseIfOtherVersionIsNull() {
      Token t1 = new Token(AND, "&", 0);
      Token t2 = null;
      assertFalse(t1.equals(t2));
    }

    @Test
    public void shouldReturnFalseIfTypesAreDifferent() {
      Token t1 = new Token(EQUAL, "=", 0);
      Token t2 = new Token(NOT_EQUAL, "!=", 0);
      assertFalse(t1.equals(t2));
    }

    @Test
    public void shouldReturnFalseIfLexemesAreDifferent() {
      Token t1 = new Token(NUMERIC, "1", 0);
      Token t2 = new Token(NUMERIC, "2", 0);
      assertFalse(t1.equals(t2));
    }

    @Test
    public void shouldReturnFalseIfPositionsAreDifferent() {
      Token t1 = new Token(NUMERIC, "1", 1);
      Token t2 = new Token(NUMERIC, "1", 2);
      assertFalse(t1.equals(t2));
    }
  }

  public static class HashCodeMethodTest {

    @Test
    public void shouldReturnSameHashCodeIfTokensAreEqual() {
      Token t1 = new Token(NUMERIC, "1", 0);
      Token t2 = new Token(NUMERIC, "1", 0);
      assertTrue(t1.equals(t2));
      assertEquals(t1.hashCode(), t2.hashCode());
    }
  }
}
