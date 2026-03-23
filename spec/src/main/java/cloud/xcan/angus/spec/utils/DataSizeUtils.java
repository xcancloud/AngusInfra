package cloud.xcan.angus.spec.utils;

import cloud.xcan.angus.spec.unit.DataSize;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class DataSizeUtils {

  private static final String[] UNITS = {"B", "kB", "MB", "GB", "TB"};

  private DataSizeUtils() {
  }

  private static DecimalFormat newFormat(String pattern) {
    DecimalFormat df = new DecimalFormat(pattern, DecimalFormatSymbols.getInstance(Locale.ROOT));
    df.setGroupingUsed(false);
    return df;
  }

  private static int digitGroup(long size) {
    int g = (int) (Math.log10((double) size) / Math.log10(1024.0d));
    return Math.min(Math.max(g, 0), UNITS.length - 1);
  }

  public static String formatRoundingSize(long size) {
    if (size <= 0L) {
      return "0B";
    }
    int digitGroups = digitGroup(size);
    String value = newFormat("###0").format((double) size / Math.pow(1024.0d, digitGroups));
    return value + UNITS[digitGroups];
  }

  public static DataSize formatRoundingDataSize(long size) {
    if (size <= 0L) {
      return DataSize.ofBytes(0);
    }
    int digitGroups = digitGroup(size);
    String value = newFormat("###0").format((double) size / Math.pow(1024.0d, digitGroups));
    return DataSize.parse(value + UNITS[digitGroups]);
  }

  public static String formatSize(long size) {
    if (size <= 0L) {
      return "0B";
    }
    int digitGroups = digitGroup(size);
    String value = newFormat("###0.#").format((double) size / Math.pow(1024.0d, digitGroups));
    return value + UNITS[digitGroups];
  }

  public static DataSize formatDataSize(long size) {
    if (size <= 0L) {
      return DataSize.ofBytes(0);
    }
    int digitGroups = digitGroup(size);
    String value = newFormat("###0.#").format((double) size / Math.pow(1024.0d, digitGroups));
    return DataSize.parse(value + UNITS[digitGroups]);
  }
}
