package cloud.xcan.angus.spec.utils;

import cloud.xcan.angus.spec.unit.DataSize;
import java.text.DecimalFormat;

public class DataSizeUtils {

  public static String formatRoundingSize(long size) {
    if (size <= 0L) {
      return "0B";
    } else {
      String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
      int digitGroups = (int) (Math.log10((double) size) / Math.log10(1024.0D));
      String var10000 = (new DecimalFormat("###0"))
          .format((double) size / Math.pow(1024.0D, (double) digitGroups));
      return var10000 + units[digitGroups];
    }
  }

  public static DataSize formatRoundingDataSize(long size) {
    if (size <= 0L) {
      return DataSize.ofBytes(0);
    } else {
      String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
      int digitGroups = (int) (Math.log10((double) size) / Math.log10(1024.0D));
      String var10000 = (new DecimalFormat("###0"))
          .format((double) size / Math.pow(1024.0D, (double) digitGroups));
      return DataSize.parse(var10000 + units[digitGroups]);
    }
  }

  public static String formatSize(long size) {
    if (size <= 0L) {
      return "0B";
    } else {
      String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
      int digitGroups = (int) (Math.log10((double) size) / Math.log10(1024.0D));
      String var10000 = (new DecimalFormat("###0.#"))
          .format((double) size / Math.pow(1024.0D, (double) digitGroups));
      return var10000 + units[digitGroups];
    }
  }

  public static DataSize formatDataSize(long size) {
    if (size <= 0L) {
      return DataSize.ofBytes(0);
    } else {
      String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
      int digitGroups = (int) (Math.log10((double) size) / Math.log10(1024.0D));
      String var10000 = (new DecimalFormat("###0.#"))
          .format((double) size / Math.pow(1024.0D, (double) digitGroups));
      return DataSize.parse(var10000 + units[digitGroups]);
    }
  }
}
