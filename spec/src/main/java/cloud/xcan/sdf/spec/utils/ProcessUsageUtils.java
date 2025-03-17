package cloud.xcan.sdf.spec.utils;

import static java.util.Objects.requireNonNull;

import cloud.xcan.sdf.spec.annotations.Nullable;
import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.ToLongFunction;

public class ProcessUsageUtils {

  private static final OperatingSystemMXBean operatingSystemBean =
      ManagementFactory.getOperatingSystemMXBean();

  /**
   * List of public, exported interface class names from supported JVM implementations.
   */
  private static final List<String> OPERATING_SYSTEM_BEAN_CLASS_NAMES =
      Arrays.asList(
          "com.ibm.lang.management.OperatingSystemMXBean", // J9
          "com.sun.management.OperatingSystemMXBean" // HotSpot
      );

  @Nullable
  private static final Class<?> OPERATING_SYSTEM_BEAN_CLASS = getFirstClassFound(
      OPERATING_SYSTEM_BEAN_CLASS_NAMES);

  @Nullable
  private static final Method PROCESS_CPU_USAGE = detectMethod("getProcessCpuLoad");

  @Nullable
  private static final Method SYSTEM_CPU_USAGE = detectMethod("getSystemCpuLoad");

  public static double getJvmUsedMemory() {
    double usedMemory = 0;
    for (MemoryPoolMXBean memoryPoolBean : ManagementFactory
        .getPlatformMXBeans(MemoryPoolMXBean.class)) {
      usedMemory += getUsageValue(memoryPoolBean, MemoryUsage::getUsed);
    }
    for (BufferPoolMXBean bufferPoolBean : ManagementFactory
        .getPlatformMXBeans(BufferPoolMXBean.class)) {
      usedMemory += bufferPoolBean.getMemoryUsed();
    }
    return usedMemory;
  }

  public static double getJvmMaxMemory() {
    double maxMemory = 0;
    for (MemoryPoolMXBean memoryPoolBean : ManagementFactory
        .getPlatformMXBeans(MemoryPoolMXBean.class)) {
      maxMemory += getUsageValue(memoryPoolBean, MemoryUsage::getMax);
    }
    return maxMemory;
  }

  public static double getProcessUsage() {
    return invoke(PROCESS_CPU_USAGE) * Runtime.getRuntime().availableProcessors() * 100; /*Top cpu*/
  }

  public static double getSystemUsage() {
    return invoke(SYSTEM_CPU_USAGE) * Runtime.getRuntime().availableProcessors() * 100; /*Top cpu*/
  }

  public static double invoke(@Nullable Method method) {
    try {
      return method != null ? (double) method.invoke(operatingSystemBean) : Double.NaN;
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      return Double.NaN;
    }
  }

  @Nullable
  public static Method detectMethod(String name) {
    requireNonNull(name);
    if (OPERATING_SYSTEM_BEAN_CLASS == null) {
      return null;
    }
    try {
      // ensure the Bean we have is actually an instance of the interface
      OPERATING_SYSTEM_BEAN_CLASS.cast(operatingSystemBean);
      return OPERATING_SYSTEM_BEAN_CLASS.getDeclaredMethod(name);
    } catch (ClassCastException | NoSuchMethodException | SecurityException e) {
      return null;
    }
  }

  @Nullable
  public static Class<?> getFirstClassFound(List<String> classNames) {
    for (String className : classNames) {
      try {
        return Class.forName(className);
      } catch (ClassNotFoundException ignore) {
      }
    }
    return null;
  }


  public static double getUsageValue(MemoryPoolMXBean memoryPoolMXBean,
      ToLongFunction<MemoryUsage> getter) {
    MemoryUsage usage = getUsage(memoryPoolMXBean);
    if (usage == null) {
      return Double.NaN;
    }
    return getter.applyAsLong(usage);
  }

  public static MemoryUsage getUsage(MemoryPoolMXBean memoryPoolMXBean) {
    try {
      return memoryPoolMXBean.getUsage();
    } catch (InternalError e) {
      // Defensive for potential InternalError with some specific JVM options. Based on its Javadoc,
      // MemoryPoolMXBean.getUsage() should return null, not throwing InternalError, so it seems to be a JVM bug.
      return null;
    }
  }
}
