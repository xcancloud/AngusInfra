package cloud.xcan.sdf.core.app;

public interface AppPropertiesRegister {

  /**
   * Register according to conditions.
   */
  default boolean support() {
    return true;
  }

  default void register0() {
    if (support()){
      register();
    }
  }

  void register();

  default AppPropertiesRegister getDefault() {
    return this;
  }

}
