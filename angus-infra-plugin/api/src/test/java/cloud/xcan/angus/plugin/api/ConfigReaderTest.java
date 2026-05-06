package cloud.xcan.angus.plugin.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ConfigReaderTest {

    @Test
    void getString_returnsValue() {
        Map<String, Object> m = Map.of("key", "value");
        assertEquals("value", ConfigReader.getString(m, "key"));
    }

    @Test
    void getString_returnsNull_whenMissing() {
        assertNull(ConfigReader.getString(Map.of(), "key"));
    }

    @Test
    void getString_returnsDefault_whenMissing() {
        assertEquals("def", ConfigReader.getString(Map.of(), "key", "def"));
    }

    @Test
    void getString_convertsNonString() {
        assertEquals("42", ConfigReader.getString(Map.of("n", 42), "n"));
    }

    @Test
    void getInt_parsesString() {
        assertEquals(99, ConfigReader.getInt(Map.of("n", "99"), "n", 0));
    }

    @Test
    void getInt_handlesNumber() {
        assertEquals(7, ConfigReader.getInt(Map.of("n", 7L), "n", 0));
    }

    @Test
    void getInt_returnsDefault_onInvalidString() {
        assertEquals(5, ConfigReader.getInt(Map.of("n", "abc"), "n", 5));
    }

    @Test
    void getLong_handlesNumber() {
        assertEquals(100_000L, ConfigReader.getLong(Map.of("n", 100_000), "n", 0));
    }

    @Test
    void getBoolean_parsesTrueString() {
        assertTrue(ConfigReader.getBoolean(Map.of("b", "true"), "b", false));
    }

    @Test
    void getBoolean_handlesBooleanType() {
        assertFalse(ConfigReader.getBoolean(Map.of("b", Boolean.FALSE), "b", true));
    }

    @Test
    void getMap_returnsTypedMap() {
        Map<String, Object> inner = Map.of("a", "b");
        Map<String, Object> m = Map.of("nested", inner);
        assertSame(inner, ConfigReader.getMap(m, "nested"));
    }

    @Test
    void getMap_returnsNull_whenNotMap() {
        assertNull(ConfigReader.getMap(Map.of("x", "string"), "x"));
    }

    @Test
    void getStringMap_returnsTypedMap() {
        Map<String, String> inner = Map.of("a", "b");
        Map<String, Object> m = Map.of("headers", inner);
        assertSame(inner, ConfigReader.getStringMap(m, "headers"));
    }

    @Test
    void getList_returnsList() {
        List<String> list = List.of("a", "b");
        Map<String, Object> m = Map.of("items", list);
        assertEquals(list, ConfigReader.<String>getList(m, "items"));
    }

    @Test
    void getList_returnsEmptyList_whenMissing() {
        assertTrue(ConfigReader.getList(Map.of(), "items").isEmpty());
    }
}
