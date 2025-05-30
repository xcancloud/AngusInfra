package cloud.xcan.angus.spec.utils;

import static cloud.xcan.angus.spec.SpecConstant.DEFAULT_CHUNK_SIZE;
import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DEFAULT_DATE_TIME_FORMAT;
import static cloud.xcan.angus.spec.experimental.Assert.assertNull;
import static cloud.xcan.angus.spec.experimental.Assert.assertTrue;
import static cloud.xcan.angus.spec.experimental.BizConstant.ARRAY_ELEMENT_SEPARATOR;
import static cloud.xcan.angus.spec.experimental.BizConstant.ARRAY_END;
import static cloud.xcan.angus.spec.experimental.BizConstant.ARRAY_START;
import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_ROOT_PID;
import static cloud.xcan.angus.spec.experimental.BizConstant.EMPTY_ARRAY;
import static cloud.xcan.angus.spec.experimental.BizConstant.EMPTY_OBJECT_ARRAY;
import static cloud.xcan.angus.spec.experimental.BizConstant.EMPTY_STRING;
import static cloud.xcan.angus.spec.experimental.BizConstant.INITIAL_HASH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MULTIPLIER;
import static cloud.xcan.angus.spec.experimental.BizConstant.NULL_STRING;
import static cloud.xcan.angus.spec.experimental.StandardCharsets.UTF_8;
import static cloud.xcan.angus.spec.http.HttpHeaderValues.BASE64;
import static cloud.xcan.angus.spec.http.HttpHeaderValues.GZIP_BASE64;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.spec.annotations.Nullable;
import cloud.xcan.angus.spec.experimental.Assert;
import cloud.xcan.angus.spec.utils.crypto.Base64Utils;
import cloud.xcan.angus.spec.utils.map.UnmodifiableMapOfLists;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.StringJoiner;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

public final class ObjectUtils extends org.apache.commons.lang3.ObjectUtils {

  private ObjectUtils() { /* no instance */ }

  /**
   * @param value string value
   * @return value, if it's not null, or return "" if the value is null.
   */
  public static String stringSafe(String value) {
    return isNull(value) ? "" : value;
  }

  /**
   * @param value object value
   * @return value, if it's not null, or return "" if the value is null.
   */
  public static String stringSafe(Object value) {
    return isNull(value) ? "" : value.toString();
  }

  /**
   * @param value integer value
   * @return value, if it's not null, or return safe if the value is null.
   */
  public static String stringSafe(String value, String safe) {
    return isNull(value) ? safe : value;
  }

  /**
   * @param actual actual value
   * @return value, if it's not null, or return safe if the value is null.
   */
  public static Integer integerSafe(Integer actual, Integer safe) {
    return isNull(actual) ? safe : actual;
  }

  /**
   * @param actual actual value
   * @return value, if it's not null and value > 0, or return safe if the value is null or value <
   * 0.
   */
  public static Integer unsignedIntegerSafe(Integer actual, Integer safe) {
    return isNull(actual) || actual < 0 ? safe : actual;
  }

  /**
   * @param actual actual value
   * @return value, if it's not null, or return safe if the value is null.
   */
  public static Long longSafe(Long actual, Long safe) {
    return isNull(actual) ? safe : actual;
  }

  /**
   * @param actual actual value
   * @return value, if it's not null and value > 0, or return safe if the value is null or value <
   * 0.
   */
  public static Long unsignedLongSafe(Long actual, Long safe) {
    return isNull(actual) || actual < 0 ? safe : actual;
  }

  /**
   * @param actual actual value
   * @param safe   value
   * @return actual value, if it's not null, or safe value if the actual value is null.
   */
  public static <T> T nullSafe(T actual, T safe) {
    return isNull(actual) ? safe : actual;
  }

  /**
   * @param actual actual value
   * @param safe   value
   * @return actual value, if it's not empty, or safe value if the actual value is empty.
   */
  public static String emptySafe(String actual, String safe) {
    return isEmpty(actual) ? safe : actual;
  }

  /**
   * @param actual actual value
   * @return actual value, if it's not empty, or "" if the actual value is empty.
   */
  public static String emptySafe(String actual) {
    return isEmpty(actual) ? "" : actual;
  }

  /**
   * @param actual actual value
   * @param safe   value
   * @return actual value, if it's not empty, or safe value if the actual value is empty.
   */
  public static <T> List<T> emptySafe(List<T> actual, List<T> safe) {
    if (isNull(actual)) {
      return safe; // Fix decompile:: isEmpty(actual) -> isEmpty((Collection)actual)
    }
    return isEmpty(actual) ? safe : actual;
  }

  /**
   * @param actual actual value
   * @return actual value, if it's not empty, or {@link Collections#EMPTY_LIST} if the actual value
   * is empty.
   */
  public static <T> List emptySafe(List<T> actual) {
    return isEmpty(actual) ? Collections.EMPTY_LIST : actual;
  }

  /**
   * @param actual actual value
   * @param safe   value
   * @return actual value, if it's not empty, or safe value if the actual value is empty.
   */
  public static <T> Set emptySafe(Set<T> actual, Set<T> safe) {
    if (isNull(actual)) {
      return safe; // Fix decompile:: isEmpty(actual) -> isEmpty((Collection)actual)
    }
    return isEmpty(actual) ? safe : actual;
  }

  /**
   * @param actual actual value
   * @return actual value, if it's not empty, or {@link Collections#EMPTY_SET} if the actual value
   * is empty.
   */
  public static <T> Set emptySafe(Set<T> actual) {
    return isEmpty(actual) ? Collections.EMPTY_SET : actual;
  }

  /**
   * @param actual actual value
   * @param safe   value
   * @return actual value, if it's not empty, or safe value if the actual value is empty.
   */
  public static <K, V> Map emptySafe(Map<K, V> actual, Map<K, V> safe) {
    if (isNull(actual)) {
      return safe; // Fix decompile:: isEmpty(actual) -> isEmpty((Collection)actual)
    }
    return isEmpty(actual) ? safe : actual;
  }

  /**
   * @param actual actual value
   * @return actual value, if it's not empty, or {@link Collections#EMPTY_MAP} if the actual value
   * is empty.
   */
  public static <K, V> Map emptySafe(Map<K, V> actual) {
    return isEmpty(actual) ? Collections.EMPTY_MAP : actual;
  }

  public static String lengthSafe(String src, int safeLength) {
    return isEmpty(src) || src.length() <= safeLength ? src : src.substring(0, safeLength);
  }

  public static String lengthSafeOmitted(String src, int safeLength) {
    Assert.assertTrue(safeLength > 3,
        "The length of the secure string value is insufficiently short.");
    return isEmpty(src) || src.length() <= safeLength ? src
        : src.substring(0, safeLength - 2) + "..";
  }

  public static int sizeSafe(String value) {
    return isNotEmpty(value) ? value.length() : 0;
  }

  public static int sizeSafe(String value, int safeSize) {
    return isNotEmpty(value) ? value.length() : safeSize;
  }

  public static <V> int sizeSafe(Collection<V> values) {
    return isNotEmpty(values) ? values.size() : 0;
  }

  public static <V> int sizeSafe(Collection<V> values, int safeSize) {
    return isNotEmpty(values) ? values.size() : safeSize;
  }

  public static <K, V> int sizeSafe(Map<K, V> values) {
    return isNotEmpty(values) ? values.size() : 0;
  }

  public static <K, V> int sizeSafe(Map<K, V> values, int safeSize) {
    return isNotEmpty(values) ? values.size() : safeSize;
  }

  public static BigDecimal amountScaleSafe(BigDecimal amount) {
    return isNull(amount) ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
        : amount.setScale(2, RoundingMode.HALF_UP);
  }

  public static String safeStringInValue(String value) {
    value = safeStringValue(value);
    value = safeInValue(value);
    return value;
  }

  public static String safeStringValue(String stringValue) {
    stringValue = StringUtils.removeStart(stringValue, "\"");
    stringValue = StringUtils.removeEnd(stringValue, "\"");
    return stringValue;
  }

  public static String safeInValue(String inStringValue) {
    inStringValue = StringUtils.removeStart(inStringValue, "[");
    inStringValue = StringUtils.removeEnd(inStringValue, "]");
    return inStringValue;
  }

  /**
   * @param object the Object to check
   */
  public static boolean isNull(Object object) {
    return object == null;
  }

  /**
   * @param object the Object to check
   */
  public static boolean isNotNull(Object object) {
    return object != null;
  }

  /**
   * @param objects the Array to check
   */
  public static boolean isEmpty(Object[] objects) {
    return objects == null || objects.length == 0
        || (objects.length == 1 && objects[0]
        == null) /* Fix:: String... policyOrAuthority, The passed value is null, the received array length=1 and the value is null*/;
  }

  /**
   * @param objects the Array to check
   */
  public static boolean isNotEmpty(Object[] objects) {
    return objects != null && (objects.length > 1 || (objects.length == 1 && objects[0]
        != null))/* Fix:: String... policyOrAuthority, The passed value is null, the received array length=1 and the value is null*/;
  }

  /**
   * @param objects the Collection to check
   */
  public static boolean isEmpty(Collection<?> objects) {
    return objects == null || objects.size() == 0
        /*Fix:: When the name of Collections.singleton(name) is null, the number of collection elements will be 1 */
        || (objects.size() == 1 && objects.iterator().next() == null);
  }

  /**
   * @param objects the Collection to check
   */
  public static boolean isNotEmpty(Collection<?> objects) {
    return !isEmpty(objects);
  }

  public static boolean isNullOrEmpty(Collection<?> collection) {
    return collection == null || collection.isEmpty();
  }

  public static boolean isNullOrEmpty(Map map) {
    return map == null || map.isEmpty();
  }

  public static boolean isNotEmpty(Map map) {
    return map != null && !map.isEmpty();
  }

  public static <T> List<T> objectToList(Object obj, Class<T> clz) {
    List<T> list = new ArrayList<>();
    if (obj instanceof ArrayList) {
      for (Object o : (List) obj) {
        list.add(clz.cast(o));
      }
      return list;
    }
    return null;
  }

