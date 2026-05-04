package cloud.xcan.angus.spec.thread;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Per-thread (and child-thread inheritable) string-keyed context using
 * {@link InheritableThreadLocal}.
 * <p>
 * Child threads receive the <strong>same map reference</strong> as the parent at creation time (JDK
 * default {@link InheritableThreadLocal} behavior). Do not mutate that map concurrently from parent
 * and child without external synchronization or use a concurrent map via {@link #set(Map)}.
 */
public final class ThreadContext {

  private static final NamedInheritableThreadLocal<Map<String, Object>> CONTEXT =
      new NamedInheritableThreadLocal<>("angus-thread-context");

  private ThreadContext() {
  }

  public static void set(Map<String, Object> map) {
    CONTEXT.set(Objects.requireNonNull(map, "map"));
  }

  /**
   * Creates a new {@link HashMap}, binds it to the current thread, and returns it.
   */
  public static Map<String, Object> create() {
    Map<String, Object> map = new HashMap<>();
    CONTEXT.set(map);
    return map;
  }

  /**
   * Returns the context map for this thread, or {@code null} if none has been {@linkplain #set set}
   * or {@linkplain #create created}.
   */
  public static Map<String, Object> get() {
    return CONTEXT.get();
  }

  /**
   * Returns the bound map, creating and binding a new {@link HashMap} if absent.
   */
  public static Map<String, Object> getOrCreate() {
    Map<String, Object> map = CONTEXT.get();
    if (map == null) {
      map = new HashMap<>();
      CONTEXT.set(map);
    }
    return map;
  }

  /**
   * @deprecated use {@link #getOrCreate()} for clarity
   */
  @Deprecated(since = "3.0.0", forRemoval = false)
  public static Map<String, Object> getAndSet() {
    return getOrCreate();
  }

  public static void add(String key, Object value) {
    getOrCreate().put(key, value);
  }

  public static Object get(String key) {
    Map<String, Object> map = CONTEXT.get();
    return map == null ? null : map.get(key);
  }

  public static boolean contains(String key) {
    Map<String, Object> map = CONTEXT.get();
    return map != null && map.containsKey(key);
  }

  public static void remove(String key) {
    Map<String, Object> map = CONTEXT.get();
    if (map != null) {
      map.remove(key);
    }
  }

  /**
   * Removes the entire context map for this thread (including inherited binding).
   */
  public static void remove() {
    CONTEXT.remove();
  }

  /**
   * Read-only view when a map exists; otherwise an empty unmodifiable map (never {@code null}).
   */
  public static Map<String, Object> getView() {
    Map<String, Object> map = CONTEXT.get();
    return map == null ? Map.of() : Collections.unmodifiableMap(map);
  }
}
