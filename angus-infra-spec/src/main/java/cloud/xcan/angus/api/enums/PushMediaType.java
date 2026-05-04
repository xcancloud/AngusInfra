package cloud.xcan.angus.api.enums;


import cloud.xcan.angus.spec.experimental.Value;

public enum PushMediaType implements Value<String> {
  PLAIN_TEXT, PICTURE, EMO, VOICE, VIDEO;

  @Override
  public String getValue() {
    return this.name();
  }

}
