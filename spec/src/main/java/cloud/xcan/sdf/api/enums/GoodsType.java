package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum GoodsType implements EnumMessage<String> {

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