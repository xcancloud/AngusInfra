package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;

public enum GoodsType implements Value<String> {

  APPLICATION,/* SERVICE, */PLUGIN, PLUGIN_APPLICATION, RESOURCE_QUOTA;

  @Override
  public String getValue() {
    return this.name();
  }

  public boolean isApplication() {
    return APPLICATION.equals(this);
  }

  public boolean isPlugin() {
    return PLUGIN.equals(this);
  }

  public boolean isPluginApplication() {
    return PLUGIN_APPLICATION.equals(this);
  }

  public boolean isWideApplication() {
    return isApplication() || isPluginApplication();
  }

  public boolean isWidePlugin() {
    return isPlugin() || isPluginApplication();
  }

  public boolean isResourceQuota() {
    return RESOURCE_QUOTA.equals(this);
  }

}
