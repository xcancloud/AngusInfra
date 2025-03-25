package cloud.xcan.angus.lettucex.config;


public enum Deployment {
  SINGLE, SENTINEL, CLUSTER;

  public boolean isSentinel() {
    return this.equals(SENTINEL);
  }

  public boolean isCluster() {
    return this.equals(CLUSTER);
  }
}
