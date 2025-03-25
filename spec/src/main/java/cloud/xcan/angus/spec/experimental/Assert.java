package cloud.xcan.angus.spec.experimental;

import static org.apache.commons.lang3.StringUtils.isBlank;

import cloud.xcan.angus.spec.utils.ObjectUtils;
import cloud.xcan.angus.spec.utils.StringUtils;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.apache.commons.lang3.Validate;

/**
 * Assertion utility class that assists in validating arguments.
 *
 * <p>Useful for identifying programmer errors early and clearly at runtime.
 *
 * <p>For example, if the contract of a public method states it does not
 * allow {@code null} arguments, {@code Assert} can be used to validate that contract. Doing this
 * clearly indicates a contract violation when it occurs and protects the class's invariants.
 *
 * <p>Typically used to validate method arguments rather than configuration
 * properties, to check for cases that are usually programmer errors rather than configuration
 * errors. In contrast to configuration initialization code, there is usually no point in falling
 * back to defaults in such methods.
 *
 * <p>This class is similar to JUnit's assertion library. If an argument value is
 * deemed invalid, an {@link IllegalArgumentException} is thrown (typically). For example:
 *
 * <pre class="code">
 * assertNotNull(clazz, "The class must not be null");
 * assertTrue(i > 0, "The value must be greater than zero");</pre>
 *
 * <p>Mainly for internal use within the framework; for a more comprehensive suite
 * of assertion utilities consider {@code org.apache.commons.lang3.Validate} from
 * <a href="https://commons.apache.org/proper/commons-lang/">Apache Commons Lang</a>,
 * Google Guava's
 * <a href="https://github.com/google/guava/wiki/PreconditionsExplained">Preconditions</a>,
 * or similar third-party libraries.
 */
public class Assert {

  /**
   * Assert a boolean expression, throwing an {@code IllegalStateException} if the expression
   * evaluates to {@code false}.
   * <p>Call {@link #assertTrue} if you wish to throw an {@code IllegalArgumentException}
   * on an assertion failure.
   * <pre class="code">Assert.state(id == null, "The id property must not already be
   * initialized");</pre>
   *
   * @param expression a boolean expression
   * @param message    the exception message to use if the assertion fails
   * @throws IllegalStateException if {@code expression} is {@code false}
   */
  public static void assertState(boolean expression, String message) {
    if (!expression) {
      throw new IllegalStateException(message);
    }
  }

  /**
   * Assert a boolean expression, throwing an {@code IllegalStateException} if the expression
   * evaluates to {@code false}.
   * <p>Call {@link #assertTrue} if you wish to throw an {@code IllegalArgumentException}
   * on an assertion failure.
   * <pre class="code">
   * Assert.state(entity.getId() == null,
   *     () -&gt; "ID for entity " + entity.getName() + " must not already be initialized");
   * </pre>
   *
   * @param expression      a boolean expression
   * @param messageSupplier a supplier for the exception message to use if the assertion fails
   * @throws IllegalStateException if {@code expression} is {@code false}
   */
  public static void assertState(boolean expression, Supplier<String> messageSupplier) {
    if (!expression) {
      throw new IllegalStateException(nullSafeGet(messageSupplier));
    }
  }

