package cloud.xcan.sdf.api.pojo.manifest;

import cloud.xcan.sdf.spec.utils.ObjectUtils;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 * Plugin data initialization is handled internally by the plugin.
 */
@Setter
@Getter
public class Installation implements Serializable {

  public static final String PACKAGE_EXTENSION_NAME = ".zip";
  public static final String PACKAGE_FULL_SUFFIX = "-full" + PACKAGE_EXTENSION_NAME;

  /**
   * Whether to install to the main application plugins directory.
   *
   * <pre>
   * Main application plugins directory: ./plugins
   *
   * Sub application plugins directory: ./apps/subapp/plugins
   * </pre>
   */
  private Boolean mainApplication;

  /**
   * Install the application code. If you are installing a sub application plug-in, the system needs
   * to find the sub application based on the code to install the plug-in.
   */
  private String applicationCode;

  /**
   * Naming Rules：
   * <pre>
   *  - PLUGINS_NAME[-plugin]-VERSION.zip
   *  - PLUGINS_NAME[-plugin]-VERSION-web.zip
   * </pre>
   * e.g. xcan-sms-proxy.aliyun-plugin-0.1.0-SNAPSHOT.zip or xcan-sms-proxy.aliyun-plugin-0.1.0-SNAPSHOT-web.zip
   */
  private Set<String> packages;

  private Boolean uninstallable;

  private Restriction restriction;

  private Map<String, String> extras;

  private Installation(Builder builder) {
    setMainApplication(builder.mainApplication);
    setApplicationCode(builder.applicationCode);
    setPackages(builder.packages);
    setUninstallable(builder.uninstallable);
    setRestriction(builder.restriction);
    setExtras(builder.extras);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static Builder newBuilder(Installation copy) {
    Builder builder = new Builder();
    builder.mainApplication = copy.getMainApplication();
    builder.applicationCode = copy.getApplicationCode();
    builder.packages = copy.getPackages();
    builder.uninstallable = copy.getUninstallable();
    builder.restriction = copy.getRestriction();
    builder.extras = copy.getExtras();
    return builder;
  }

  public boolean hasWebPackage() {
    return ObjectUtils.isNotEmpty(packages) && packages.stream().anyMatch(x -> x.contains("-web"));
  }

  public static final class Builder {

    private Boolean mainApplication;
    private String applicationCode;
    private Set<String> packages;
    private Boolean uninstallable;
    private Restriction restriction;
    private Map<String, String> extras;

    private Builder() {
    }

    public Builder mainApplication(Boolean mainApplication) {
      this.mainApplication = mainApplication;
      return this;
    }

    public Builder applicationCode(String applicationCode) {
      this.applicationCode = applicationCode;
      return this;
    }

    public Builder packages(Set<String> packages) {
      this.packages = packages;
      return this;
    }

    public Builder uninstallable(Boolean uninstallable) {
      this.uninstallable = uninstallable;
      return this;
    }

    public Builder restriction(Restriction restriction) {
      this.restriction = restriction;
      return this;
    }

    public Builder extras(Map<String, String> extras) {
      this.extras = extras;
      return this;
    }

    public Installation build() {
      return new Installation(this);
    }
  }
}
