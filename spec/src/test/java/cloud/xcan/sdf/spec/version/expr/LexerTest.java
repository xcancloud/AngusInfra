package cloud.xcan.sdf.spec.version.expr;

import static cloud.xcan.sdf.spec.version.expr.Lexer.Token.Type.DOT;
import static cloud.xcan.sdf.spec.version.expr.Lexer.Token.Type.EOI;
import static cloud.xcan.sdf.spec.version.expr.Lexer.Token.Type.GREATER;
import static cloud.xcan.sdf.spec.version.expr.Lexer.Token.Type.NUMERIC;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import cloud.xcan.sdf.spec.version.TypeStream;
import cloud.xcan.sdf.spec.version.expr.Lexer.Token;
import org.junit.Test;

public class LexerTest {

  @Test
  public void shouldTokenizeVersionString() {
    Token[] expected = {
        new Token(GREATER, ">", 0),
        new Token(NUMERIC, "1", 1),
        new Token(DOT, ".", 2),
        new Token(NUMERIC, "0", 3),
        new Token(DOT, ".", 4),
        new Token(NUMERIC, "0", 5),
        new Token(EOI, null, 6),
    };
    Lexer lexer = new Lexer();
    TypeStream<Token> TypeStream = lexer.tokenize(">1.0.0");
    assertArrayEquals(expected, TypeStream.toArray());
  }

  @Test
  public void shouldSkipWhitespaces() {
    Token[] expected = {
        new Token(GREATER, ">", 0),
        new Token(NUMERIC, "1", 2),
        new Token(EOI, null, 3),
    };
    Lexer lexer = new Lexer();
    TypeStream<Token> TypeStream = lexer.tokenize("> 1");
    assertArrayEquals(expected, TypeStream.toArray());
  }

  @Test
  public void shouldEndWithEol() {
    Token[] expected = {
        new Token(NUMERIC, "1", 0),
        new Token(DOT, ".", 1),
        new Token(NUMERIC, "2", 2),
        new Token(DOT, ".", 3),
        new Token(NUMERIC, "3", 4),
        new Token(EOI, null, 5),
    };
    Lexer lexer = new Lexer();
    TypeStream<Token> TypeStream = lexer.tokenize("1.2.3");
    assertArrayEquals(expected, TypeStream.toArray());
  }

  @Test
  public void shouldRaiseErrorOnIllegalCharacter() {
    Lexer lexer = new Lexer();
    try {
      lexer.tokenize("@1.0.0");
    } catch (LexerException e) {
      return;
    }
    fail("Should raise error on illegal character");
  }
}
