package cloud.xcan.angus.plugin.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 用于从步骤配置 {@code Map<String, Object>} 中读取类型化值的工具类。
 *
 * <p>插件可直接使用这些方法，或通过强类型请求配置 POJO 委托调用。
 * 所有方法都是空安全的，并在缺失时返回合理的默认值。
 *
 * @since 2.0.0
 */
public final class ConfigReader {

    private ConfigReader() {}

    public static String getString(Map<String, Object> config, String key) {
        Object v = config.get(key);
        return v != null ? v.toString() : null;
    }

    public static String getString(Map<String, Object> config, String key, String defaultValue) {
        Object v = config.get(key);
        return v != null ? v.toString() : defaultValue;
    }

    public static int getInt(Map<String, Object> config, String key, int defaultValue) {
        Object v = config.get(key);
        if (v == null) return defaultValue;
        if (v instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(v.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static long getLong(Map<String, Object> config, String key, long defaultValue) {
        Object v = config.get(key);
        if (v == null) return defaultValue;
        if (v instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(v.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean getBoolean(Map<String, Object> config, String key, boolean defaultValue) {
        Object v = config.get(key);
        if (v == null) return defaultValue;
        if (v instanceof Boolean b) return b;
        return Boolean.parseBoolean(v.toString());
    }

    public static double getDouble(Map<String, Object> config, String key, double defaultValue) {
        Object v = config.get(key);
        if (v == null) return defaultValue;
        if (v instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(v.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static Integer getInteger(Map<String, Object> config, String key) {
        Object v = config.get(key);
        if (v == null) return null;
        if (v instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(v.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMap(Map<String, Object> config, String key) {
        Object v = config.get(key);
        if (v instanceof Map<?, ?> m) return (Map<String, Object>) m;
        return null;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> getStringMap(Map<String, Object> config, String key) {
        Object v = config.get(key);
        if (v instanceof Map<?, ?> m) return (Map<String, String>) m;
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> getList(Map<String, Object> config, String key) {
        Object v = config.get(key);
        if (v instanceof List<?> l) return (List<T>) l;
        return Collections.emptyList();
    }
}
