package cloud.xcan.angus.plugin.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import cloud.xcan.angus.plugin.api.PluginManager;
import cloud.xcan.angus.plugin.core.DefaultPluginManager;
import cloud.xcan.angus.plugin.management.PluginManagementService;
import cloud.xcan.angus.plugin.store.DiskPluginStore;
import cloud.xcan.angus.plugin.store.PluginStore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class PluginAutoConfigurationTest {

  @Test
  void loadsDiskStoreAndManagerAndManagementService() {
    new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(PluginAutoConfiguration.class))
        .withPropertyValues(
            "angus.plugin.enabled=true",
            "angus.plugin.auto-load=false",
            "angus.plugin.storage-type=DISK"
        )
        .run(ctx -> {
          assertThat(ctx).hasSingleBean(PluginStore.class);
          assertThat(ctx.getBean(PluginStore.class)).isInstanceOf(DiskPluginStore.class);
          assertThat(ctx).hasSingleBean(PluginManager.class);
          assertThat(ctx.getBean(PluginManager.class)).isInstanceOf(DefaultPluginManager.class);
          assertThat(ctx).hasSingleBean(PluginManagementService.class);
        });
  }
}
