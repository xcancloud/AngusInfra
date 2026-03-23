package cloud.xcan.angus.core.app;

/**
 * Invoked by {@link AppBeanReadyInit} after the context has started; use for late wiring that must
 * run after all beans are available.
 */
@FunctionalInterface
public interface AppBeanReady {

  void ready();
}
