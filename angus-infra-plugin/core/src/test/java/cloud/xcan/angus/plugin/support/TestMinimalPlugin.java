package cloud.xcan.angus.plugin.support;

import cloud.xcan.angus.plugin.api.Plugin;
import cloud.xcan.angus.plugin.api.PluginContext;
import cloud.xcan.angus.plugin.exception.PluginException;
import cloud.xcan.angus.plugin.model.PluginState;

/**
 * Minimal {@link Plugin} for building test plugin JARs and exercising the plugin manager.
 */
public class TestMinimalPlugin implements Plugin {

  public static final ThreadLocal<Boolean> FAIL_INIT =
      ThreadLocal.withInitial(() -> false);
  public static final ThreadLocal<Boolean> FAIL_START =
      ThreadLocal.withInitial(() -> false);
  public static final ThreadLocal<Boolean> FAIL_STOP =
      ThreadLocal.withInitial(() -> false);
  public static final ThreadLocal<Boolean> FAIL_DESTROY =
      ThreadLocal.withInitial(() -> false);

  private PluginState state = PluginState.UNKNOWN;

  public static void clearFlags() {
    FAIL_INIT.remove();
    FAIL_START.remove();
    FAIL_STOP.remove();
    FAIL_DESTROY.remove();
  }

  @Override
  public String getId() {
    return "test-minimal";
  }

  @Override
  public String getName() {
    return "Test Minimal";
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
    if (Boolean.TRUE.equals(FAIL_INIT.get())) {
      throw new PluginException("init failed");
    }
    state = PluginState.INITIALIZED;
  }

  @Override
  public void start() throws PluginException {
    if (Boolean.TRUE.equals(FAIL_START.get())) {
      throw new PluginException("start failed");
    }
    state = PluginState.STARTED;
  }

  @Override
  public void stop() throws PluginException {
    if (Boolean.TRUE.equals(FAIL_STOP.get())) {
      throw new PluginException("stop failed");
    }
    state = PluginState.STOPPED;
  }

  @Override
  public void destroy() throws PluginException {
    if (Boolean.TRUE.equals(FAIL_DESTROY.get())) {
      throw new PluginException("destroy failed");
    }
  }

  @Override
  public PluginState getState() {
    return state;
  }
}
