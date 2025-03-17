package cloud.xcan.sdf.spec.version;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import cloud.xcan.sdf.spec.version.TypeStream.ElementType;
import java.util.Iterator;
import org.junit.Test;

public class TypeStreamTest {

  @Test
  public void shouldBeBackedByArray() {
    Character[] input = {'a', 'b', 'c'};
    TypeStream<Character> TypeStream = new TypeStream<Character>(input);
    assertArrayEquals(input, TypeStream.toArray());
  }

  @Test
  public void shouldImplementIterable() {
    Character[] input = {'a', 'b', 'c'};
    TypeStream<Character> TypeStream = new TypeStream<Character>(input);
    Iterator<Character> it = TypeStream.iterator();
    for (Character chr : input) {
      assertEquals(chr, it.next());
    }
    assertFalse(it.hasNext());
  }

  @Test
  public void shouldNotReturnRealElementsArray() {
    TypeStream<Character> TypeStream = new TypeStream<Character>(
        new Character[]{'a', 'b', 'c'}
    );
    Character[] charArray = TypeStream.toArray();
    charArray[0] = Character.valueOf('z');
    assertEquals(Character.valueOf('z'), charArray[0]);
    assertEquals(Character.valueOf('a'), TypeStream.toArray()[0]);
  }

  @Test
  public void shouldReturnArrayOfElementsThatAreLeftInTypeStream() {
    TypeStream<Character> TypeStream = new TypeStream<Character>(
        new Character[]{'a', 'b', 'c'}
    );
    TypeStream.consume();
    TypeStream.consume();
    assertEquals(1, TypeStream.toArray().length);
    assertEquals(Character.valueOf('c'), TypeStream.toArray()[0]);
  }

  @Test
  public void shouldConsumeElementsOneByOne() {
    TypeStream<Character> TypeStream = new TypeStream<Character>(
        new Character[]{'a', 'b', 'c'}
    );
    assertEquals(Character.valueOf('a'), TypeStream.consume());
    assertEquals(Character.valueOf('b'), TypeStream.consume());
    assertEquals(Character.valueOf('c'), TypeStream.consume());
  }

  @Test
  public void shouldRaiseErrorWhenUnexpectedElementConsumed() {
    TypeStream<Character> TypeStream = new TypeStream<Character>(
        new Character[]{'a', 'b', 'c'}
    );
    try {
      TypeStream.consume(new ElementType<Character>() {
        @Override
        public boolean isMatchedBy(Character element) {
          return false;
        }
      });
    } catch (UnexpectedElementException e) {
      return;
    }
    fail("Should raise error when unexpected element type is consumed");
  }

  @Test
  public void shouldLookaheadWithoutConsuming() {
    TypeStream<Character> TypeStream = new TypeStream<Character>(
        new Character[]{'a', 'b', 'c'}
    );
    assertEquals(Character.valueOf('a'), TypeStream.lookahead());
    assertEquals(Character.valueOf('a'), TypeStream.lookahead());
  }

  @Test
  public void shouldLookaheadArbitraryNumberOfElements() {
    TypeStream<Character> TypeStream = new TypeStream<Character>(
        new Character[]{'a', 'b', 'c'}
    );
    assertEquals(Character.valueOf('a'), TypeStream.lookahead(1));
    assertEquals(Character.valueOf('b'), TypeStream.lookahead(2));
    assertEquals(Character.valueOf('c'), TypeStream.lookahead(3));
  }

  @Test
  public void shouldCheckIfLookaheadIsOfExpectedTypes() {
    TypeStream<Character> TypeStream = new TypeStream<Character>(
        new Character[]{'a', 'b', 'c'}
    );
    assertTrue(TypeStream.positiveLookahead(
        new ElementType<Character>() {
          @Override
          public boolean isMatchedBy(Character element) {
            return element == 'a';
          }
        }
    ));
    assertFalse(TypeStream.positiveLookahead(
        new ElementType<Character>() {
          @Override
          public boolean isMatchedBy(Character element) {
            return element == 'c';
          }
        }
    ));
  }

  @Test
  public void shouldCheckIfElementOfExpectedTypesExistBeforeGivenType() {
    TypeStream<Character> TypeStream = new TypeStream<Character>(
        new Character[]{'1', '.', '0', '.', '0'}
    );
    assertTrue(TypeStream.positiveLookaheadBefore(
        new ElementType<Character>() {
          @Override
          public boolean isMatchedBy(Character element) {
            return element == '.';
          }
        },
        new ElementType<Character>() {
          @Override
          public boolean isMatchedBy(Character element) {
            return element == '1';
          }
        }
    ));
    assertFalse(TypeStream.positiveLookaheadBefore(
        new ElementType<Character>() {
          @Override
          public boolean isMatchedBy(Character element) {
            return element == '1';
          }
        },
        new ElementType<Character>() {
          @Override
          public boolean isMatchedBy(Character element) {
            return element == '.';
          }
        }
    ));
  }

  @Test
  public void shouldCheckIfElementOfExpectedTypesExistUntilGivenPosition() {
    TypeStream<Character> TypeStream = new TypeStream<Character>(
        new Character[]{'1', '.', '0', '.', '0'}
    );
    assertTrue(TypeStream.positiveLookaheadUntil(
        3,
        new ElementType<Character>() {
          @Override
          public boolean isMatchedBy(Character element) {
            return element == '0';
          }
        }
    ));
    assertFalse(TypeStream.positiveLookaheadUntil(
        3,
        new ElementType<Character>() {
          @Override
          public boolean isMatchedBy(Character element) {
            return element == 'a';
          }
        }
    ));
  }

  @Test
  public void shouldPushBackOneElementAtATime() {
    TypeStream<Character> TypeStream = new TypeStream<Character>(
        new Character[]{'a', 'b', 'c'}
    );
    assertEquals(Character.valueOf('a'), TypeStream.consume());
    TypeStream.pushBack();
    assertEquals(Character.valueOf('a'), TypeStream.consume());
  }

  @Test
  public void shouldStopPushingBackWhenThereAreNoElements() {
    TypeStream<Character> TypeStream = new TypeStream<Character>(
        new Character[]{'a', 'b', 'c'}
    );
    assertEquals(Character.valueOf('a'), TypeStream.consume());
    assertEquals(Character.valueOf('b'), TypeStream.consume());
    assertEquals(Character.valueOf('c'), TypeStream.consume());
    TypeStream.pushBack();
    TypeStream.pushBack();
    TypeStream.pushBack();
    TypeStream.pushBack();
    assertEquals(Character.valueOf('a'), TypeStream.consume());
  }

  @Test
  public void shouldKeepTrackOfCurrentOffset() {
    TypeStream<Character> TypeStream = new TypeStream<Character>(
        new Character[]{'a', 'b', 'c'}
    );
    assertEquals(0, TypeStream.currentOffset());
    TypeStream.consume();
    assertEquals(1, TypeStream.currentOffset());
    TypeStream.consume();
    TypeStream.consume();
    assertEquals(3, TypeStream.currentOffset());
  }
}
