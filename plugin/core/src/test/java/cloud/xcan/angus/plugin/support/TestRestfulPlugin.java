package cloud.xcan.angus.plugin.support;

import cloud.xcan.angus.plugin.api.PluginContext;
import cloud.xcan.angus.plugin.api.RestfulPlugin;
import cloud.xcan.angus.plugin.exception.PluginException;
import cloud.xcan.angus.plugin.model.PluginState;
import java.util.List;

public class TestRestfulPlugin implements RestfulPlugin {

  private PluginState state = PluginState.UNKNOWN;

  @Override
  public List<Class<?>> getControllerClasses() {
    return List.of(SamplePluginController.class);
  }

  @Override
  public String getId() {
    return "test-restful";
  }

  @Override
  public String getName() {
    return "Test Restful";
  }

  @Override
  public String getVersion() {
    return "1.0.0";
  }

  @Override
  public String getDescription() {
    return "test";
  }

  @Override
  public String getAuthor() {
    return "test";
  }

  @Override
  public void initialize(PluginContext context) throws PluginException {
    state = PluginState.INITIALIZED;
  }

  @Override
  public void start() throws PluginException {
    state = PluginState.STARTED;
  }

  @Override
  public void stop() throws PluginException {
    state = PluginState.STOPPED;
  }

  @Override
  public void destroy() throws PluginException {
    // no-op
  }

  @Override
  public PluginState getState() {
    return state;
  }
}
