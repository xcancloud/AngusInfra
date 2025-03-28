package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

/**
 * Product version type:
 *
 * <pre>
 * - CLOUD_SERVICE (Cloud Service Edition)
 * - DATACENTER (Data Center Edition)
 * - ENTERPRISE(Enterprise Edition)
 * - COMMUNITY (Community Edition)
 * - GENERIC(Generic Edition) - Not Supported
 * </pre>
 */
@EndpointRegister
public enum EditionType implements EnumMessage<String> {

  CLOUD_SERVICE,
  COMMUNITY,
  ENTERPRISE,
  DATACENTER
  /*, GENERIC*/;

  @Override
  public String getValue() {
    return this.name();
  }

  public boolean isCloudService() {
    return this.equals(CLOUD_SERVICE);
  }

  public boolean isDatacenter() {
    return this.equals(DATACENTER);
  }

  public boolean isEnterprise() {
    return this.equals(ENTERPRISE);
  }

  public boolean isCommunity() {
    return this.equals(COMMUNITY);
  }

  public boolean isPrivatization() {
    return isDatacenter() || isEnterprise() || isCommunity();
  }

  public boolean isPrivatizationFree() {
    return isCommunity();
  }

}