  /**
   * Attempt to find a present-valued optional in a list of optionals.
   *
   * @param firstValue     The first value that should be checked.
   * @param fallbackValues The suppliers we should check in order for a present value.
   * @return The first present value (or Optional.empty() if none are present)
   */
  @SafeVarargs
  public static <T> Optional<T> firstPresent(Optional<T> firstValue,
      Supplier<Optional<T>>... fallbackValues) {
    if (firstValue.isPresent()) {
      return firstValue;
    }

    for (Supplier<Optional<T>> fallbackValueSupplier : fallbackValues) {
      Optional<T> fallbackValue = fallbackValueSupplier.get();
      if (fallbackValue.isPresent()) {
        return fallbackValue;
      }
    }
    return Optional.empty();
  }

  public static <T> Optional<T> firstPresent(Optional<T> firstValue, Supplier<T> fallbackValue) {
    if (firstValue.isPresent()) {
      return firstValue;
    }
    return Optional.ofNullable(fallbackValue.get());
  }

  /**
   * Returns a new list containing the second list appended to the first list.
   */
  public static <T> List<T> mergeLists(List<T> list1, List<T> list2) {
    List<T> newMerged = new LinkedList<>();
    if (list1 != null) {
      newMerged.addAll(list1);
    }
    if (list2 != null) {
      newMerged.addAll(list2);
    }
    return newMerged;
  }

  /**
   * @param list List to get first element from.
   * @param <T>  Type of elements in the list.
   * @return The first element in the list if it exists. If the list is null or empty this will
   * return null.
   */
  public static <T> T firstIfPresent(List<T> list) {
    if (list == null || list.isEmpty()) {
      return null;
    } else {
      return list.get(0);
    }
  }

  public static <T, V> Map<T, List<V>> mergeMaps(Map<T, List<V>> map1, Map<T, List<V>> map2) {
    if (isEmpty(map1)) {
      return map2;
    }

    if (isEmpty(map2)) {
      return map1;
    }

    Map<T, List<V>> newMerged = new HashMap<>();

    for (Entry<T, List<V>> entry : map1.entrySet()) {
      T key = entry.getKey();
      List<V> value = entry.getValue();
      newMerged.put(key, new ArrayList<>(value));
    }

    for (Entry<T, List<V>> entry : map2.entrySet()) {
      T key = entry.getKey();
      List<V> value = entry.getValue();
      if (newMerged.containsKey(key)) {
        newMerged.get(key).addAll(value);
      } else {
        newMerged.put(key, new ArrayList<>(value));
      }
    }
    return newMerged;
  }

  public static <T, V> Map<T, List<V>> mergeToMaps(Map<T, List<V>> from, Map<T, List<V>> to) {
    if (isEmpty(from)) {
      return to;
    }

    if (isNull(to)) {
      to = new HashMap<>();
    }

    for (Entry<T, List<V>> entry : from.entrySet()) {
      T key = entry.getKey();
      List<V> value = entry.getValue();
      if (to.containsKey(key)) {
        to.get(key).addAll(value);
      } else {
        to.put(key, new ArrayList<>(value));
      }
    }
    return to;
  }

  public static Map<Long, Long> sumMaps(Map<Long, Long>... maps) {
    Map<Long, Long> result = new HashMap<>();
    for (Map<Long, Long> map : maps) {
      for (Entry<Long, Long> entry : map.entrySet()) {
        Long key = entry.getKey();
        Long value = entry.getValue();
        result.put(key, (result.getOrDefault(key, 0L)) + value);
      }
    }
    return result;
  }

  /**
   * Perform a deep copy of the provided map of lists. This only performs a deep copy of the map and
   * lists. Entries are not copied, so care should be taken to ensure that entries are immutable if
   * preventing unwanted mutations of the elements is desired.
   */
  public static <T, U> Map<T, List<U>> deepCopyMap(Map<T, ? extends List<U>> map) {
    return deepCopyMap(map, () -> new LinkedHashMap<>(map.size()));
  }

  /**
   * Perform a deep copy of the provided map of lists. This only performs a deep copy of the map and
   * lists. Entries are not copied, so care should be taken to ensure that entries are immutable if
   * preventing unwanted mutations of the elements is desired.
   */
  public static <T, U> Map<T, List<U>> deepCopyMap(Map<T, ? extends List<U>> map,
      Supplier<Map<T, List<U>>> mapConstructor) {
    Map<T, List<U>> result = mapConstructor.get();
    map.forEach((k, v) -> result.put(k, new ArrayList<>(v)));
    return result;
  }

  public static <T, U> Map<T, List<U>> unmodifiableMapOfLists(Map<T, List<U>> map) {
    return new UnmodifiableMapOfLists<>(map);
  }

  /**
   * Perform a deep copy of the provided map of lists, and make the result unmodifiable.
   * <p>
   * This is equivalent to calling {@link #deepCopyMap} followed by
   * {@link #unmodifiableMapOfLists}.
   */
  public static <T, U> Map<T, List<U>> deepUnmodifiableMap(Map<T, ? extends List<U>> map) {
    return unmodifiableMapOfLists(deepCopyMap(map));
  }

  /**
   * Perform a deep copy of the provided map of lists, and make the result unmodifiable.
   * <p>
   * This is equivalent to calling {@link #deepCopyMap} followed by
   * {@link #unmodifiableMapOfLists}.
   */
  public static <T, U> Map<T, List<U>> deepUnmodifiableMap(Map<T, ? extends List<U>> map,
      Supplier<Map<T, List<U>>> mapConstructor) {
    return unmodifiableMapOfLists(deepCopyMap(map, mapConstructor));
  }


  /**
   * Collect a stream of {@link Entry} to a {@link Map} with the same key/value types
   *
   * @param <K> the key type
   * @param <V> the value type
   * @return a map
   */
  public static <K, V> Collector<Entry<K, V>, ?, Map<K, V>> toMap() {
    return Collectors.toMap(Entry::getKey, Entry::getValue);
  }

  /**
   * Transforms the values of a map to another map with the same keys, using the supplied function.
   *
   * @param inputMap the input map
   * @param mapper   the function used to transform the map values
   * @param <K>      the key type
   * @param <VInT>   the value type for the input map
   * @param <VOutT>  the value type for the output map
   * @return a map
   */
  public static <K, VInT, VOutT> Map<K, VOutT> mapValues(Map<K, VInT> inputMap,
      Function<VInT, VOutT> mapper) {
    return inputMap.entrySet().stream()
        .collect(Collectors.toMap(Entry::getKey, e -> mapper.apply(e.getValue())));
  }

  /**
   * Filters a map based on a condition
   *
   * @param map       the input map
   * @param condition the predicate to filter on
   * @param <K>       the key type
   * @param <V>       the value type
   * @return the filtered map
   */
  public static <K, V> Map<K, V> filterMap(Map<K, V> map, Predicate<Entry<K, V>> condition) {
    return map.entrySet().stream().filter(condition)
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  /**
   * Return a new map that is the inverse of the supplied map, with the values becoming the keys and
   * vice versa. Requires the values to be unique.
   *
   * @param inputMap a map where both the keys and values are unique
   * @param <K>      the key type
   * @param <V>      the value type
   * @return a map
   */
  public static <K, V> Map<K, V> inverseMap(Map<V, K> inputMap) {
    return inputMap.entrySet().stream()
        .collect(Collectors.toMap(Entry::getValue, Entry::getKey));
  }

  /**
   * For a collection of values of type {@code V} that can all be converted to type {@code K},
   * create a map that indexes all of the values by {@code K}. This requires that no two values map
   * to the same index.
   *
   * @param values        the collection of values to index
   * @param indexFunction the function used to convert a value to its index
   * @param <K>           the index (or key) type
   * @param <V>           the value type
   * @return a (modifiable) map that indexes K to its unique value V
   * @throws IllegalArgumentException if any of the values map to the same index
   */
  public static <K, V> Map<K, V> uniqueIndex(Iterable<V> values,
      Function<? super V, K> indexFunction) {
    Map<K, V> map = new HashMap<>();
    for (V value : values) {
      K index = indexFunction.apply(value);
      V prev = map.put(index, value);
      assertNull(prev, "No duplicate indices allowed but both %s and %s have the same index: %s",
          prev, value, index);
    }
    return map;
  }

  /**
   * Get De-duplication data.
   * <p>
   * Usage example:
   *
   * <pre>
   *   List<User> newUsers = users.stream()
   *          .filter(ObjectUtils.distinctByKey(User::getUsername))
   *          .collect(Collectors.toList());
   * </pre>
   *
   * @return De-duplication result
   */
  public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
    Map<Object, Boolean> seen = new ConcurrentHashMap<>();
    return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
  }

