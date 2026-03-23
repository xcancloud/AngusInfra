package cloud.xcan.angus.plugin.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import cloud.xcan.angus.plugin.model.PluginState;
import org.junit.jupiter.api.Test;

class PluginEventsTest {

  private final Object src = new Object();

  @Test
  void loadedStartedStoppedUnloaded() {
    PluginLoadedEvent loaded = new PluginLoadedEvent(src, "i", "n", "v");
    assertEquals(PluginState.INITIALIZED, loaded.getEventState());
    assertEquals("i", loaded.getPluginId());

    PluginStartedEvent started = new PluginStartedEvent(src, "i", "n", "v");
    assertEquals(PluginState.STARTED, started.getEventState());

    PluginStoppedEvent stopped = new PluginStoppedEvent(src, "i", "n", "v");
    assertEquals(PluginState.STOPPED, stopped.getEventState());

    PluginUnloadedEvent unloaded = new PluginUnloadedEvent(src, "i", "n", "v");
    assertEquals(PluginState.UNLOADING, unloaded.getEventState());
  }

  @Test
  void failedCarriesReason() {
    RuntimeException ex = new RuntimeException("x");
    PluginFailedEvent failed = new PluginFailedEvent(src, "i", "n", "v", "reason", ex);
    assertEquals(PluginState.ERROR, failed.getEventState());
    assertEquals("reason", failed.getFailureReason());
    assertSame(ex, failed.getException());
  }
}
