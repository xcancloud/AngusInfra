package cloud.xcan.angus.core.biz.server;

import java.util.ArrayList;
import java.util.List;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor.ProcessorIdentifier;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;

public class ServerInfoUtils {

  SystemInfo si = new SystemInfo();
  HardwareAbstractionLayer hal = si.getHardware();

  public List<String> getIa() {
    try {
      List<NetworkIF> var0 = hal.getNetworkIFs();
      List<String> ips = null;
      if (var0 != null && !var0.isEmpty()) {
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
    } catch (Exception e) {
    }
    return null;
  }

  public List<String> getMa() {
    try {
      List<NetworkIF> var0 = hal.getNetworkIFs();
      List<String> var2 = null;
      if (var0 != null && !var0.isEmpty()) {
        var2 = new ArrayList<>();
        for (NetworkIF var : var0) {
          String var1 = var.getMacaddr();
          if (var1 != null) {
            var2.add(var1);
          }
        }
      }
      return var2;
    } catch (Exception e) {
    }
    return null;
  }

  public List<String> getCs() {
    try {
      List<String> var0 = null;
      ProcessorIdentifier pd = hal.getProcessor().getProcessorIdentifier();
      if (pd != null) {
        var0 = new ArrayList<>();
        var0.add(pd.getProcessorID());
      }
      return var0;
    } catch (Exception e) {
    }
    return null;
  }

  public String getMbs() {
    try {
      return hal.getComputerSystem().getBaseboard().getSerialNumber();
    } catch (Exception e) {
    }
    return null;
  }
}
