package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum SyncStatus implements EnumMessage<String> {
  /**
   * Business data flag that does not support synchronization
   */
  UNSUPPORTED,
  /**
   * Current business processing
   */
  BIZ_PENDING,
  /**
   * Waiting for synchronization status
   */
  SYNC_PENDING,
  /**
   * Synchronization success flag
   */
  SYNCHRONIZED;

  @Override
  public String getValue() {
    return this.name();
  }
}