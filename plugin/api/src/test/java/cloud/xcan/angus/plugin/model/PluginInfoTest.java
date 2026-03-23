package cloud.xcan.angus.plugin.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class PluginInfoTest {

  @Test
  void builderPopulatesAllFields() {
    LocalDateTime now = LocalDateTime.now();
    PluginInfo info = PluginInfo.builder()
        .id("id")
        .name("name")
        .version("1")
        .description("d")
        .author("a")
        .state(PluginState.STARTED)
        .loadedAt(now)
        .startedAt(now)
        .pluginClass("c")
        .dependencies(List.of("d1"))
        .type("t")
        .apiPrefix("/p")
        .endpointCount(3)
        .filePath("/f")
        .fileSize(9L)
        .enabled(true)
        .homepage("h")
        .license("l")
        .tags(List.of("t1"))
        .build();

    assertEquals("id", info.getId());
    assertEquals("name", info.getName());
    assertEquals(PluginState.STARTED, info.getState());
    assertEquals(3, info.getEndpointCount());
    assertEquals(List.of("d1"), info.getDependencies());
  }
}
