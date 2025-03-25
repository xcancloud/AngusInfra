package cloud.xcan.angus.spec.thread;


import java.util.HashMap;
import java.util.Map;

public class ThreadContext {

  /**
   * The InheritableThreadLocal class is a subclass of ThreadLocal. Instead of each thread having
   * its own value inside a ThreadLocal, the InheritableThreadLocal grants access to values to a
   * thread and all child threads created by that thread.
   */
  public static ThreadLocal<Map<String, Object>> tl = new InheritableThreadLocal<>();

  public static void set(Map<String, Object> map) {
    tl.set(map);
  }

  public static Map<String, Object> create() {
    Map<String, Object> map = new HashMap<>();
    tl.set(map);
    return map;
  }

  public static Map<String, Object> get() {
    Map<String, Object> map = tl.get();
    if (map == null) {
      map = new HashMap<>();
      // tl.set(map);
    }
    return map;
  }

  public static Map<String, Object> getAndSet() {
    Map<String, Object> map = tl.get();
    if (map == null) {
      map = new HashMap<>();
      tl.set(map);
    }
    return map;
  }

  public static void add(String key, Object value) {
    getAndSet().put(key, value);
  }

  public static Object get(String key) {
    return get().get(key);
  }

  public static boolean contains(String key) {
    return get().containsKey(key);
  }

  public static void remove(String key) {
    get().remove(key);
  }

  public static void remove() {
    tl.remove();
  }
}
