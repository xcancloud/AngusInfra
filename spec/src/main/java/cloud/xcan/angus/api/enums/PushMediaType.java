package cloud.xcan.angus.api.enums;


import cloud.xcan.angus.spec.ValueObject;
import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;
import lombok.Getter;

@Getter
@EndpointRegister
public enum PushMediaType implements ValueObject<PushMediaType>, EnumMessage<String> {
  PLAIN_TEXT, PICTURE, EMO, VOICE, VIDEO;

  @Override
  public String getValue() {
    return this.name();
  }

  @Override
  public boolean sameValueAs(final PushMediaType other) {
    return this.getValue().equals(other.getValue());
  }

}
