package cloud.xcan.angus.api.enums;

public enum InstallType {

  SHARED, STANDALONE;

  public boolean isShared() {
    return this.equals(SHARED);
  }

  public boolean isStandalone() {
    return this.equals(STANDALONE);
  }

}
