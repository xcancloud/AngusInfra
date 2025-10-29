package cloud.xcan.angus.queue.core.entity;

public enum MessageStatus {
  READY(0),
  LEASED(1),
  DONE(2);
  private final int code;

  MessageStatus(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }
}