  /**
   * Get duplicate data.
   * <p>
   * Usage example:
   *
   * <pre>
   *   List<User> newUsers = users.stream()
   *       .filter(ObjectUtils.duplicateByKey(User::getUsername))
   *       .collect(Collectors.toList());
   * </pre>
   *
   * @return Duplicate data
   */
  public static <T> Predicate<T> duplicateByKey(Function<? super T, ?> keyExtractor) {
    Map<Object, Boolean> seen = new ConcurrentHashMap<>();
    return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) != null;
  }

  /**
   * Get De-duplication data.
   *
   * @return De-duplication result
   */
  public static <T> List<T> distinct(Collection<T> values) {
    if (values == null || values.size() <= 0) {
      return Collections.emptyList();
    }
    return values.stream().distinct().collect(Collectors.toList());
  }

  /**
   * Get De-duplication data.
   *
   * @return De-duplication result
   */
  public static <T> Collection<T> distinctByKey(Collection<T> src,
      Function<? super T, ?> keyExtractor) {
    if (isEmpty(src)) {
      return src;
    }
    Map<Object, T> seen = new ConcurrentHashMap<>();
    for (T t : src) {
      seen.put(keyExtractor.apply(t), t);
    }
    return seen.values();
  }

  /**
   * Return whether the given throwable is a checked exception: that is, neither a RuntimeException
   * nor an Error.
   *
   * @param ex the throwable to check
   * @return whether the throwable is a checked exception
   * @see Exception
   * @see RuntimeException
   * @see Error
   */
  public static boolean isCheckedException(Throwable ex) {
    return !(ex instanceof RuntimeException || ex instanceof Error);
  }

  /**
   * Check whether the given exception is compatible with the specified exception types, as declared
   * in a throws clause.
   *
   * @param ex                 the exception to check
   * @param declaredExceptions the exception types declared in the throws clause
   * @return whether the given exception is compatible
   */
  public static boolean isCompatibleWithThrowsClause(Throwable ex,
      @Nullable Class<?>... declaredExceptions) {
    if (!isCheckedException(ex)) {
      return true;
    }
    if (declaredExceptions != null) {
      for (Class<?> declaredException : declaredExceptions) {
        if (declaredException.isInstance(ex)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Determine whether the given object is an array: either an Object array or a primitive array.
   *
   * @param obj the object to check
   */
  public static boolean isArray(@Nullable Object obj) {
    return (obj != null && obj.getClass().isArray());
  }


  /**
   * Determine whether the given object is empty.
   * <p>This method supports the following object types.
   * <ul>
   * <li>{@code Optional}: considered empty if {@link Optional#empty()}</li>
   * <li>{@code Array}: considered empty if its length is zero</li>
   * <li>{@link CharSequence}: considered empty if its length is zero</li>
   * <li>{@link Collection}: delegates to {@link Collection#isEmpty()}</li>
   * <li>{@link Map}: delegates to {@link Map#isEmpty()}</li>
   * </ul>
   * <p>If the given object is non-null and not one of the aforementioned
   * supported types, this method returns {@code false}.
   *
   * @param obj the object to check
   * @return {@code true} if the object is {@code null} or <em>empty</em>
   * @see Optional#isPresent()
   * @see ObjectUtils#isEmpty(Object[])
   */
  public static boolean isEmpty(@Nullable Object obj) {
    if (obj == null) {
      return true;
    }

    if (obj instanceof Optional) {
      return ((Optional<?>) obj).isEmpty();
    }
    if (obj instanceof CharSequence) {
      return ((CharSequence) obj).length() == 0;
    }
    if (obj.getClass().isArray()) {
      return Array.getLength(obj) == 0;
    }
    if (obj instanceof Collection) {
      /*Fix:: When the name of Collections.singleton(name) is null, the number of collection elements will be 1 */
      return isEmpty((Collection<?>) obj);
    }
    if (obj instanceof Map) {
      return ((Map) obj).isEmpty();
    }

    // else
    return false;
  }

  /**
   * Unwrap the given object which is potentially a {@link Optional}.
   *
   * @param obj the candidate object
   * @return either the value held within the {@code Optional}, {@code null} if the {@code Optional}
   * is empty, or simply the given object as-is
   * @since 5.0
   */
  @Nullable
  public static Object unwrapOptional(@Nullable Object obj) {
    if (obj instanceof Optional) {
      Optional<?> optional = (Optional<?>) obj;
      if (optional.isEmpty()) {
        return null;
      }
      Object result = optional.get();
      assertTrue(!(result instanceof Optional), "Multi-level Optional usage not supported");
      return result;
    }
    return obj;
  }

  /**
   * Check whether the given array contains the given element.
   *
   * @param array   the array to check (may be {@code null}, in which case the return value will
   *                always be {@code false})
   * @param element the element to check for
   * @return whether the element has been found in the given array
   */
  public static boolean containsElement(@Nullable Object[] array, Object element) {
    if (array == null) {
      return false;
    }
    for (Object arrayEle : array) {
      if (nullSafeEquals(arrayEle, element)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Check whether the given array of enum constants contains a constant with the given name,
   * ignoring case when determining a match.
   *
   * @param enumValues the enum values to check, typically obtained via {@code MyEnum.values()}
   * @param constant   the constant name to find (must not be null or empty string)
   * @return whether the constant has been found in the given array
   */
  public static boolean containsConstant(Enum<?>[] enumValues, String constant) {
    return containsConstant(enumValues, constant, false);
  }

  /**
   * Check whether the given array of enum constants contains a constant with the given name.
   *
   * @param enumValues    the enum values to check, typically obtained via {@code MyEnum.values()}
   * @param constant      the constant name to find (must not be null or empty string)
   * @param caseSensitive whether case is significant in determining a match
   * @return whether the constant has been found in the given array
   */
  public static boolean containsConstant(Enum<?>[] enumValues, String constant,
      boolean caseSensitive) {
    for (Enum<?> candidate : enumValues) {
      if (caseSensitive ? candidate.toString().equals(constant) :
          candidate.toString().equalsIgnoreCase(constant)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Case insensitive alternative to {@link Enum#valueOf(Class, String)}.
   *
   * @param <E>        the concrete Enum type
   * @param enumValues the array of all Enum constants in question, usually per
   *                   {@code Enum.values()}
   * @param constant   the constant to get the enum value of
   * @throws IllegalArgumentException if the given constant is not found in the given array of enum
   *                                  values. Use {@link #containsConstant(Enum[], String)} as a
   *                                  guard to avoid this exception.
   */
  public static <E extends Enum<?>> E caseInsensitiveValueOf(E[] enumValues, String constant) {
    for (E candidate : enumValues) {
      if (candidate.toString().equalsIgnoreCase(constant)) {
        return candidate;
      }
    }
    throw new IllegalArgumentException("Constant [" + constant + "] does not exist in enum type " +
        enumValues.getClass().getComponentType().getName());
  }

  /**
   * Append the given object to the given array, returning a new array consisting of the input array
   * contents plus the given object.
   *
   * @param array the array to append to (can be {@code null})
   * @param obj   the object to append
   * @return the new array (of the same component type; never {@code null})
   */
  public static <A, O extends A> A[] addObjectToArray(@Nullable A[] array, @Nullable O obj) {
    Class<?> compType = Object.class;
    if (array != null) {
      compType = array.getClass().getComponentType();
    } else if (obj != null) {
      compType = obj.getClass();
    }
    int newArrLength = (array != null ? array.length + 1 : 1);
    @SuppressWarnings("unchecked")
    A[] newArr = (A[]) Array.newInstance(compType, newArrLength);
    if (array != null) {
      System.arraycopy(array, 0, newArr, 0, array.length);
    }
    newArr[newArr.length - 1] = obj;
    return newArr;
  }

  /**
   * Convert the given array (which may be a primitive array) to an object array (if necessary of
   * primitive wrapper objects).
   * <p>A {@code null} source value will be converted to an
   * empty Object array.
   *
   * @param source the (potentially primitive) array
   * @return the corresponding object array (never {@code null})
   * @throws IllegalArgumentException if the parameter is not an array
   */
  public static Object[] toObjectArray(@Nullable Object source) {
    if (source instanceof Object[]) {
      return (Object[]) source;
    }
    if (source == null) {
      return EMPTY_OBJECT_ARRAY;
    }
    if (!source.getClass().isArray()) {
      throw new IllegalArgumentException("Source is not an array: " + source);
    }
    int length = Array.getLength(source);
    if (length == 0) {
      return EMPTY_OBJECT_ARRAY;
    }
    Class<?> wrapperType = Array.get(source, 0).getClass();
    Object[] newArray = (Object[]) Array.newInstance(wrapperType, length);
    for (int i = 0; i < length; i++) {
      newArray[i] = Array.get(source, i);
    }
    return newArray;
  }

  //---------------------------------------------------------------------
  // Convenience methods for content-based equality/hash-code handling
  //---------------------------------------------------------------------

  /**
   * Determine if the given objects are equal, returning {@code true} if both are {@code null} or
   * {@code false} if only one is {@code null}.
   * <p>Compares arrays with {@code Arrays.equals}, performing an equality
   * check based on the array elements rather than the array reference.
   *
   * @param o1 first Object to compare
   * @param o2 second Object to compare
   * @return whether the given objects are equal
   * @see Object#equals(Object)
   * @see Arrays#equals
   */
  public static boolean nullSafeEquals(@Nullable Object o1, @Nullable Object o2) {
    if (o1 == o2) {
      return true;
    }
    if (o1 == null || o2 == null) {
      return false;
    }
    if (o1.equals(o2)) {
      return true;
    }
    if (o1.getClass().isArray() && o2.getClass().isArray()) {
      return arrayEquals(o1, o2);
    }
    return false;
  }

  /**
   * Compare the given arrays with {@code Arrays.equals}, performing an equality check based on the
   * array elements rather than the array reference.
   *
   * @param o1 first array to compare
   * @param o2 second array to compare
   * @return whether the given objects are equal
   * @see #nullSafeEquals(Object, Object)
   * @see Arrays#equals
   */
  private static boolean arrayEquals(Object o1, Object o2) {
    if (o1 instanceof Object[] && o2 instanceof Object[]) {
      return Arrays.equals((Object[]) o1, (Object[]) o2);
    }
    if (o1 instanceof boolean[] && o2 instanceof boolean[]) {
      return Arrays.equals((boolean[]) o1, (boolean[]) o2);
    }
    if (o1 instanceof byte[] && o2 instanceof byte[]) {
      return Arrays.equals((byte[]) o1, (byte[]) o2);
    }
    if (o1 instanceof char[] && o2 instanceof char[]) {
      return Arrays.equals((char[]) o1, (char[]) o2);
    }
    if (o1 instanceof double[] && o2 instanceof double[]) {
      return Arrays.equals((double[]) o1, (double[]) o2);
    }
    if (o1 instanceof float[] && o2 instanceof float[]) {
      return Arrays.equals((float[]) o1, (float[]) o2);
    }
    if (o1 instanceof int[] && o2 instanceof int[]) {
      return Arrays.equals((int[]) o1, (int[]) o2);
    }
    if (o1 instanceof long[] && o2 instanceof long[]) {
      return Arrays.equals((long[]) o1, (long[]) o2);
    }
    if (o1 instanceof short[] && o2 instanceof short[]) {
      return Arrays.equals((short[]) o1, (short[]) o2);
    }
    return false;
  }

  public static boolean collectionEquals(Collection<?> c1, Collection<?> c2) {
    if (c1 == null && c2 == null) {
      return true;
    }
    if (c1 == null || c2 == null || c1.size() != c2.size()) {
      return false;
    }
    Object[] array1 = c1.toArray();
    Object[] array2 = c2.toArray();
    return Arrays.equals(array1, array2);
  }

  /**
   * Return as hash code for the given object; typically the value of {@code Object#hashCode()}}. If
   * the object is an array, this method will delegate to any of the {@code nullSafeHashCode}
   * methods for arrays in this class. If the object is {@code null}, this method returns 0.
   *
   * @see Object#hashCode()
   * @see #nullSafeHashCode(Object[])
   * @see #nullSafeHashCode(boolean[])
   * @see #nullSafeHashCode(byte[])
   * @see #nullSafeHashCode(char[])
   * @see #nullSafeHashCode(double[])
   * @see #nullSafeHashCode(float[])
   * @see #nullSafeHashCode(int[])
   * @see #nullSafeHashCode(long[])
   * @see #nullSafeHashCode(short[])
   */
  public static int nullSafeHashCode(@Nullable Object obj) {
    if (obj == null) {
      return 0;
    }
    if (obj.getClass().isArray()) {
      if (obj instanceof Object[]) {
        return nullSafeHashCode((Object[]) obj);
      }
      if (obj instanceof boolean[]) {
        return nullSafeHashCode((boolean[]) obj);
      }
      if (obj instanceof byte[]) {
        return nullSafeHashCode((byte[]) obj);
      }
      if (obj instanceof char[]) {
        return nullSafeHashCode((char[]) obj);
      }
      if (obj instanceof double[]) {
        return nullSafeHashCode((double[]) obj);
      }
      if (obj instanceof float[]) {
        return nullSafeHashCode((float[]) obj);
      }
      if (obj instanceof int[]) {
        return nullSafeHashCode((int[]) obj);
      }
      if (obj instanceof long[]) {
        return nullSafeHashCode((long[]) obj);
      }
      if (obj instanceof short[]) {
        return nullSafeHashCode((short[]) obj);
      }
    }
    return obj.hashCode();
  }

  /**
   * Return a hash code based on the contents of the specified array. If {@code array} is
   * {@code null}, this method returns 0.
   */
  public static int nullSafeHashCode(@Nullable Object[] array) {
    if (array == null) {
      return 0;
    }
    int hash = INITIAL_HASH;
    for (Object element : array) {
      hash = MULTIPLIER * hash + nullSafeHashCode(element);
    }
    return hash;
  }

  /**
   * Return a hash code based on the contents of the specified array. If {@code array} is
   * {@code null}, this method returns 0.
   */
  public static int nullSafeHashCode(@Nullable boolean[] array) {
    if (array == null) {
      return 0;
    }
    int hash = INITIAL_HASH;
    for (boolean element : array) {
      hash = MULTIPLIER * hash + Boolean.hashCode(element);
    }
    return hash;
  }

  /**
   * Return a hash code based on the contents of the specified array. If {@code array} is
   * {@code null}, this method returns 0.
   */
  public static int nullSafeHashCode(@Nullable byte[] array) {
    if (array == null) {
      return 0;
    }
    int hash = INITIAL_HASH;
    for (byte element : array) {
      hash = MULTIPLIER * hash + element;
    }
    return hash;
  }

  /**
   * Return a hash code based on the contents of the specified array. If {@code array} is
   * {@code null}, this method returns 0.
   */
  public static int nullSafeHashCode(@Nullable char[] array) {
    if (array == null) {
      return 0;
    }
    int hash = INITIAL_HASH;
    for (char element : array) {
      hash = MULTIPLIER * hash + element;
    }
    return hash;
  }

  /**
   * Return a hash code based on the contents of the specified array. If {@code array} is
   * {@code null}, this method returns 0.
   */
  public static int nullSafeHashCode(@Nullable double[] array) {
    if (array == null) {
      return 0;
    }
    int hash = INITIAL_HASH;
    for (double element : array) {
      hash = MULTIPLIER * hash + Double.hashCode(element);
    }
    return hash;
  }

  /**
   * Return a hash code based on the contents of the specified array. If {@code array} is
   * {@code null}, this method returns 0.
   */
  public static int nullSafeHashCode(@Nullable float[] array) {
    if (array == null) {
      return 0;
    }
    int hash = INITIAL_HASH;
    for (float element : array) {
      hash = MULTIPLIER * hash + Float.hashCode(element);
    }
    return hash;
  }

  /**
   * Return a hash code based on the contents of the specified array. If {@code array} is
   * {@code null}, this method returns 0.
   */
  public static int nullSafeHashCode(@Nullable int[] array) {
    if (array == null) {
      return 0;
    }
    int hash = INITIAL_HASH;
    for (int element : array) {
      hash = MULTIPLIER * hash + element;
    }
    return hash;
  }

  /**
   * Return a hash code based on the contents of the specified array. If {@code array} is
   * {@code null}, this method returns 0.
   */
  public static int nullSafeHashCode(@Nullable long[] array) {
    if (array == null) {
      return 0;
    }
    int hash = INITIAL_HASH;
    for (long element : array) {
      hash = MULTIPLIER * hash + Long.hashCode(element);
    }
    return hash;
  }

  /**
   * Return a hash code based on the contents of the specified array. If {@code array} is
   * {@code null}, this method returns 0.
   */
  public static int nullSafeHashCode(@Nullable short[] array) {
    if (array == null) {
      return 0;
    }
    int hash = INITIAL_HASH;
    for (short element : array) {
      hash = MULTIPLIER * hash + element;
    }
    return hash;
  }

  //---------------------------------------------------------------------
  // Convenience methods for toString output
  //---------------------------------------------------------------------

  /**
   * Return a String representation of an object's overall identity.
   *
   * @param obj the object (may be {@code null})
   * @return the object's identity as String representation, or an empty String if the object was
   * {@code null}
   */
  public static String identityToString(@Nullable Object obj) {
    if (obj == null) {
      return EMPTY_STRING;
    }
    return obj.getClass().getName() + "@" + getIdentityHexString(obj);
  }

  /**
   * Return a hex String form of an object's identity hash code.
   *
   * @param obj the object
   * @return the object's identity code in hex notation
   */
  public static String getIdentityHexString(Object obj) {
    return Integer.toHexString(System.identityHashCode(obj));
  }

  /**
   * Return a content-based String representation if {@code obj} is not {@code null}; otherwise
   * returns an empty String.
   * <p>Differs from {@link #nullSafeToString(Object)} in that it returns
   * an empty String rather than "null" for a {@code null} value.
   *
   * @param obj the object to build a display String for
   * @return a display String representation of {@code obj}
   * @see #nullSafeToString(Object)
   */
  public static String getDisplayString(@Nullable Object obj) {
    if (obj == null) {
      return EMPTY_STRING;
    }
    return nullSafeToString(obj);
  }

  /**
   * Determine the class name for the given object.
   * <p>Returns a {@code "null"} String if {@code obj} is {@code null}.
   *
   * @param obj the object to introspect (may be {@code null})
   * @return the corresponding class name
   */
  public static String nullSafeClassName(@Nullable Object obj) {
    return (obj != null ? obj.getClass().getName() : NULL_STRING);
  }

  /**
   * Return a String representation of the specified Object.
   * <p>Builds a String representation of the contents in case of an array.
   * Returns a {@code "null"} String if {@code obj} is {@code null}.
   *
   * @param obj the object to build a String representation for
   * @return a String representation of {@code obj}
   */
  public static String nullSafeToString(@Nullable Object obj) {
    if (obj == null) {
      return NULL_STRING;
    }
    if (obj instanceof String) {
      return (String) obj;
    }
    if (obj instanceof Object[]) {
      return nullSafeToString((Object[]) obj);
    }
    if (obj instanceof boolean[]) {
      return nullSafeToString((boolean[]) obj);
    }
    if (obj instanceof byte[]) {
      return nullSafeToString((byte[]) obj);
    }
    if (obj instanceof char[]) {
      return nullSafeToString((char[]) obj);
    }
    if (obj instanceof double[]) {
      return nullSafeToString((double[]) obj);
    }
    if (obj instanceof float[]) {
      return nullSafeToString((float[]) obj);
    }
    if (obj instanceof int[]) {
      return nullSafeToString((int[]) obj);
    }
    if (obj instanceof long[]) {
      return nullSafeToString((long[]) obj);
    }
    if (obj instanceof short[]) {
      return nullSafeToString((short[]) obj);
    }
    String str = obj.toString();
    return (str != null ? str : EMPTY_STRING);
  }

  /**
   * Return a String representation of the contents of the specified array.
   * <p>The String representation consists of a list of the array's elements,
   * enclosed in curly braces ({@code "{}"}). Adjacent elements are separated by the characters
   * {@code ", "} (a comma followed by a space). Returns a {@code "null"} String if {@code array} is
   * {@code null}.
   *
   * @param array the array to build a String representation for
   * @return a String representation of {@code array}
   */
  public static String nullSafeToString(@Nullable Object[] array) {
    if (array == null) {
      return NULL_STRING;
    }
    int length = array.length;
    if (length == 0) {
      return EMPTY_ARRAY;
    }
    StringJoiner stringJoiner = new StringJoiner(ARRAY_ELEMENT_SEPARATOR, ARRAY_START, ARRAY_END);
    for (Object o : array) {
      stringJoiner.add(String.valueOf(o));
    }
    return stringJoiner.toString();
  }

  /**
   * Return a String representation of the contents of the specified array.
   * <p>The String representation consists of a list of the array's elements,
   * enclosed in curly braces ({@code "{}"}). Adjacent elements are separated by the characters
   * {@code ", "} (a comma followed by a space). Returns a {@code "null"} String if {@code array} is
   * {@code null}.
   *
   * @param array the array to build a String representation for
   * @return a String representation of {@code array}
   */
  public static String nullSafeToString(@Nullable boolean[] array) {
    if (array == null) {
      return NULL_STRING;
    }
    int length = array.length;
    if (length == 0) {
      return EMPTY_ARRAY;
    }
    StringJoiner stringJoiner = new StringJoiner(ARRAY_ELEMENT_SEPARATOR, ARRAY_START, ARRAY_END);
    for (boolean b : array) {
      stringJoiner.add(String.valueOf(b));
    }
    return stringJoiner.toString();
  }

  /**
   * Return a String representation of the contents of the specified array.
   * <p>The String representation consists of a list of the array's elements,
   * enclosed in curly braces ({@code "{}"}). Adjacent elements are separated by the characters
   * {@code ", "} (a comma followed by a space). Returns a {@code "null"} String if {@code array} is
   * {@code null}.
   *
   * @param array the array to build a String representation for
   * @return a String representation of {@code array}
   */
  public static String nullSafeToString(@Nullable byte[] array) {
    if (array == null) {
      return NULL_STRING;
    }
    int length = array.length;
    if (length == 0) {
      return EMPTY_ARRAY;
    }
    StringJoiner stringJoiner = new StringJoiner(ARRAY_ELEMENT_SEPARATOR, ARRAY_START, ARRAY_END);
    for (byte b : array) {
      stringJoiner.add(String.valueOf(b));
    }
    return stringJoiner.toString();
  }

  /**
   * Return a String representation of the contents of the specified array.
   * <p>The String representation consists of a list of the array's elements,
   * enclosed in curly braces ({@code "{}"}). Adjacent elements are separated by the characters
   * {@code ", "} (a comma followed by a space). Returns a {@code "null"} String if {@code array} is
   * {@code null}.
   *
   * @param array the array to build a String representation for
   * @return a String representation of {@code array}
   */
  public static String nullSafeToString(@Nullable char[] array) {
    if (array == null) {
      return NULL_STRING;
    }
    int length = array.length;
    if (length == 0) {
      return EMPTY_ARRAY;
    }
    StringJoiner stringJoiner = new StringJoiner(ARRAY_ELEMENT_SEPARATOR, ARRAY_START, ARRAY_END);
    for (char c : array) {
      stringJoiner.add('\'' + String.valueOf(c) + '\'');
    }
    return stringJoiner.toString();
  }

  /**
   * Return a String representation of the contents of the specified array.
   * <p>The String representation consists of a list of the array's elements,
   * enclosed in curly braces ({@code "{}"}). Adjacent elements are separated by the characters
   * {@code ", "} (a comma followed by a space). Returns a {@code "null"} String if {@code array} is
   * {@code null}.
   *
   * @param array the array to build a String representation for
   * @return a String representation of {@code array}
   */
  public static String nullSafeToString(@Nullable double[] array) {
    if (array == null) {
      return NULL_STRING;
    }
    int length = array.length;
    if (length == 0) {
      return EMPTY_ARRAY;
    }
    StringJoiner stringJoiner = new StringJoiner(ARRAY_ELEMENT_SEPARATOR, ARRAY_START, ARRAY_END);
    for (double d : array) {
      stringJoiner.add(String.valueOf(d));
    }
    return stringJoiner.toString();
  }

  /**
   * Return a String representation of the contents of the specified array.
   * <p>The String representation consists of a list of the array's elements,
   * enclosed in curly braces ({@code "{}"}). Adjacent elements are separated by the characters
   * {@code ", "} (a comma followed by a space). Returns a {@code "null"} String if {@code array} is
   * {@code null}.
   *
   * @param array the array to build a String representation for
   * @return a String representation of {@code array}
   */
  public static String nullSafeToString(@Nullable float[] array) {
    if (array == null) {
      return NULL_STRING;
    }
    int length = array.length;
    if (length == 0) {
      return EMPTY_ARRAY;
    }
    StringJoiner stringJoiner = new StringJoiner(ARRAY_ELEMENT_SEPARATOR, ARRAY_START, ARRAY_END);
    for (float f : array) {
      stringJoiner.add(String.valueOf(f));
    }
    return stringJoiner.toString();
  }

  /**
   * Return a String representation of the contents of the specified array.
   * <p>The String representation consists of a list of the array's elements,
   * enclosed in curly braces ({@code "{}"}). Adjacent elements are separated by the characters
   * {@code ", "} (a comma followed by a space). Returns a {@code "null"} String if {@code array} is
   * {@code null}.
   *
   * @param array the array to build a String representation for
   * @return a String representation of {@code array}
   */
  public static String nullSafeToString(@Nullable int[] array) {
    if (array == null) {
      return NULL_STRING;
    }
    int length = array.length;
    if (length == 0) {
      return EMPTY_ARRAY;
    }
    StringJoiner stringJoiner = new StringJoiner(ARRAY_ELEMENT_SEPARATOR, ARRAY_START, ARRAY_END);
    for (int i : array) {
      stringJoiner.add(String.valueOf(i));
    }
    return stringJoiner.toString();
  }

  /**
   * Return a String representation of the contents of the specified array.
   * <p>The String representation consists of a list of the array's elements,
   * enclosed in curly braces ({@code "{}"}). Adjacent elements are separated by the characters
   * {@code ", "} (a comma followed by a space). Returns a {@code "null"} String if {@code array} is
   * {@code null}.
   *
   * @param array the array to build a String representation for
   * @return a String representation of {@code array}
   */
  public static String nullSafeToString(@Nullable long[] array) {
    if (array == null) {
      return NULL_STRING;
    }
    int length = array.length;
    if (length == 0) {
      return EMPTY_ARRAY;
    }
    StringJoiner stringJoiner = new StringJoiner(ARRAY_ELEMENT_SEPARATOR, ARRAY_START, ARRAY_END);
    for (long l : array) {
      stringJoiner.add(String.valueOf(l));
    }
    return stringJoiner.toString();
  }

  /**
   * Return a String representation of the contents of the specified array.
   * <p>The String representation consists of a list of the array's elements,
   * enclosed in curly braces ({@code "{}"}). Adjacent elements are separated by the characters
   * {@code ", "} (a comma followed by a space). Returns a {@code "null"} String if {@code array} is
   * {@code null}.
   *
   * @param array the array to build a String representation for
   * @return a String representation of {@code array}
   */
  public static String nullSafeToString(@Nullable short[] array) {
    if (array == null) {
      return NULL_STRING;
    }
    int length = array.length;
    if (length == 0) {
      return EMPTY_ARRAY;
    }
    StringJoiner stringJoiner = new StringJoiner(ARRAY_ELEMENT_SEPARATOR, ARRAY_START, ARRAY_END);
    for (short s : array) {
      stringJoiner.add(String.valueOf(s));
    }
    return stringJoiner.toString();
  }

  public static Long pidSafe(Long pid) {
    return pidSafe(pid, DEFAULT_ROOT_PID);
  }

  public static Long pidSafe(Long pid, Long safePid) {
    return isNull(pid) || pid < 1 ? safePid : pid;
  }

  public static List<List<String>> arrayToLists(String[] values) {
    if (isEmpty(values)) {
      return null;
    }
    List<List<String>> lists = new ArrayList<>();
    for (String value : values) {
      lists.add(List.of(value));
    }
    return lists;
  }

  /**
   * Gets the first non null data value in the map.
   */
  public static <K, V> V getFirstNotNull(Map<K, V> map) {
    V obj = null;
    for (Entry<K, V> entry : map.entrySet()) {
      obj = entry.getValue();
      if (obj != null) {
        break;
      }
    }
    return obj;
  }

  public static <T> List<T> emptyListIfNull(List<T> list) {
    return isNull(list) ? new ArrayList<>() : list;
  }

  public static boolean localDateTimeEqualsInFormat(LocalDateTime d1, LocalDateTime d2) {
    if (d1 == null && d2 == null) {
      return true;
    }
    if (d1 == null || d2 == null) {
      return false;
    }
    return d1.getYear() == d2.getYear() &&
        d1.getMonth() == d2.getMonth() &&
        d1.getDayOfYear() == d2.getDayOfYear() &&
        d1.getHour() == d2.getHour() &&
        d1.getMinute() == d2.getMinute() &&
        d1.getSecond() == d2.getSecond();
  }

  public static <T> LinkedHashSet<T> linkedSetOf(T... t) {
    if (isEmpty(t)) {
      return null;
    }
    return new LinkedHashSet<T>(Arrays.asList(t));
  }

  public static <T> LinkedHashSet<T> linkedSetOf(Collection<T> t) {
    if (isEmpty(t)) {
      return null;
    }
    return new LinkedHashSet<T>(t);
  }

  public static <K, T> boolean mapEquals(Map<K, T> map1, Map<K, T> map2) {
    if (map1 == null && map2 == null) {
      return true;
    }
    if ((map1 == null && map2.isEmpty()) || (map2 == null && map1.isEmpty())) {
      return true;
    }
    if (map1 == null || map2 == null) {
      return false;
    }
    for (Entry<K, T> entry : map1.entrySet()) {
      K key = entry.getKey();
      T value1 = entry.getValue();
      T value2 = map2.get(key);
      if (!Objects.equals(value1, value2)) {
        return false;
      }
    }
    return true;
  }

  public static <K, T> boolean identityMapEquals(IdentityHashMap<K, T> map1,
      IdentityHashMap<K, T> map2) {
    if (map1 == null && map2 == null) {
      return true;
    }
    if (map1 == null || map2 == null) {
      return false;
    }
    if (map1.size() != map2.size()) {
      return false;
    }

    for (Entry<K, T> entry1 : map1.entrySet()) {
      boolean hasKey1 = false;
      for (Entry<K, T> entry2 : map2.entrySet()) {
        if (entry1.getKey().equals(entry2.getKey())) {
          hasKey1 = true;
          if (!Objects.equals(entry1.getValue(), entry2.getValue())) {
            return false;
          }
        }
      }
      if (!hasKey1) {
        return false;
      }
    }
    return true;
  }

  public static <V> boolean listEquals(List<V> c1, List<V> c2) {
    if (c1 == null && c2 == null) {
      return true;
    }
    if ((c1 == null && c2.isEmpty()) || (c2 == null && c1.isEmpty())) {
      return true;
    }
    if (c1 == null || c2 == null) {
      return false;
    }

    for (int i = 0; i < c1.size(); i++) {
      V value1 = c1.get(i);
      V value2 = c2.get(i);
      if (!Objects.equals(value1, value2)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Get Duplicate data.
   *
   * @return Duplicate data
   */
  public static <T> List<T> duplicate(Collection<T> values) {
    if (values == null || values.size() <= 0) {
      return Collections.emptyList();
    }
    return values.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()))
        .entrySet().stream().filter(e -> e.getValue() > 1)
        .map(Entry::getKey).collect(Collectors.toList());
  }

  public static String formatMessage(String template, Object... args) {
    if (args == null) {
      return template;
    } else {
      template = String.valueOf(template);
      StringBuilder builder = new StringBuilder(template.length() + 16 * args.length);
      int tStart = 0, i, phStart;
      for (i = 0; i < args.length; tStart = phStart + 2) {
        phStart = template.indexOf("{}", tStart);
        if (phStart == -1) {
          break;
        }
        builder.append(template.substring(tStart, phStart));
        builder.append(args[i++]);
      }
      builder.append(template.substring(tStart));
      if (i < args.length) {
        builder.append(" [");
        builder.append(args[i++]);
        while (i < args.length) {
          builder.append(", ");
          builder.append(args[i++]);
        }
        builder.append(']');
      }
      return builder.toString();
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T convert(Object value, Class<T> clazz) {
    if (isNull(value)) {
      return null;
    }
    try {
      //is enum
      if (clazz.isEnum()) {
        return (T) Enum.valueOf((Class<Enum>) clazz, (String) value);
      }
      //other type
      if (String.class.equals(clazz)) {
        return (T) value.toString();
      } else if (Character.class.equals(clazz)) {
        return (T) value.toString();
      } else if (Double.class.equals(clazz)) {
        return (T) Double.valueOf(value.toString());
      } else if (Float.class.equals(clazz)) {
        return (T) Float.valueOf(value.toString());
      } else if (Long.class.equals(clazz)) {
        return (T) Long.valueOf(value.toString());
      } else if (Integer.class.equals(clazz)) {
        return (T) Integer.valueOf(value.toString());
      } else if (Short.class.equals(clazz)) {
        return (T) Short.valueOf(value.toString());
      } else if (Byte.class.equals(clazz)) {
        return (T) Byte.valueOf(value.toString());
      } else if (Boolean.class.equals(clazz)) {
        return (T) BooleanUtils.toBooleanObject(value.toString());
      } else if (BigDecimal.class.equals(clazz)) {
        return (T) BigDecimal.valueOf(Double.parseDouble(value.toString()));
      } else if (LocalDateTime.class.equals(clazz)) {
        Timestamp time = (Timestamp) value;
        return (T) time.toLocalDateTime();
      } else if (Date.class.equals(clazz)) {
        return (T) DateUtils.parseDate(value.toString(), DEFAULT_DATE_TIME_FORMAT);
      } else {
        // Note: Inconsistent with jpa objectMapper.
        return JsonUtils.fromJson(value.toString(), clazz);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static boolean isBasicType(Object object) {
    Class className = object.getClass();
    return className.equals(Integer.class) ||
        className.equals(Byte.class) ||
        className.equals(Long.class) ||
        className.equals(Double.class) ||
        className.equals(Float.class) ||
        className.equals(Character.class) ||
        className.equals(Short.class) ||
        className.equals(Boolean.class);
  }

  public static boolean isTrueValue(String value) {
    if (isBlank(value)) {
      return false;
    }
    return value.equalsIgnoreCase("true") || value.equals("1")
        || value.equalsIgnoreCase("yes") || value.equals("y")
        || value.equals("是") || value.equals("真");
  }

  @SneakyThrows
  public static URI appendParameter(String uri, String paramName, String paramValue) {
    return appendParameter(new URI(uri), paramName, paramValue);
  }

  @SneakyThrows
  public static URI appendParameter(URI uri, String paramName, String paramValue) {
    String originalQuery = uri.getQuery();
    String additionalQuery = paramName + "=" + URLEncoder.encode(paramValue, UTF_8);
    String newQuery =
        originalQuery != null ? originalQuery + "&" + additionalQuery : additionalQuery;
    return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), newQuery,
        uri.getFragment());
  }

  public static String cleanUrl(String url) {
    String result = null;
    try {
      result = url.replaceAll("\\{", "%7B").
          replaceAll("\\}", "%7D").
          replaceAll(" ", "%20");
    } catch (Exception t) {
      t.printStackTrace();
    }
    return result;
  }

  public static String getCauseMessage(Exception e) {
    return nonNull(e.getCause()) && isNotEmpty(e.getCause().getMessage())
        ? e.getCause().getMessage() : e.getMessage();
  }

  public static byte[] decodeToBytes(String content, String encoding) throws IOException {
    if (isEmpty(content)) {
      return new byte[0];
    }
    if (BASE64.equalsIgnoreCase(encoding)) {
      return Base64Utils.decode(content);
    } else if (GZIP_BASE64.equalsIgnoreCase(encoding)) {
      return GzipUtils.decompress(Base64Utils.decode(content));
    }
    return content.getBytes();
  }

  public static String encodeFromBytes(byte[] content, String encoding) throws IOException {
    if (isEmpty(content)) {
      return null;
    }
    if (BASE64.equalsIgnoreCase(encoding)) {
      return Base64Utils.encode(content);
    } else if (GZIP_BASE64.equalsIgnoreCase(encoding)) {
      return Base64Utils.encode(GzipUtils.compress(content));
    }
    return new String(content, UTF_8);
  }

  /**
   * This is <em>almost</em> equivalent to the {@link String#split(String)} method in JDK 1.4. It is
   * here to enable us to support earlier JDKs.
   * <p>
   * Note that unlike JDK1.4 split(), it optionally ignores leading split Characters, and the
   * splitChar parameter is not a Regular expression
   * <p>
   * This piece of code used to be part of JMeterUtils, but was moved here because some JOrphan
   * classes use it too.
   *
   * @param splittee  String to be split
   * @param splitChar Character(s) to split the string on, these are treated as a single unit
   * @param truncate  Should adjacent and leading/trailing splitChars be removed?
   * @return Array of all the tokens; empty if the input string is {@code null} or the splitChar is
   * {@code null}
   * @see #split(String, String, String)
   */
  public static String[] split(String splittee, String splitChar, boolean truncate) { //NOSONAR
    if (splittee == null || splitChar == null) {
      return new String[0];
    }
    final String EMPTY_ELEMENT = "";
    int spot;
    final int splitLength = splitChar.length();
    final String adjacentSplit = splitChar + splitChar;
    final int adjacentSplitLength = adjacentSplit.length();
    if (truncate) {
      while ((spot = splittee.indexOf(adjacentSplit)) != -1) {
        splittee = splittee.substring(0, spot + splitLength)
            + splittee.substring(spot + adjacentSplitLength, splittee.length());
      }
      if (splittee.startsWith(splitChar)) {
        splittee = splittee.substring(splitLength);
      }
      if (splittee.endsWith(splitChar)) { // Remove trailing splitter
        splittee = splittee.substring(0, splittee.length() - splitLength);
      }
    }
    List<String> returns = new ArrayList<>();
    final int length = splittee.length(); // This is the new length
    int start = 0;
    spot = 0;
    while (start < length && (spot = splittee.indexOf(splitChar, start)) > -1) {
      if (spot > 0) {
        returns.add(splittee.substring(start, spot));
      } else {
        returns.add(EMPTY_ELEMENT);
      }
      start = spot + splitLength;
    }
    if (start < length) {
      returns.add(splittee.substring(start));
    } else if (spot == length - splitLength) {// Found splitChar at end of line
      returns.add(EMPTY_ELEMENT);
    }
    return returns.toArray(new String[returns.size()]);
  }

  public static String[] split(String splittee, String splitChar) {
    return split(splittee, splitChar, true);
  }

  /**
   * Takes a String and a tokenizer character string, and returns a new array of strings of the
   * string split by the tokenizer character(s).
   * <p>
   * Trailing delimiters are significant (unless the default = null)
   *
   * @param splittee String to be split.
   * @param delims   Delimiter character(s) to split the string on
   * @param def      Default value to place between two split chars that have nothing between them.
   *                 If null, then ignore omitted elements.
   * @return Array of all the tokens.
   * @throws NullPointerException if splittee or delims are {@code null}
   * @see #split(String, String, boolean)
   * @see #split(String, String)
   * <p>
   * This is a rewritten version of JMeterUtils.split()
   */
  public static String[] split(String splittee, String delims, String def) {
    StringTokenizer tokens = new StringTokenizer(splittee, delims, def != null);
    boolean lastWasDelim = false;
    List<String> strList = new ArrayList<>();
    while (tokens.hasMoreTokens()) {
      String tok = tokens.nextToken();
      if (tok.length() == 1 // we have a single character; could be a token
          && delims.contains(tok)) // it is a token
      {
        if (lastWasDelim) {// we saw a delimiter last time
          strList.add(def);// so add the default
        }
        lastWasDelim = true;
      } else {
        lastWasDelim = false;
        strList.add(tok);
      }
    }
    if (lastWasDelim) {
      strList.add(def);
    }
    return strList.toArray(new String[strList.size()]);
  }


  private static final char[] SPACES_CHARS = "                                 ".toCharArray();
  private static final int SPACES_LEN = SPACES_CHARS.length;

  /**
   * Right aligns some text in a StringBuilder N.B. modifies the input builder
   *
   * @param in  StringBuilder containing some text
   * @param len output length desired
   * @return input StringBuilder, with leading spaces
   */
  public static StringBuilder rightAlign(StringBuilder in, int len) {
    int pfx = len - in.length();
    if (pfx <= 0) {
      return in;
    }
    if (pfx > SPACES_LEN) {
      pfx = SPACES_LEN;
    }
    in.insert(0, SPACES_CHARS, 0, pfx);
    return in;
  }

  /**
   * Left aligns some text in a StringBuilder N.B. modifies the input builder
   *
   * @param in  StringBuilder containing some text
   * @param len output length desired
   * @return input StringBuilder, with trailing spaces
   */
  public static StringBuilder leftAlign(StringBuilder in, int len) {
    int sfx = len - in.length();
    if (sfx <= 0) {
      return in;
    }
    if (sfx > SPACES_LEN) {
      sfx = SPACES_LEN;
    }
    in.append(SPACES_CHARS, 0, sfx);
    return in;
  }

  /**
   * Convert a boolean to its upper case string representation. Equivalent to
   * Boolean.valueOf(boolean).toString().toUpperCase().
   *
   * @param value boolean to convert
   * @return "TRUE" or "FALSE"
   */
  public static String booleanToSTRING(boolean value) {
    return value ? "TRUE" : "FALSE";
  }

  /**
   * Simple-minded String.replace() for JDK1.3 Should probably be recoded...
   *
   * @param source  input string
   * @param search  string to look for (no regular expressions)
   * @param replace string to replace the search string
   * @return the output string
   */
  public static String replaceFirst(String source, String search, String replace) {
    int start = source.indexOf(search);
    int len = search.length();
    if (start == -1) {
      return source;
    }
    if (start == 0) {
      return replace + source.substring(len);
    }
    return source.substring(0, start) + replace + source.substring(start + len);
  }

  /**
   * Version of String.replaceAll() for JDK1.3 See below for another version which replaces strings
   * rather than chars and provides a fast path which does not allocate memory
   *
   * @param source  input string
   * @param search  char to look for (no regular expressions)
   * @param replace string to replace the search string
   * @return the output string
   */
  public static String replaceAllChars(String source, char search, String replace) {
    int indexOf = source.indexOf(search);
    if (indexOf == -1) {
      return source;
    }

    int offset = 0;
    char[] chars = source.toCharArray();
    StringBuilder sb = new StringBuilder(source.length() + 20);
    while (indexOf != -1) {
      sb.append(chars, offset, indexOf - offset);
      sb.append(replace);
      offset = indexOf + 1;
      indexOf = source.indexOf(search, offset);
    }
    sb.append(chars, offset, chars.length - offset);

    return sb.toString();
  }

  /**
   * Replace all patterns in a String
   *
   * @param input   - string to be transformed
   * @param pattern - pattern to replace
   * @param sub     - replacement
   * @return the updated string
   * @see String#replaceAll(String regex, String replacement) - JDK1.4 only
   */
  public static String substitute(final String input, final String pattern, final String sub) {
    StringBuilder ret = new StringBuilder(input.length());
    int start = 0;
    int index = -1;
    final int length = pattern.length();
    while ((index = input.indexOf(pattern, start)) >= start) {
      ret.append(input.substring(start, index));
      ret.append(sub);
      start = index + length;
    }
    ret.append(input.substring(start));
    return ret.toString();
  }

  /**
   * Trim a string by the tokens provided.
   *
   * @param input  string to trim
   * @param delims list of delimiters
   * @return input trimmed at the first delimiter
   */
  public static String trim(final String input, final String delims) {
    StringTokenizer tokens = new StringTokenizer(input, delims);
    return tokens.hasMoreTokens() ? tokens.nextToken() : "";
  }

  /**
   * Returns a slice of a byte array.
   * <p>
   * TODO - add bounds checking?
   *
   * @param array input array
   * @param begin start of slice
   * @param end   end of slice
   * @return slice from the input array
   */
  public static byte[] getByteArraySlice(byte[] array, int begin, int end) {
    byte[] slice = new byte[end - begin + 1];
    System.arraycopy(array, begin, slice, 0, slice.length);
    return slice;
  }

  // N.B. Commons IO IOUtils has equivalent methods; these were added before IO was included
  // TODO - perhaps deprecate these in favour of Commons IO?

  /**
   * Close a Closeable with no error thrown
   *
   * @param cl - Closeable (may be null)
   */
  public static void closeQuietly(Closeable cl) {
    try {
      if (cl != null) {
        cl.close();
      }
    } catch (IOException ignored) {
      // NOOP
    }
  }

  /**
   * close a Socket with no error thrown
   *
   * @param sock - Socket (may be null)
   */
  public static void closeQuietly(Socket sock) {
    try {
      if (sock != null) {
        sock.close();
      }
    } catch (IOException ignored) {
      // NOOP
    }
  }

  /**
   * close a Socket with no error thrown
   *
   * @param sock - ServerSocket (may be null)
   */
  public static void closeQuietly(ServerSocket sock) {
    try {
      if (sock != null) {
        sock.close();
      }
    } catch (IOException ignored) {
      // NOOP
    }
  }

  /**
   * Check if a byte array starts with the given byte array.
   *
   * @param target array to scan
   * @param search array to search for
   * @param offset starting offset (&ge;0)
   * @return true if the search array matches the target at the current offset
   * @see String#startsWith(String, int)
   */
  public static boolean startsWith(byte[] target, byte[] search, int offset) {
    final int targetLength = target.length;
    final int searchLength = search.length;
    if (offset < 0 || searchLength > targetLength + offset) {
      return false;
    }
    for (int i = 0; i < searchLength; i++) {
      if (target[i + offset] != search[i]) {
        return false;
      }
    }
    return true;
  }

  private static final byte[] XML_PFX = {'<', '?', 'x', 'm', 'l'};// "<?xml "

  /**
   * Detects if some content starts with the standard XML prefix.
   *
   * @param target the content to check
   * @return true if the document starts with the standard XML prefix.
   */
  public static boolean isXML(byte[] target) {
    return startsWith(target, XML_PFX, 0);
  }

  /**
   * Convert binary byte array to hex string.
   *
   * @param ba input binary byte array
   * @return hex representation of binary input
   */
  public static String baToHexString(byte[] ba) {
    StringBuilder sb = new StringBuilder(ba.length * 2);
    for (byte b : ba) {
      int j = b & 0xff;
      if (j < 16) {
        sb.append('0'); // $NON-NLS-1$ add zero padding
      }
      sb.append(Integer.toHexString(j));
    }
    return sb.toString();
  }

  /**
   * Convert binary byte array to hex string.
   *
   * @param ba        input binary byte array
   * @param separator the separator to be added between pairs of hex digits
   * @return hex representation of binary input
   */
  public static String baToHexString(byte[] ba, char separator) {
    StringBuilder sb = new StringBuilder(ba.length * 2);
    for (int i = 0; i < ba.length; i++) {
      if (i > 0 && separator != 0) {
        sb.append(separator);
      }
      int j = ba[i] & 0xff;
      if (j < 16) {
        sb.append('0'); // $NON-NLS-1$ add zero padding
      }
      sb.append(Integer.toHexString(j));
    }
    return sb.toString();
  }

  /**
   * Convert binary byte array to hex string.
   *
   * @param ba input binary byte array
   * @return hex representation of binary input
   */
  public static byte[] baToHexBytes(byte[] ba) {
    byte[] hb = new byte[ba.length * 2];
    for (int i = 0; i < ba.length; i++) {
      byte upper = (byte) ((ba[i] & 0xf0) >> 4);
      byte lower = (byte) (ba[i] & 0x0f);
      hb[2 * i] = toHexChar(upper);
      hb[2 * i + 1] = toHexChar(lower);
    }
    return hb;
  }

  private static byte toHexChar(byte in) {
    if (in < 10) {
      return (byte) (in + '0');
    }
    return (byte) ((in - 10) + 'a');
  }

  /**
   * Read as much as possible into buffer.
   *
   * @param is     the stream to read from
   * @param buffer output buffer
   * @param offset offset into buffer
   * @param length number of bytes to read
   * @return the number of bytes actually read
   * @throws IOException if some I/O errors occur
   */
  public static int read(InputStream is, byte[] buffer, int offset, int length) throws IOException {
    int remaining = length;
    while (remaining > 0) {
      int location = length - remaining;
      int count = is.read(buffer, location, remaining);
      if (-1 == count) { // EOF
        break;
      }
      remaining -= count;
    }
    return length - remaining;
  }

  /**
   * Display currently running threads on system.out This may be expensive to run. Mainly designed
   * for use at the end of a non-GUI test to check for threads that might prevent the JVM from
   * exiting.
   *
   * @param includeDaemons whether to include daemon threads or not.
   */
  public static void displayThreads(boolean includeDaemons) {
    Map<Thread, StackTraceElement[]> m = Thread.getAllStackTraces();
    String lineSeparator = System.getProperty("line.separator");
    for (Entry<Thread, StackTraceElement[]> e : m.entrySet()) {
      boolean daemon = e.getKey().isDaemon();
      if (includeDaemons || !daemon) {
        StringBuilder builder = new StringBuilder();
        StackTraceElement[] ste = e.getValue();
        for (StackTraceElement stackTraceElement : ste) {
          int lineNumber = stackTraceElement.getLineNumber();
          builder.append(stackTraceElement.getClassName())
              .append("#")
              .append(stackTraceElement.getMethodName())
              .append(lineNumber >= 0 ? " at line:" + stackTraceElement.getLineNumber() : "")
              .append(lineSeparator);
        }
        System.out.println(e.getKey().toString() + (daemon ? " (daemon)" : "") + ", stackTrace:"
            + builder.toString());
      }
    }
  }

  /**
   * Returns {@code null} if input is empty, {@code null} or contains spaces only
   *
   * @param input String
   * @return trimmed input or {@code null}
   */
  public static String nullifyIfEmptyTrimmed(final String input) {
    if (input == null) {
      return null;
    }
    String trimmed = input.trim();
    if (trimmed.length() == 0) {
      return null;
    }
    return trimmed;
  }

  /**
   * Check that value is empty (""), {@code null} or whitespace only.
   *
   * @param value Value
   * @return {@code true} if the String is not empty (""), not {@code null} and not whitespace only.
   */
  public static boolean isBlank(final String value) {
    return StringUtils.isBlank(value);
  }

  /**
   * Write data to an output stream in chunks with a maximum size of 4K. This is to avoid
   * OutOfMemory issues if the data buffer is very large and the JVM needs to copy the buffer for
   * use by native code.
   *
   * @param data   the buffer to be written
   * @param output the output stream to use
   * @throws IOException if there is a problem writing the data
   */
  // Bugzilla 54990
  public static void write(byte[] data, OutputStream output) throws IOException {
    int bytes = data.length;
    int offset = 0;
    while (bytes > 0) {
      int chunk = Math.min(bytes, DEFAULT_CHUNK_SIZE);
      output.write(data, offset, chunk);
      bytes -= chunk;
      offset += chunk;
    }
  }

  /**
   * Returns duration formatted with format HH:mm:ss.
   *
   * @param elapsedSec long elapsed time in seconds
   * @return String formatted with format HH:mm:ss
   */
  @SuppressWarnings("boxing")
  public static String formatDuration(long elapsedSec) {
    return String.format("%02d:%02d:%02d",
        elapsedSec / 3600, (elapsedSec % 3600) / 60, elapsedSec % 60);
  }

  /**
   * Check whether we can write to a folder. A folder can be written to if if does not contain any
   * file or folder Throw {@link IllegalArgumentException} if folder cannot be written to either:
   * <ul>
   *  <li>Because it exists but is not a folder</li>
   *  <li>Because it exists but is not empty</li>
   *  <li>Because it does not exist but cannot be created</li>
   * </ul>
   *
   * @param folder to check
   * @throws IllegalArgumentException when folder can't be written to
   */
  public static void canSafelyWriteToFolder(File folder) {
    canSafelyWriteToFolder(folder, false, file -> true);
  }


  /**
   * Check whether we can write to a folder. A folder can be written to if
   * folder.listFiles(exporterFileFilter) does not return any file or folder. Throw
   * {@link IllegalArgumentException} if folder cannot be written to either:
   * <ul>
   *  <li>Because it exists but is not a folder</li>
   *  <li>Because it exists but is not empty using folder.listFiles(exporterFileFilter)</li>
   *  <li>Because it does not exist but cannot be created</li>
   * </ul>
   *
   * @param folder     to check
   * @param fileFilter used to filter listing of folder
   * @throws IllegalArgumentException when folder can't be written to
   */
  public static void canSafelyWriteToFolder(File folder, FileFilter fileFilter) {
    canSafelyWriteToFolder(folder, false, fileFilter);
  }

  /**
   * Check whether we can write to a folder. If {@code deleteFolderContent} is {@code true} the
   * folder or file with the same name will be emptied or deleted.
   *
   * @param folder              to check
   * @param deleteFolderContent flag whether the folder should be emptied or a file with the same
   *                            name deleted
   * @throws IllegalArgumentException when folder can't be written to Throw IllegalArgumentException
   *                                  if folder cannot be written
   */
  public static void canSafelyWriteToFolder(File folder, boolean deleteFolderContent) {
    canSafelyWriteToFolder(folder, deleteFolderContent, file -> true);
  }


  /**
   * Check whether we can write to a folder.
   *
   * @param folder               which should be checked for writability and emptiness
   * @param deleteFolderIfExists flag whether the folder should be emptied or a file with the same
   *                             name deleted
   * @param exporterFileFilter   used for filtering listing of the folder
   * @throws IllegalArgumentException when folder can't be written to. That could have the following
   *                                  reasons:
   *                                  <ul>
   *                                   <li>it exists but is not a folder</li>
   *                                   <li>it exists but is not empty</li>
   *                                   <li>it does not exist but cannot be created</li>
   *                                  </ul>
   */
  public static void canSafelyWriteToFolder(File folder, boolean deleteFolderIfExists,
      FileFilter exporterFileFilter) {
    if (folder.exists()) {
      if (folder.isFile()) {
        if (deleteFolderIfExists) {
          if (!folder.delete()) {
            throw new IllegalArgumentException("Cannot write to '"
                + folder.getAbsolutePath() + "' as it is an existing file and delete failed");
          }
        } else {
          throw new IllegalArgumentException("Cannot write to '"
              + folder.getAbsolutePath() + "' as it is an existing file");
        }
      } else {
        File[] listedFiles = folder.listFiles(exporterFileFilter);
        if (listedFiles != null && listedFiles.length > 0) {
          if (deleteFolderIfExists) {
            try {
              FileUtils.deleteDirectory(folder);
            } catch (IOException ex) {
              throw new IllegalArgumentException("Cannot write to '" + folder.getAbsolutePath()
                  + "' as folder is not empty and cleanup failed with error:" + ex.getMessage(),
                  ex);
            }
            if (!folder.mkdir()) {
              throw new IllegalArgumentException(
                  "Cannot create folder " + folder.getAbsolutePath());
            }
          } else {
            throw new IllegalArgumentException("Cannot write to '"
                + folder.getAbsolutePath() + "' as folder is not empty");
          }
        }
      }
    } else {
      // check we can create it
      if (!folder.getAbsoluteFile().getParentFile().canWrite()) {
        throw new IllegalArgumentException("Cannot write to '"
            + folder.getAbsolutePath()
            + "' as folder does not exist and parent folder is not writable");
      }
    }
  }

  /**
   * Replace in source all matches of regex by replacement taking into account case if caseSensitive
   * is true
   *
   * @param source        Source text
   * @param regex         Regular expression
   * @param replacement   Replacement text to which function applies a quoting
   * @param caseSensitive is case taken into account
   * @return array of Object where first row is the replaced text, second row is the number of
   * replacement that occurred
   */
  @SuppressWarnings("JdkObsolete")
  public static Object[] replaceAllWithRegex(
      String source, String regex, String replacement, boolean caseSensitive) {
    java.util.regex.Pattern pattern = caseSensitive ?
        java.util.regex.Pattern.compile(regex) :
        java.util.regex.Pattern.compile(regex, java.util.regex.Pattern.CASE_INSENSITIVE);
    final String replacementQuoted = Matcher.quoteReplacement(replacement);
    Matcher matcher = pattern.matcher(source);
    int totalReplaced = 0;
    // Can be replaced with StringBuilder for Java 9+
    StringBuffer result = new StringBuffer(); // NOSONAR Matcher#appendReplacement needs a StringBuffer
    while (matcher.find()) {
      matcher.appendReplacement(result, replacementQuoted);
      totalReplaced++;
    }
    matcher.appendTail(result);

    return new Object[]{
        result.toString(),
        totalReplaced
    };
  }

  /**
   * Replace all occurrences of {@code regex} in {@code value} by {@code replaceBy} if {@code value}
   * is not blank. The replaced text is fed into the {@code setter}.
   *
   * @param regex         Regular expression that is used for the search
   * @param replaceBy     value that is used for replacement
   * @param caseSensitive flag whether the regex should be applied case sensitive
   * @param value         in which the replacement takes place
   * @param setter        that gets called with the replaced value
   * @return number of matches that were replaced
   */
  public static int replaceValue(String regex, String replaceBy, boolean caseSensitive,
      String value, Consumer<String> setter) {
    if (StringUtils.isBlank(value)) {
      return 0;
    }
    Object[] result = replaceAllWithRegex(value, regex, replaceBy, caseSensitive);
    int nbReplaced = (Integer) result[1];
    if (nbReplaced <= 0) {
      return 0;
    }
    setter.accept((String) result[0]);
    return nbReplaced;
  }

  /**
   * Takes an array of strings and a tokenizer character, and returns a string of all the strings
   * concatenated with the tokenizer string in between each one.
   *
   * @param splittee  Array of Objects to be concatenated.
   * @param splitChar Object to unsplit the strings with.
   * @return Array of all the tokens.
   */
  public static String unsplit(Object[] splittee, Object splitChar) {
    StringBuilder retVal = new StringBuilder();
    int count = -1;
    while (++count < splittee.length) {
      if (splittee[count] != null) {
        retVal.append(splittee[count]);
      }
      if (count + 1 < splittee.length && splittee[count + 1] != null) {
        retVal.append(splitChar);
      }
    }
    return retVal.toString();
  }

  public static String getFileNameInUrl(URL url) {
    String fileName = url.getFile();

    int lastIndex = fileName.lastIndexOf('/');
    if (lastIndex != -1) {
      fileName = fileName.substring(lastIndex + 1);
    }
    return fileName;
  }

  public static String getFileNameInUrl(String url) throws MalformedURLException {
    String fileName = new URL(url).getFile();

    int lastIndex = fileName.lastIndexOf('/');
    if (lastIndex != -1) {
      fileName = fileName.substring(lastIndex + 1);
    }
    return fileName;
  }

  public static double calcRate(long part, long total) {
    if (total == 0) {
      return 0;
    }
    double rate = (double) part / total;
    return formatDouble(rate * 100, "0.00");
  }

  public static double calcRate(double part, double total) {
    if (total == 0) {
      return 0;
    }
    double rate = part / total;
    return formatDouble(rate * 100, "0.00");
  }

  public static double formatDouble(double rate, String pattern) {
    DecimalFormat df = new DecimalFormat(pattern);
    return Double.parseDouble(df.format(rate));
  }

  public static BigDecimal formatDecimal(double rate, String pattern) {
    DecimalFormat df = new DecimalFormat(pattern);
    return BigDecimal.valueOf(Double.parseDouble(df.format(rate)));
  }

  public static Map<String, String> convertToMap(Properties properties) {
    Map<String, String> map = new HashMap<>();
    for (String key : properties.stringPropertyNames()) {
      String value = properties.getProperty(key);
      map.put(key, value);
    }
    return map;
  }
}
