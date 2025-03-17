package cloud.xcan.sdf.api.enums;


import cloud.xcan.sdf.spec.ValueObject;
import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;
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
