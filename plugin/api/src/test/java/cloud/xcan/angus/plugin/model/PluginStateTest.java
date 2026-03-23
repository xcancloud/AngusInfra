package cloud.xcan.angus.plugin.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PluginStateTest {

  @Test
  void fromHandlesNullAndUnknown() {
    assertEquals(PluginState.UNKNOWN, PluginState.from(null));
    assertEquals(PluginState.UNKNOWN, PluginState.from("not-a-state"));
  }

  @Test
  void fromParsesName() {
    assertEquals(PluginState.STARTED, PluginState.from("started"));
    assertEquals(PluginState.STARTED, PluginState.from("STARTED"));
  }
}
