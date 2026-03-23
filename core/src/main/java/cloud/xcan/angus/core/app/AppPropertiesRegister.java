package cloud.xcan.angus.core.app;

/**
 * Optional hook to register JVM or environment properties during startup
 * ({@link AppPropertiesRegisterInit}).
 */
public interface AppPropertiesRegister {

  /**
   * When {@code false}, {@link #register0()} skips {@link #register()}.
   */
  default boolean support() {
    return true;
  }

  default void register0() {
    if (support()) {
      register();
    }
  }

  void register();

  default AppPropertiesRegister getDefault() {
    return this;
  }

}
