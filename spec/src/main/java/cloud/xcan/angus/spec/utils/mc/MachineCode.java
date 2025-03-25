package cloud.xcan.angus.spec.utils.mc;

import static cloud.xcan.angus.spec.utils.mc.Util.format;
import static cloud.xcan.angus.spec.utils.mc.Util.md5;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class MachineCode {

  private final String ipAddress;

  private final String macAddress;

  private final String cpuSerial;

  private final String mainBoardSerial;

  private MachineCode(Builder builder) {
    if (isBlank(builder.ipAddress) && isBlank(builder.macAddress)
        && isBlank(builder.cpuSerial) && isBlank(builder.mainBoardSerial)) {
      throw new IllegalArgumentException("Machine information is missing");
    }
    this.ipAddress = builder.ipAddress;
    this.macAddress = builder.macAddress;
    this.cpuSerial = builder.cpuSerial;
    this.mainBoardSerial = builder.mainBoardSerial;
  }

  public String getCode() {
    StringBuilder machineInfo = new StringBuilder();
    if (!isBlank(ipAddress)) {
      machineInfo.append(ipAddress);
    } else if (!isBlank(macAddress)) {
      machineInfo.append(ipAddress);
    } else if (!isBlank(cpuSerial)) {
      machineInfo.append(cpuSerial);
    } else if (!isBlank(mainBoardSerial)) {
      machineInfo.append(mainBoardSerial);
    }
    String machineDigest = md5(machineInfo.toString());
    return format(machineDigest);
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public String getMacAddress() {
    return macAddress;
  }

  public String getCpuSerial() {
    return cpuSerial;
  }

  public String getMainBoardSerial() {
    return mainBoardSerial;
  }

  public static final class Builder {

    private String ipAddress;
    private String macAddress;
    private String cpuSerial;
    private String mainBoardSerial;

    public Builder() {
    }

    public Builder ipAddress(String val) {
      ipAddress = val;
      return this;
    }

    public Builder macAddress(String val) {
      macAddress = val;
      return this;
    }

    public Builder cpuSerial(String val) {
      cpuSerial = val;
      return this;
    }

    public Builder mainBoardSerial(String val) {
      mainBoardSerial = val;
      return this;
    }

    public MachineCode build() {
      return new MachineCode(this);
    }
  }
}
