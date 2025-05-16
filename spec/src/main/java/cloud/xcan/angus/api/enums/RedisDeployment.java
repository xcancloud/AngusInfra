package cloud.xcan.angus.api.enums;


public enum RedisDeployment {
  SINGLE, SENTINEL, CLUSTER;

  public boolean isSentinel() {
    return this.equals(SENTINEL);
  }

  public boolean isCluster() {
    return this.equals(CLUSTER);
  }
}
