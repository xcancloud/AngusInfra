package cloud.xcan.angus.spec.setting;


/**
 * The system properties usually provided by the Java runtime.
 */
public enum JavaSystemSetting implements SystemSetting {
  JAVA_VERSION("java.version"),
  JAVA_VENDOR("java.vendor"),
  TEMP_DIRECTORY("java.io.tmpdir"),
  JAVA_VM_NAME("java.vm.name"),
  JAVA_VM_VERSION("java.vm.version"),

  OS_NAME("os.name"),
  OS_VERSION("os.version"),

  USER_HOME("user.home"),
  USER_LANGUAGE("user.language"),
  USER_REGION("user.region"),
  USER_NAME("user.name"),

  SSL_KEY_STORE("javax.net.ssl.keyStore"),
  SSL_KEY_STORE_PASSWORD("javax.net.ssl.keyStorePassword"),
  SSL_KEY_STORE_TYPE("javax.net.ssl.keyStoreType");

  private final String systemProperty;

  JavaSystemSetting(String systemProperty) {
    this.systemProperty = systemProperty;
  }

  @Override
  public String property() {
    return systemProperty;
  }

  @Override
  public String environmentVariable() {
    return null;
  }

  @Override
  public String defaultValue() {
    return null;
  }
}