  /**
   * Assert a boolean expression, throwing an {@code IllegalArgumentException} if the expression
   * evaluates to {@code false}.
   * <pre class="code">Assert.assertTrue(i &gt; 0, "The value must be greater than zero");</pre>
   *
   * @param expression a boolean expression
   * @param message    the exception message to use if the assertion fails
   * @throws IllegalArgumentException if {@code expression} is {@code false}
   */
  public static void assertTrue(boolean expression, String message) {
    if (!expression) {
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * Assert a boolean expression, throwing an {@code IllegalArgumentException} if the expression
   * evaluates to {@code false}.
   * <pre class="code">
   * assertTrue(i &gt; 0, () -&gt; "The value '" + i + "' must be greater than zero");
   * </pre>
   *
   * @param expression      a boolean expression
   * @param messageSupplier a supplier for the exception message to use if the assertion fails
   * @throws IllegalArgumentException if {@code expression} is {@code false}
   */
  public static void assertTrue(boolean expression, Supplier<String> messageSupplier) {
    if (!expression) {
      throw new IllegalArgumentException(nullSafeGet(messageSupplier));
    }
  }

  /**
   * Assert that an object is {@code null}.
   * <pre class="code">Assert.isNull(value, "The value must be null");</pre>
   *
   * @param object  the object to check
   * @param message the exception message to use if the assertion fails
   * @throws IllegalArgumentException if the object is not {@code null}
   */
  public static <T> T assertNull(T object, String message) {
    if (object != null) {
      throw new IllegalArgumentException(message);
    }
    return object;
  }

  /**
   * Assert that an object is {@code null}.
   * <pre class="code">
   * Assert.isNull(value, () -&gt; "The value '" + value + "' must be null");
   * </pre>
   *
   * @param object          the object to check
   * @param messageSupplier a supplier for the exception message to use if the assertion fails
   * @throws IllegalArgumentException if the object is not {@code null}
   */
  public static <T> T assertNull(T object, Supplier<String> messageSupplier) {
    if (object != null) {
      throw new IllegalArgumentException(nullSafeGet(messageSupplier));
    }
    return object;
  }

  /**
   * Assert that an object is not {@code null}.
   * <pre class="code">Assert.assertNotNull(clazz, "The class must not be null");</pre>
   *
   * @param object  the object to check
   * @param message the exception message to use if the assertion fails
   * @throws IllegalArgumentException if the object is {@code null}
   */
  public static <T> T assertNotNull(T object, String message) {
    if (object == null) {
      throw new IllegalArgumentException(message);
    }
    return object;
  }

  /**
   * Assert that an object is not {@code null}.
   * <pre class="code">
   * assertNotNull(entity.getId(),
   *     () -&gt; "ID for entity " + entity.getName() + " must not be null");
   * </pre>
   *
   * @param object          the object to check
   * @param messageSupplier a supplier for the exception message to use if the assertion fails
   * @throws IllegalArgumentException if the object is {@code null}
   */
  public static <T> T assertNotNull(T object, Supplier<String> messageSupplier) {
    if (object == null) {
      throw new IllegalArgumentException(nullSafeGet(messageSupplier));
    }
    return object;
  }

  /**
   * Assert that the given String is not empty; that is, it must not be {@code null} and not the
   * empty String.
   * <pre class="code">Assert.hasLength(name, "Name must not be empty");</pre>
   *
   * @param text    the String to check
   * @param message the exception message to use if the assertion fails
   * @throws IllegalArgumentException if the text is empty
   * @see StringUtils#hasLength
   */
  public static String assertHasLength(String text, String message) {
    if (!StringUtils.hasLength(text)) {
      throw new IllegalArgumentException(message);
    }
    return text;
  }

  /**
   * Assert that the given String is not empty; that is, it must not be {@code null} and not the
   * empty String.
   * <pre class="code">
   * Assert.hasLength(account.getName(),
   *     () -&gt; "Name for account '" + account.getId() + "' must not be empty");
   * </pre>
   *
   * @param text            the String to check
   * @param messageSupplier a supplier for the exception message to use if the assertion fails
   * @throws IllegalArgumentException if the text is empty
   * @see StringUtils#hasLength
   */
  public static String assertHasLength(String text, Supplier<String> messageSupplier) {
    if (!StringUtils.hasLength(text)) {
      throw new IllegalArgumentException(nullSafeGet(messageSupplier));
    }
    return text;
  }

  /**
   * Assert that the given String contains valid text content; that is, it must not be {@code null}
   * and must contain at least one non-whitespace character.
   * <pre class="code">Assert.hasText(name, "'name' must not be empty");</pre>
   *
   * @param text    the String to check
   * @param message the exception message to use if the assertion fails
   * @throws IllegalArgumentException if the text does not contain valid text content
   * @see StringUtils#hasText
   */
  public static String assertHasText(String text, String message) {
    if (!StringUtils.hasText(text)) {
      throw new IllegalArgumentException(message);
    }
    return text;
  }

  /**
   * Assert that the given String contains valid text content; that is, it must not be {@code null}
   * and must contain at least one non-whitespace character.
   * <pre class="code">
   * Assert.hasText(account.getName(),
   *     () -&gt; "Name for account '" + account.getId() + "' must not be empty");
   * </pre>
   *
   * @param text            the String to check
   * @param messageSupplier a supplier for the exception message to use if the assertion fails
   * @throws IllegalArgumentException if the text does not contain valid text content
   * @see StringUtils#hasText
   */
  public static String assertHasText(String text, Supplier<String> messageSupplier) {
    if (!StringUtils.hasText(text)) {
      throw new IllegalArgumentException(nullSafeGet(messageSupplier));
    }
    return text;
  }

  /**
   * Assert that the given text does not contain the given substring.
   * <pre class="code">Assert.doesNotContain(name, "rod", "Name must not contain 'rod'");</pre>
   *
   * @param textToSearch the text to search
   * @param substring    the substring to find within the text
   * @param message      the exception message to use if the assertion fails
   * @throws IllegalArgumentException if the text contains the substring
   */
  public static String assertNotContain(String textToSearch, String substring,
      String message) {
    if (StringUtils.hasLength(textToSearch) && StringUtils.hasLength(substring) &&
        textToSearch.contains(substring)) {
      throw new IllegalArgumentException(message);
    }
    return textToSearch;
  }

  /**
   * Assert that the given text does not contain the given substring.
   * <pre class="code">
   * Assert.doesNotContain(name, forbidden, () -&gt; "Name must not contain '" + forbidden + "'");
   * </pre>
   *
   * @param textToSearch    the text to search
   * @param substring       the substring to find within the text
   * @param messageSupplier a supplier for the exception message to use if the assertion fails
   * @throws IllegalArgumentException if the text contains the substring
   */
  public static String assertNotContain(String textToSearch, String substring,
      Supplier<String> messageSupplier) {
    if (StringUtils.hasLength(textToSearch) && StringUtils.hasLength(substring) &&
        textToSearch.contains(substring)) {
      throw new IllegalArgumentException(nullSafeGet(messageSupplier));
    }
    return textToSearch;
  }

  /**
   * Assert that an array contains elements; that is, it must not be {@code null} and must contain
   * at least one element.
   * <pre class="code">Assert.notEmpty(array, "The array must contain elements");</pre>
   *
   * @param array   the array to check
   * @param message the exception message to use if the assertion fails
   * @throws IllegalArgumentException if the object array is {@code null} or contains no elements
   */
  public static Object[] assertNotEmpty(Object[] array, String message) {
    if (ObjectUtils.isEmpty(array)) {
      throw new IllegalArgumentException(message);
    }
    return array;
  }

  /**
   * Assert that an array contains elements; that is, it must not be {@code null} and must contain
   * at least one element.
   * <pre class="code">
   * Assert.notEmpty(array, () -&gt; "The " + arrayType + " array must contain elements");
   * </pre>
   *
   * @param array           the array to check
   * @param messageSupplier a supplier for the exception message to use if the assertion fails
   * @throws IllegalArgumentException if the object array is {@code null} or contains no elements
   */
  public static Object[] assertNotEmpty(Object[] array, Supplier<String> messageSupplier) {
    if (ObjectUtils.isEmpty(array)) {
      throw new IllegalArgumentException(nullSafeGet(messageSupplier));
    }
    return array;
  }

  /**
   * Assert that an array contains no {@code null} elements.
   * <p>Note: Does not complain if the array is empty!
   * <pre class="code">Assert.noNullElements(array, "The array must contain non-null
   * elements");</pre>
   *
   * @param array   the array to check
   * @param message the exception message to use if the assertion fails
   * @throws IllegalArgumentException if the object array contains a {@code null} element
   */
  public static Object[] assertNoNullElements(Object[] array, String message) {
    if (array != null) {
      for (Object element : array) {
        if (element == null) {
          throw new IllegalArgumentException(message);
        }
      }
    }
    return array;
  }

  /**
   * Assert that an array contains no {@code null} elements.
   * <p>Note: Does not complain if the array is empty!
   * <pre class="code">
   * Assert.noNullElements(array, () -&gt; "The " + arrayType + " array must contain non-null elements");
   * </pre>
   *
   * @param array           the array to check
   * @param messageSupplier a supplier for the exception message to use if the assertion fails
   * @throws IllegalArgumentException if the object array contains a {@code null} element
   */
  public static Object[] assertNoNullElements(Object[] array, Supplier<String> messageSupplier) {
    if (array != null) {
      for (Object element : array) {
        if (element == null) {
          throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
      }
    }
    return array;
  }

  /**
   * Assert that a collection contains elements; that is, it must not be {@code null} and must
   * contain at least one element.
   * <pre class="code">Assert.notEmpty(collection, "Collection must contain elements");</pre>
   *
   * @param collection the collection to check
   * @param message    the exception message to use if the assertion fails
   * @throws IllegalArgumentException if the collection is {@code null} or contains no elements
   */
  public static Collection<?> assertNotEmpty(Collection<?> collection, String message) {
    if (ObjectUtils.isEmpty(collection)) {
      throw new IllegalArgumentException(message);
    }
    return collection;
  }

  /**
   * Assert that a collection contains elements; that is, it must not be {@code null} and must
   * contain at least one element.
   * <pre class="code">
   * Assert.notEmpty(collection, () -&gt; "The " + collectionType + " collection must contain elements");
   * </pre>
   *
   * @param collection      the collection to check
   * @param messageSupplier a supplier for the exception message to use if the assertion fails
   * @throws IllegalArgumentException if the collection is {@code null} or contains no elements
   */
  public static Collection<?> assertNotEmpty(Collection<?> collection,
      Supplier<String> messageSupplier) {
    if (ObjectUtils.isEmpty(collection)) {
      throw new IllegalArgumentException(nullSafeGet(messageSupplier));
    }
    return collection;
  }

  /**
   * Assert that a collection contains no {@code null} elements.
   * <p>Note: Does not complain if the collection is empty!
   * <pre class="code">Assert.noNullElements(collection, "Collection must contain non-null
   * elements");</pre>
   *
   * @param collection the collection to check
   * @param message    the exception message to use if the assertion fails
   * @throws IllegalArgumentException if the collection contains a {@code null} element
   */
  public static Collection<?> assertNoNullElements(Collection<?> collection, String message) {
    if (collection != null) {
      for (Object element : collection) {
        if (element == null) {
          throw new IllegalArgumentException(message);
        }
      }
    }
    return collection;
  }

  /**
   * Assert that a collection contains no {@code null} elements.
   * <p>Note: Does not complain if the collection is empty!
   * <pre class="code">
   * Assert.noNullElements(collection, () -&gt; "Collection " + collectionName + " must contain non-null elements");
   * </pre>
   *
   * @param collection      the collection to check
   * @param messageSupplier a supplier for the exception message to use if the assertion fails
   * @throws IllegalArgumentException if the collection contains a {@code null} element
   */
  public static Collection<?> assertNoNullElements(Collection<?> collection,
      Supplier<String> messageSupplier) {
    if (collection != null) {
      for (Object element : collection) {
        if (element == null) {
          throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
      }
    }
    return collection;
  }

  /**
   * Assert that a Map contains entries; that is, it must not be {@code null} and must contain at
   * least one entry.
   * <pre class="code">Assert.notEmpty(map, "Map must contain entries");</pre>
   *
   * @param map     the map to check
   * @param message the exception message to use if the assertion fails
   * @throws IllegalArgumentException if the map is {@code null} or contains no entries
   */
  public static Map<?, ?> assertNotEmpty(Map<?, ?> map, String message) {
    if (ObjectUtils.isEmpty(map)) {
      throw new IllegalArgumentException(message);
    }
    return map;
  }

  /**
   * Assert that a Map contains entries; that is, it must not be {@code null} and must contain at
   * least one entry.
   * <pre class="code">
   * Assert.notEmpty(map, () -&gt; "The " + mapType + " map must contain entries");
   * </pre>
   *
   * @param map             the map to check
   * @param messageSupplier a supplier for the exception message to use if the assertion fails
   * @throws IllegalArgumentException if the map is {@code null} or contains no entries
   */
  public static Map<?, ?> assertNotEmpty(Map<?, ?> map, Supplier<String> messageSupplier) {
    if (ObjectUtils.isEmpty(map)) {
      throw new IllegalArgumentException(nullSafeGet(messageSupplier));
    }
    return map;
  }

  /**
   * Assert that the provided object is an instance of the provided class.
   * <pre class="code">Assert.instanceOf(Foo.class, foo, "Foo expected");</pre>
   *
   * @param type    the type to check against
   * @param obj     the object to check
   * @param message a message which will be prepended to provide further context. If it is empty or
   *                ends in ":" or ";" or "," or ".", a full exception message will be appended. If
   *                it ends in a space, the name of the offending object's type will be appended. In
   *                any other case, a ":" with a space and the name of the offending object's type
   *                will be appended.
   * @throws IllegalArgumentException if the object is not an instance of type
   */
  public static Class<?> assertInstanceOf(Class<?> type, Object obj, String message) {
    assertNotNull(type, "Type to check against must not be null");
    if (!type.isInstance(obj)) {
      instanceCheckFailed(type, obj, message);
    }
    return type;
  }

  /**
   * Assert that the provided object is an instance of the provided class.
   * <pre class="code">
   * Assert.instanceOf(Foo.class, foo, () -&gt; "Processing " + Foo.class.getSimpleName() + ":");
   * </pre>
   *
   * @param type            the type to check against
   * @param obj             the object to check
   * @param messageSupplier a supplier for the exception message to use if the assertion fails. See
   *                        {@link #assertInstanceOf(Class, Object, String)} for details.
   * @throws IllegalArgumentException if the object is not an instance of type
   */
  public static Class<?> assertInstanceOf(Class<?> type, Object obj,
      Supplier<String> messageSupplier) {
    assertNull(type, "Type to check against must not be null");
    if (!type.isInstance(obj)) {
      instanceCheckFailed(type, obj, nullSafeGet(messageSupplier));
    }
    return type;
  }

  /**
   * Assert that the provided object is an instance of the provided class.
   * <pre class="code">Assert.instanceOf(Foo.class, foo);</pre>
   *
   * @param type the type to check against
   * @param obj  the object to check
   * @throws IllegalArgumentException if the object is not an instance of type
   */
  public static Class<?> assertInstanceOf(Class<?> type, Object obj) {
    assertInstanceOf(type, obj, "");
    return type;
  }

  /**
   * Assert that {@code superType.isAssignableFrom(subType)} is {@code true}.
   * <pre class="code">Assert.isAssignable(Number.class, myClass, "Number expected");</pre>
   *
   * @param superType the super type to check against
   * @param subType   the sub type to check
   * @param message   a message which will be prepended to provide further context. If it is empty
   *                  or ends in ":" or ";" or "," or ".", a full exception message will be
   *                  appended. If it ends in a space, the name of the offending sub type will be
   *                  appended. In any other case, a ":" with a space and the name of the offending
   *                  sub type will be appended.
   * @throws IllegalArgumentException if the classes are not assignable
   */
  public static Class<?> assertAssignable(Class<?> superType, Class<?> subType, String message) {
    assertNull(superType, "Super type to check against must not be null");
    if (subType == null || !superType.isAssignableFrom(subType)) {
      assignableCheckFailed(superType, subType, message);
    }
    return superType;
  }

  /**
   * Assert that {@code superType.isAssignableFrom(subType)} is {@code true}.
   * <pre class="code">
   * Assert.isAssignable(Number.class, myClass, () -&gt; "Processing " + myAttributeName + ":");
   * </pre>
   *
   * @param superType       the super type to check against
   * @param subType         the sub type to check
   * @param messageSupplier a supplier for the exception message to use if the assertion fails. See
   *                        {@link #assertAssignable(Class, Class, String)} for details.
   * @throws IllegalArgumentException if the classes are not assignable
   */
  public static Class<?> assertAssignable(Class<?> superType, Class<?> subType,
      Supplier<String> messageSupplier) {
    assertNull(superType, "Super type to check against must not be null");
    if (subType == null || !superType.isAssignableFrom(subType)) {
      assignableCheckFailed(superType, subType, nullSafeGet(messageSupplier));
    }
    return superType;
  }

  /**
   * Assert that {@code superType.isAssignableFrom(subType)} is {@code true}.
   * <pre class="code">Assert.isAssignable(Number.class, myClass);</pre>
   *
   * @param superType the super type to check
   * @param subType   the sub type to check
   * @throws IllegalArgumentException if the classes are not assignable
   */
  public static Class<?> assertAssignable(Class<?> superType, Class<?> subType) {
    assertAssignable(superType, subType, "");
    return superType;
  }

  private static void instanceCheckFailed(Class<?> type, Object obj,
      String msg) {
    String className = (obj != null ? obj.getClass().getName() : "null");
    String result = "";
    boolean defaultMessage = true;
    if (StringUtils.hasLength(msg)) {
      if (endsWithSeparator(msg)) {
        result = msg + " ";
      } else {
        result = messageWithTypeName(msg, className);
        defaultMessage = false;
      }
    }
    if (defaultMessage) {
      result = result + ("Object of class [" + className + "] must be an instance of " + type);
    }
    throw new IllegalArgumentException(result);
  }

  private static void assignableCheckFailed(Class<?> superType, Class<?> subType,
      String msg) {
    String result = "";
    boolean defaultMessage = true;
    if (StringUtils.hasLength(msg)) {
      if (endsWithSeparator(msg)) {
        result = msg + " ";
      } else {
        result = messageWithTypeName(msg, subType);
        defaultMessage = false;
      }
    }
    if (defaultMessage) {
      result = result + (subType + " is not assignable to " + superType);
    }
    throw new IllegalArgumentException(result);
  }

  private static boolean endsWithSeparator(String msg) {
    return (msg.endsWith(":") || msg.endsWith(";") || msg.endsWith(",") || msg.endsWith("."));
  }

  private static String messageWithTypeName(String msg, Object typeName) {
    return msg + (msg.endsWith(" ") ? "" : ": ") + typeName;
  }

  private static String nullSafeGet(Supplier<String> messageSupplier) {
    return (messageSupplier != null ? messageSupplier.get() : null);
  }

  // isTrue
  //---------------------------------------------------------------------------------

  /**
   * <p>Validate that the argument condition is {@code true}; otherwise
   * throwing an exception with the specified message. This method is useful when validating
   * according to an arbitrary boolean expression, such as validating a primitive number or using
   * your own custom validation expression.</p>
   *
   * <pre>
   * Validate.isTrue(i &gt;= min &amp;&amp; i &lt;= max, "The value must be between &#37;d and &#37;d", min, max);
   * Validate.isTrue(myObject.isOk(), "The object is not okay");</pre>
   *
   * @param expression the boolean expression to check
   * @param message    the {@link String#format(String, Object...)} exception message if invalid,
   *                   not null
   * @param values     the optional values for the formatted exception message, null array not
   *                   recommended
   * @throws IllegalArgumentException if expression is {@code false}
   */
  public static void assertTrue(final boolean expression, final String message,
      final Object... values) {
    if (!expression) {
      throw new IllegalArgumentException(String.format(message, values));
    }
  }

  // isFalse
  //---------------------------------------------------------------------------------

  /**
   * <p>Validate that the argument condition is {@code false}; otherwise
   * throwing an exception with the specified message. This method is useful when validating
   * according to an arbitrary boolean expression, such as validating a primitive number or using
   * your own custom validation expression.</p>
   *
   * <pre>
   * Validate.isFalse(myObject.permitsSomething(), "The object is not allowed to permit something");</pre>
   *
   * @param expression the boolean expression to check
   * @param message    the {@link String#format(String, Object...)} exception message if not false,
   *                   not null
   * @param values     the optional values for the formatted exception message, null array not
   *                   recommended
   * @throws IllegalArgumentException if expression is {@code true}
   */
  public static void assertFalse(final boolean expression, final String message,
      final Object... values) {
    assertTrue(!expression, message, values);
  }

  // notNull
  //---------------------------------------------------------------------------------

  /**
   * <p>Validate that the specified argument is not {@code null};
   * otherwise throwing an exception with the specified message.
   *
   * <pre>Validate.notNull(myObject, "The object must not be null");</pre>
   *
   * @param <T>     the object type
   * @param object  the object to check
   * @param message the {@link String#format(String, Object...)} exception message if invalid, not
   *                null
   * @param values  the optional values for the formatted exception message
   * @return the validated object (never {@code null} for method chaining)
   * @throws NullPointerException if the object is {@code null}
   */
  public static <T> T assertNotNull(final T object, final String message, final Object... values) {
    if (object == null) {
      throw new NullPointerException(String.format(message, values));
    }
    return object;
  }

  /**
   * <p>Validate that the specified argument is {@code null};
   * otherwise throwing an exception with the specified message.
   *
   * <pre>Validate.isNull(myObject, "The object must be null");</pre>
   *
   * @param <T>     the object type
   * @param object  the object to check
   * @param message the {@link String#format(String, Object...)} exception message if invalid, not
   *                null
   * @param values  the optional values for the formatted exception message
   * @throws IllegalArgumentException if the object is not {@code null}
   */
  public static <T> void assertNull(final T object, final String message, final Object... values) {
    if (object != null) {
      throw new IllegalArgumentException(String.format(message, values));
    }
  }

  /**
   * <p>Validate that the specified field/param is not {@code null};
   * otherwise throwing an exception with a precanned message that includes the parameter name.
   *
   * <pre>Validate.paramNotNull(myObject, "myObject");</pre>
   *
   * @param <T>       the object type
   * @param object    the object to check
   * @param paramName The name of the param or field being checked.
   * @return the validated object (never {@code null} for method chaining)
   * @throws NullPointerException if the object is {@code null}
   */
  public static <T> T assertParamNotNull(final T object, final String paramName) {
    if (object == null) {
      throw new NullPointerException(String.format("%s must not be null.", paramName));
    }
    return object;
  }

  /**
   * <p>Validate that the specified char sequence is neither
   * {@code null}, a length of zero (no characters), empty nor whitespace; otherwise throwing an
   * exception with the specified message.
   *
   * <pre>Validate.paramNotBlank(myCharSequence, "myCharSequence");</pre>
   *
   * @param <T>       the char sequence type
   * @param chars     the character sequence to check
   * @param paramName The name of the param or field being checked.
   * @return the validated char sequence (never {@code null} for method chaining)
   * @throws NullPointerException if the char sequence is {@code null}
   */
  public static <T extends CharSequence> T assertParamNotBlank(final T chars,
      final String paramName) {
    if (chars == null) {
      throw new NullPointerException(String.format("%s must not be null.", paramName));
    }
    if (isBlank(chars)) {
      throw new IllegalArgumentException(
          String.format("%s must not be blank or empty.", paramName));
    }
    return chars;
  }

  /**
   * <p>Validate the stateful predicate is true for the given object and return the object;
   * otherwise throw an exception with the specified message.</p>
   * <p>
   * {@code String value = Validate.validState(someString, s -> s.length() == 0, "must be blank got:
   * %s", someString);}
   *
   * @param <T>     the object type
   * @param object  the object to check
   * @param test    the predicate to apply, will return true if the object is valid
   * @param message the {@link String#format(String, Object...)} exception message if invalid, not
   *                null
   * @param values  the optional values for the formatted exception message
   * @return the validated object
   * @throws NullPointerException if the object is {@code null}
   */
  public static <T> T validState(final T object, final Predicate<T> test, final String message,
      final Object... values) {
    if (!test.test(object)) {
      throw new IllegalStateException(String.format(message, values));
    }
    return object;
  }

  // notEmpty array
  //---------------------------------------------------------------------------------

  /**
   * <p>Validate that the specified argument array is neither {@code null}
   * nor a length of zero (no elements); otherwise throwing an exception with the specified
   * message.
   *
   * <pre>Validate.notEmpty(myArray, "The array must not be empty");</pre>
   *
   * @param <T>     the array type
   * @param array   the array to check, validated not null by this method
   * @param message the {@link String#format(String, Object...)} exception message if invalid, not
   *                null
   * @param values  the optional values for the formatted exception message, null array not
   *                recommended
   * @return the validated array (never {@code null} method for chaining)
   * @throws NullPointerException     if the array is {@code null}
   * @throws IllegalArgumentException if the array is empty
   */
  public static <T> T[] assertNotEmpty(final T[] array, final String message,
      final Object... values) {
    if (array == null) {
      throw new NullPointerException(String.format(message, values));
    }
    if (array.length == 0) {
      throw new IllegalArgumentException(String.format(message, values));
    }
    return array;
  }

  // notEmpty collection
  //---------------------------------------------------------------------------------

  /**
   * <p>Validate that the specified argument collection is neither {@code null}
   * nor a size of zero (no elements); otherwise throwing an exception with the specified message.
   *
   * <pre>Validate.notEmpty(myCollection, "The collection must not be empty");</pre>
   *
   * @param <T>        the collection type
   * @param collection the collection to check, validated not null by this method
   * @param message    the {@link String#format(String, Object...)} exception message if invalid,
   *                   not null
   * @param values     the optional values for the formatted exception message, null array not
   *                   recommended
   * @return the validated collection (never {@code null} method for chaining)
   * @throws NullPointerException     if the collection is {@code null}
   * @throws IllegalArgumentException if the collection is empty
   */
  public static <T extends Collection<?>> T assertNotEmpty(final T collection, final String message,
      final Object... values) {
    if (collection == null) {
      throw new NullPointerException(String.format(message, values));
    }
    if (collection.isEmpty()) {
      throw new IllegalArgumentException(String.format(message, values));
    }
    return collection;
  }

  // notEmpty map
  //---------------------------------------------------------------------------------

  /**
   * <p>Validate that the specified argument map is neither {@code null}
   * nor a size of zero (no elements); otherwise throwing an exception with the specified message.
   *
   * <pre>Validate.notEmpty(myMap, "The map must not be empty");</pre>
   *
   * @param <T>     the map type
   * @param map     the map to check, validated not null by this method
   * @param message the {@link String#format(String, Object...)} exception message if invalid, not
   *                null
   * @param values  the optional values for the formatted exception message, null array not
   *                recommended
   * @return the validated map (never {@code null} method for chaining)
   * @throws NullPointerException     if the map is {@code null}
   * @throws IllegalArgumentException if the map is empty
   */
  public static <T extends Map<?, ?>> T assertNotEmpty(final T map, final String message,
      final Object... values) {
    if (map == null) {
      throw new NullPointerException(String.format(message, values));
    }
    if (map.isEmpty()) {
      throw new IllegalArgumentException(String.format(message, values));
    }
    return map;
  }

  // notEmpty string
  //---------------------------------------------------------------------------------

  /**
   * <p>Validate that the specified argument character sequence is
   * neither {@code null} nor a length of zero (no characters); otherwise throwing an exception with
   * the specified message.
   *
   * <pre>Validate.notEmpty(myString, "The string must not be empty");</pre>
   *
   * @param <T>     the character sequence type
   * @param chars   the character sequence to check, validated not null by this method
   * @param message the {@link String#format(String, Object...)} exception message if invalid, not
   *                null
   * @param values  the optional values for the formatted exception message, null array not
   *                recommended
   * @return the validated character sequence (never {@code null} method for chaining)
   * @throws NullPointerException     if the character sequence is {@code null}
   * @throws IllegalArgumentException if the character sequence is empty
   */
  public static <T extends CharSequence> T assertNotEmpty(final T chars, final String message,
      final Object... values) {
    if (chars == null) {
      throw new NullPointerException(String.format(message, values));
    }
    if (chars.length() == 0) {
      throw new IllegalArgumentException(String.format(message, values));
    }
    return chars;
  }

  // notBlank string
  //---------------------------------------------------------------------------------

  /**
   * <p>Validate that the specified argument character sequence is
   * neither {@code null}, a length of zero (no characters), empty nor whitespace; otherwise
   * throwing an exception with the specified message.
   *
   * <pre>Validate.notBlank(myString, "The string must not be blank");</pre>
   *
   * @param <T>     the character sequence type
   * @param chars   the character sequence to check, validated not null by this method
   * @param message the {@link String#format(String, Object...)} exception message if invalid, not
   *                null
   * @param values  the optional values for the formatted exception message, null array not
   *                recommended
   * @return the validated character sequence (never {@code null} method for chaining)
   * @throws NullPointerException     if the character sequence is {@code null}
   * @throws IllegalArgumentException if the character sequence is blank
   */
  public static <T extends CharSequence> T assertNotBlank(final T chars, final String message,
      final Object... values) {
    if (chars == null) {
      throw new NullPointerException(String.format(message, values));
    }
    if (isBlank(chars)) {
      throw new IllegalArgumentException(String.format(message, values));
    }
    return chars;
  }

  // noNullElements array
  //---------------------------------------------------------------------------------

  /**
   * <p>Validate that the specified argument array is neither
   * {@code null} nor contains any elements that are {@code null}; otherwise throwing an exception
   * with the specified message.
   *
   * <pre>Validate.noNullElements(myArray, "The array is null or contains null.");</pre>
   *
   * @param <T>     the array type
   * @param array   the array to check, validated not null by this method
   * @param message the {@link String#format(String, Object...)} exception message if invalid, not
   *                null
   * @param values  the optional values for the formatted exception message
   * @return the validated array (never {@code null} method for chaining)
   * @throws NullPointerException     if the array is {@code null}
   * @throws IllegalArgumentException if an element is {@code null}
   */
  public static <T> T[] assertNoNullElements(final T[] array, final String message,
      final Object... values) {
    Validate.notNull(array, message);
    for (T anArray : array) {
      if (anArray == null) {
        throw new IllegalArgumentException(String.format(message, values));
      }
    }
    return array;
  }

  // noNullElements iterable
  //---------------------------------------------------------------------------------

  /**
   * <p>Validate that the specified argument iterable is neither
   * {@code null} nor contains any elements that are {@code null}; otherwise throwing an exception
   * with the specified message.
   *
   * <pre>Validate.noNullElements(myCollection, "The collection is null or contains null.");</pre>
   *
   * @param <T>      the iterable type
   * @param iterable the iterable to check, validated not null by this method
   * @param message  the {@link String#format(String, Object...)} exception message if invalid, not
   *                 null
   * @param values   the optional values for the formatted exception message.
   * @return the validated iterable (never {@code null} method for chaining)
   * @throws NullPointerException     if the array is {@code null}
   * @throws IllegalArgumentException if an element is {@code null}
   */
  public static <T extends Iterable<?>> T assertNoNullElements(final T iterable,
      final String message, final Object... values) {
    Validate.notNull(iterable, "The validated object is null");
    for (Object o : iterable) {
      if (o == null) {
        throw new IllegalArgumentException(String.format(message, values));
      }
    }
    return iterable;
  }

  // validState
  //---------------------------------------------------------------------------------

  /**
   * <p>Validate that the stateful condition is {@code true}; otherwise
   * throwing an exception with the specified message. This method is useful when validating
   * according to an arbitrary boolean expression, such as validating a primitive number or using
   * your own custom validation expression.</p>
   *
   * <pre>Validate.validState(this.isOk(), "The state is not OK: %s", myObject);</pre>
   *
   * @param expression the boolean expression to check
   * @param message    the {@link String#format(String, Object...)} exception message if invalid,
   *                   not null
   * @param values     the optional values for the formatted exception message, null array not
   *                   recommended
   * @throws IllegalStateException if expression is {@code false}
   */
  public static void validState(final boolean expression, final String message,
      final Object... values) {
    if (!expression) {
      throw new IllegalStateException(String.format(message, values));
    }
  }

  // inclusiveBetween
  //---------------------------------------------------------------------------------

  /**
   * <p>Validate that the specified argument object fall between the two
   * inclusive values specified; otherwise, throws an exception with the specified message.</p>
   *
   * <pre>Validate.inclusiveBetween(0, 2, 1, "Not in boundaries");</pre>
   *
   * @param <T>     the type of the argument object
   * @param start   the inclusive start value, not null
   * @param end     the inclusive end value, not null
   * @param value   the object to validate, not null
   * @param message the {@link String#format(String, Object...)} exception message if invalid, not
   *                null
   * @param values  the optional values for the formatted exception message, null array not
   *                recommended
   * @throws IllegalArgumentException if the value falls outside the boundaries
   */
  public static <T extends Comparable<U>, U> T inclusiveBetween(final U start, final U end,
      final T value,
      final String message, final Object... values) {
    if (value.compareTo(start) < 0 || value.compareTo(end) > 0) {
      throw new IllegalArgumentException(String.format(message, values));
    }
    return value;
  }

  /**
   * Validate that the specified primitive value falls between the two inclusive values specified;
   * otherwise, throws an exception with the specified message.
   *
   * <pre>Validate.inclusiveBetween(0, 2, 1, "Not in range");</pre>
   *
   * @param start   the inclusive start value
   * @param end     the inclusive end value
   * @param value   the value to validate
   * @param message the exception message if invalid, not null
   * @throws IllegalArgumentException if the value falls outside the boundaries
   */
  public static long inclusiveBetween(final long start, final long end, final long value,
      final String message) {
    if (value < start || value > end) {
      throw new IllegalArgumentException(message);
    }
    return value;
  }

  /**
   * Validate that the specified primitive value falls between the two inclusive values specified;
   * otherwise, throws an exception with the specified message.
   *
   * <pre>Validate.inclusiveBetween(0.1, 2.1, 1.1, "Not in range");</pre>
   *
   * @param start   the inclusive start value
   * @param end     the inclusive end value
   * @param value   the value to validate
   * @param message the exception message if invalid, not null
   * @throws IllegalArgumentException if the value falls outside the boundaries
   */
  public static double inclusiveBetween(final double start, final double end, final double value,
      final String message) {
    if (value < start || value > end) {
      throw new IllegalArgumentException(message);
    }
    return value;
  }

  // exclusiveBetween
  //---------------------------------------------------------------------------------

  /**
   * <p>Validate that the specified argument object fall between the two
   * exclusive values specified; otherwise, throws an exception with the specified message.</p>
   *
   * <pre>Validate.exclusiveBetween(0, 2, 1, "Not in boundaries");</pre>
   *
   * @param <T>     the type of the argument object
   * @param start   the exclusive start value, not null
   * @param end     the exclusive end value, not null
   * @param value   the object to validate, not null
   * @param message the {@link String#format(String, Object...)} exception message if invalid, not
   *                null
   * @param values  the optional values for the formatted exception message, null array not
   *                recommended
   * @throws IllegalArgumentException if the value falls outside the boundaries
   */
  public static <T extends Comparable<U>, U> T exclusiveBetween(final U start, final U end,
      final T value,
      final String message, final Object... values) {
    if (value.compareTo(start) <= 0 || value.compareTo(end) >= 0) {
      throw new IllegalArgumentException(String.format(message, values));
    }
    return value;
  }

  /**
   * Validate that the specified primitive value falls between the two exclusive values specified;
   * otherwise, throws an exception with the specified message.
   *
   * <pre>Validate.exclusiveBetween(0, 2, 1, "Not in range");</pre>
   *
   * @param start   the exclusive start value
   * @param end     the exclusive end value
   * @param value   the value to validate
   * @param message the exception message if invalid, not null
   * @throws IllegalArgumentException if the value falls outside the boundaries
   */
  public static long exclusiveBetween(final long start, final long end, final long value,
      final String message) {
    if (value <= start || value >= end) {
      throw new IllegalArgumentException(message);
    }
    return value;
  }

  /**
   * Validate that the specified primitive value falls between the two exclusive values specified;
   * otherwise, throws an exception with the specified message.
   *
   * <pre>Validate.exclusiveBetween(0.1, 2.1, 1.1, "Not in range");</pre>
   *
   * @param start   the exclusive start value
   * @param end     the exclusive end value
   * @param value   the value to validate
   * @param message the exception message if invalid, not null
   * @throws IllegalArgumentException if the value falls outside the boundaries
   */
  public static double exclusiveBetween(final double start, final double end, final double value,
      final String message) {
    if (value <= start || value >= end) {
      throw new IllegalArgumentException(message);
    }
    return value;
  }

  // isInstanceOf
  //---------------------------------------------------------------------------------

  /**
   * <p>Validate that the argument is an instance of the specified class; otherwise
   * throwing an exception with the specified message. This method is useful when validating
   * according to an arbitrary class</p>
   *
   * <pre>Validate.isInstanceOf(OkClass.class, object, "Wrong class, object is of class %s",
   *   object.getClass().getName());</pre>
   *
   * @param type    the class the object must be validated against, not null
   * @param obj     the object to check, null throws an exception
   * @param message the {@link String#format(String, Object...)} exception message if invalid, not
   *                null
   * @param values  the optional values for the formatted exception message, null array not
   *                recommended
   * @throws IllegalArgumentException if argument is not of specified class
   */
  public static <T, U> U assertInstanceOf(final Class<U> type, final T obj, final String message,
      final Object... values) {
    if (!type.isInstance(obj)) {
      throw new IllegalArgumentException(String.format(message, values));
    }
    return type.cast(obj);
  }

  // isAssignableFrom
  //---------------------------------------------------------------------------------

  /**
   * Validates that the argument can be converted to the specified class, if not throws an
   * exception.
   *
   * <p>This method is useful when validating if there will be no casting errors.</p>
   *
   * <pre>Validate.isAssignableFrom(SuperClass.class, object.getClass());</pre>
   *
   * <p>The message of the exception is &quot;The validated object can not be converted to
   * the&quot; followed by the name of the class and &quot;class&quot;</p>
   *
   * @param superType the class the class must be validated against, not null
   * @param type      the class to check, not null
   * @param message   the {@link String#format(String, Object...)} exception message if invalid, not
   *                  null
   * @param values    the optional values for the formatted exception message, null array not
   *                  recommended
   * @throws IllegalArgumentException if argument can not be converted to the specified class
   */
  public static <T> Class<? extends T> assertAssignableFrom(final Class<T> superType,
      final Class<?> type,
      final String message, final Object... values) {
    if (!superType.isAssignableFrom(type)) {
      throw new IllegalArgumentException(String.format(message, values));
    }
    return (Class<? extends T>) type;
  }

  /**
   * Asserts that the given number is positive (non-negative and non-zero).
   *
   * @param num       Number to validate
   * @param fieldName Field name to display in exception message if not positive.
   * @return Number if positive.
   */
  public static int assertPositive(int num, String fieldName) {
    if (num <= 0) {
      throw new IllegalArgumentException(String.format("%s must be positive", fieldName));
    }
    return num;
  }

  /**
   * Asserts that the given number is positive (non-negative and non-zero).
   *
   * @param num       Number to validate
   * @param fieldName Field name to display in exception message if not positive.
   * @return Number if positive.
   */
  public static long assertPositive(long num, String fieldName) {
    if (num <= 0) {
      throw new IllegalArgumentException(String.format("%s must be positive", fieldName));
    }
    return num;
  }

  public static double assertPositive(double num, String fieldName) {
    if (num <= 0) {
      throw new IllegalArgumentException(String.format("%s must be positive", fieldName));
    }
    return num;
  }

  public static int assertNotNegative(int num, String fieldName) {
    if (num < 0) {
      throw new IllegalArgumentException(String.format("%s must not be negative", fieldName));
    }
    return num;
  }

  public static long assertNotNegative(long num, String fieldName) {
    if (num < 0) {
      throw new IllegalArgumentException(String.format("%s must not be negative", fieldName));
    }
    return num;
  }

  /**
   * Asserts that the given duration is positive (non-negative and non-zero).
   *
   * @param duration  Number to validate
   * @param fieldName Field name to display in exception message if not positive.
   * @return Duration if positive.
   */
  public static Duration assertPositive(Duration duration, String fieldName) {
    if (duration == null) {
      throw new IllegalArgumentException(String.format("%s cannot be null", fieldName));
    }

    if (duration.isNegative() || duration.isZero()) {
      throw new IllegalArgumentException(String.format("%s must be positive", fieldName));
    }
    return duration;
  }

  /**
   * Asserts that the given duration is positive (non-negative and non-zero) or null.
   *
   * @param duration  Number to validate
   * @param fieldName Field name to display in exception message if not positive.
   * @return Duration if positive or null.
   */
  public static Duration assertPositiveOrNull(Duration duration, String fieldName) {
    if (duration == null) {
      return null;
    }
    return assertPositive(duration, fieldName);
  }

  /**
   * Asserts that the given boxed integer is positive (non-negative and non-zero) or null.
   *
   * @param num       Boxed integer to validate
   * @param fieldName Field name to display in exception message if not positive.
   * @return Duration if positive or null.
   */
  public static Integer assertPositiveOrNull(Integer num, String fieldName) {
    if (num == null) {
      return null;
    }
    return assertPositive(num, fieldName);
  }

  /**
   * Asserts that the given boxed long is positive (non-negative and non-zero) or null.
   *
   * @param num       Boxed integer to validate
   * @param fieldName Field name to display in exception message if not positive.
   * @return Duration if positive or null.
   */
  public static Long assertPositiveOrNull(Long num, String fieldName) {
    if (num == null) {
      return null;
    }
    return assertPositive(num, fieldName);
  }

  /**
   * Asserts that the given boxed double is positive (non-negative and non-zero) or null.
   *
   * @param num       Boxed double to validate
   * @param fieldName Field name to display in exception message if not positive.
   * @return Duration if double or null.
   */
  public static Double assertPositiveOrNull(Double num, String fieldName) {
    if (num == null) {
      return null;
    }
    return assertPositive(num, fieldName);
  }

  /**
   * Asserts that the given boxed long is positive (non-negative and non-zero) or null.
   *
   * @param num       Boxed long to validate
   * @param fieldName Field name to display in exception message if not positive.
   * @return Duration if positive or null.
   */
  public static Long isPositiveOrNull(Long num, String fieldName) {
    if (num == null) {
      return null;
    }
    return assertPositive(num, fieldName);
  }

  /**
   * Asserts that the given duration is positive (non-negative and non-zero).
   *
   * @param duration  Number to validate
   * @param fieldName Field name to display in exception message if not positive.
   * @return Duration if positive.
   */
  public static Duration assertNotNegative(Duration duration, String fieldName) {
    if (duration == null) {
      throw new IllegalArgumentException(String.format("%s cannot be null", fieldName));
    }

    if (duration.isNegative()) {
      throw new IllegalArgumentException(String.format("%s must not be negative", fieldName));
    }
    return duration;
  }

  /**
   * Returns the param if non null, otherwise gets a default value from the provided
   * {@link Supplier}.
   *
   * @param param        Param to return if non null.
   * @param defaultValue Supplier of default value.
   * @param <T>          Type of value.
   * @return Value of param or default value if param was null.
   */
  public static <T> T getOrDefault(T param, Supplier<T> defaultValue) {
    assertParamNotNull(defaultValue, "defaultValue");
    return param != null ? param : defaultValue.get();
  }

  /**
   * Verify that only one of the objects is non null. If all objects are null this method does not
   * throw.
   *
   * @param message Error message if more than one object is non-null.
   * @param objs    Objects to validate.
   * @throws IllegalArgumentException if more than one of the objects was non-null.
   */
  public static void mutuallyExclusive(String message, Object... objs) {
    boolean oneProvided = false;
    for (Object o : objs) {
      if (o != null) {
        if (oneProvided) {
          throw new IllegalArgumentException(message);
        } else {
          oneProvided = true;
        }
      }
    }
  }

}
