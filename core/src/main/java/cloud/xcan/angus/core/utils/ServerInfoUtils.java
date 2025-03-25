package cloud.xcan.angus.core.utils;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.spec.utils.mc.MachineCode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import namesbean.SystemInfo;
import namesbean.hardware.CentralProcessor.ProcessorIdentifier;
import namesbean.hardware.HardwareAbstractionLayer;
import namesbean.hardware.NetworkIF;

@Slf4j
public class ServerInfoUtils {

  public final static SystemInfo SYSTEM_INFO = new SystemInfo();
  public final static HardwareAbstractionLayer HARDWARE = SYSTEM_INFO.getHardware();

  public static List<String> getIpv4Address() {
    List<NetworkIF> var0 = HARDWARE.getNetworkIFs();
    List<String> ips = null;
    if (var0 != null && var0.size() > 0) {
      ips = new ArrayList<>();
      for (NetworkIF var : var0) {
        String[] var1 = var.getIPv4addr();
        if (var1 != null) {
          for (String var2 : var1) {
            if (var2 != null) {
              ips.add(var2);
            }
          }
        }
      }
    }
    return ips;
  }

  public static List<String> getMacAddress() {
    List<NetworkIF> var0 = HARDWARE.getNetworkIFs();
    List<String> var2 = null;
    if (var0 != null && var0.size() > 0) {
      var2 = new ArrayList<>();
      for (NetworkIF var : var0) {
        String var1 = var.getMacaddr();
        if (var1 != null /*Exclude duplicates*/ && !var2.contains(var1)) {
          var2.add(var1);
        }
      }
      Collections.reverse(var2);
    }
    return var2;
  }

  public static List<String> getProcessorId() {
    List<String> var0 = null;
    ProcessorIdentifier pd = HARDWARE.getProcessor().getProcessorIdentifier();
    if (pd != null) {
      var0 = new ArrayList<>();
      var0.add(pd.getProcessorID());
    }
    return var0;
  }

  public static String getBaseboardSn() {
    return HARDWARE.getComputerSystem().getBaseboard().getSerialNumber();
  }

  public static String getMachineCode() {
    return getMachineCodeBuilder().getCode();
  }

  public static MachineCode getMachineCodeBuilder() {
    List<String> ips = getIpv4Address(), macAddress = getMacAddress(), cpuIds = getProcessorId();
    String baseboardSns = getBaseboardSn();
    return new MachineCode.Builder()
        .ipAddress(isNotEmpty(ips) ? String.join(",", ips) : null)
        .macAddress(isNotEmpty(macAddress) ? String.join(",", macAddress) : null)
        .cpuSerial(isNotEmpty(cpuIds) ? String.join(",", cpuIds) : null)
        .mainBoardSerial(baseboardSns).build();
  }
}
