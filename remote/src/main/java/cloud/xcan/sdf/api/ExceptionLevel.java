package cloud.xcan.sdf.api;


import cloud.xcan.sdf.spec.experimental.Value;

public enum ExceptionLevel implements Value<String> {

  /**
   * Ignorable exception, the exception will not cause any impact on the system and business.
   */
  IGNORABLE,
  /**
   * Exceptions that must be paid attention to but do not need to be handled, only serve as
   * warnings.
   */
  WARNING,
  /**
   * An error has occurred in the system or business. This error is not expected and has a small
   * impact, but we need to deal with or repair it.
   */
  ERROR,
  /**
   * An error has occurred in the system or business. This error often has a great impact on the
   * system and business, causing the system or business part of the function to not work properly.
   */
  URGENT,
  /**
   * The error is fatal, the business and system functions are not working properly, the system may
   * or has been down.
   */
  DEADLY;

  @Override
  public String getValue() {
    return this.name();
  }
}
