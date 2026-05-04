package cloud.xcan.angus.api.enums;


import cloud.xcan.angus.spec.experimental.Value;

public enum RedisDeployment implements Value<String> {
  SINGLE, SENTINEL, CLUSTER;

  public boolean isSentinel() {
    return this.equals(SENTINEL);
  }

  public boolean isCluster() {
    return this.equals(CLUSTER);
  }

  @Override
  public String getValue() {
    return this.name();
  }
}
