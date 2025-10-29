package cloud.xcan.angus.plugin.core;

import cloud.xcan.angus.plugin.api.Plugin;
import cloud.xcan.angus.plugin.api.PluginContext;
import cloud.xcan.angus.plugin.model.PluginDescriptor;
import cloud.xcan.angus.plugin.model.PluginState;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class PluginWrapper {
    private Plugin plugin;
    private PluginDescriptor descriptor;
    private PluginClassLoader classLoader;
    private Path pluginPath;
    private PluginState state;
    private LocalDateTime loadedAt;
    private LocalDateTime startedAt;
    private PluginContext context;
}

