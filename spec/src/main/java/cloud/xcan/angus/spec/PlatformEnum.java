package cloud.xcan.angus.spec;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumValueMessage;

/**
 * An enumeration of supported operating systems. The order of declaration matches the osType
 * constants in the JNA Platform class.
 */
@EndpointRegister
public enum PlatformEnum implements EnumValueMessage<String> {
  /**
   * macOS
   */
  MACOS("MacOS"),
  /**
   * A flavor of Linux
   */
  LINUX("Linux"),
  /**
   * Microsoft Windows
   */
  WINDOWS("Windows"),
  /**
   * Solaris (SunOS)
   */
  SOLARIS("Solaris"),
  /**
   * FreeBSD
   */
  FREEBSD("FreeBSD"),
  /**
   * OpenBSD
   */
  OPENBSD("OpenBSD"),
  /**
   * Windows Embedded Compact
   */
  WINDOWSCE("Windows CE"),
  /**
   * IBM AIX
   */
  AIX("AIX"),
  /**
   * Android
   */
  ANDROID("Android"),
  /**
   * GNU operating system
   */
  GNU("GNU"),
  /**
   * Debian GNU/kFreeBSD
   */
  KFREEBSD("kFreeBSD"),
  /**
   * NetBSD
   */
  NETBSD("NetBSD"),
  /**
   * An unspecified system
   */
  UNKNOWN("Unknown");
//    /**
//     * Legacy name for Mac OS version 10.x
//     *
//     * @deprecated use {@link MACOS}
//     */
//    @Deprecated
//    MACOSX("macOS");

  private String name;

  PlatformEnum(String name) {
    this.name = name;
  }

  /**
   * Gets the friendly name of the platform
   *
   * @return the friendly name of the platform
   */
  public String getName() {
    return this.name;
  }

  public boolean isWindows() {
    return this.equals(WINDOWS);
  }

  public boolean isMaxOs() {
    return this.equals(MACOS);
  }

  /**
   * Gets the friendly name of the specified JNA Platform type
   *
   * @param osType The constant returned from JNA's {@link com.sun.jna.Platform#getOSType()}
   *               method.
   * @return the friendly name of the specified JNA Platform type
   */
  public static String getName(int osType) {
    return getValue(osType).getName();
  }

  /**
   * Gets the value corresponding to the specified JNA Platform type
   *
   * @param osType The constant returned from JNA's {@link com.sun.jna.Platform#getOSType()}
   *               method.
   * @return the value corresponding to the specified JNA Platform type
   */
  public static PlatformEnum getValue(int osType) {
    if (osType < 0 || osType >= UNKNOWN.ordinal()) {
      return UNKNOWN;
    }
    return values()[osType];
  }

  @Override
  public String getValue() {
    return this.name();
  }
}
