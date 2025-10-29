package cloud.xcan.angus.plugin.management;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PluginStats {
    private int totalPlugins;
    private int activePlugins;
    private int restEndpoints;
}

