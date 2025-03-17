package cloud.xcan.sdf.spec.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Commonly used formatting of floating-point values used when writing custom exposition to various
 * monitoring systems.
 */
public final class DoubleFormatUtils {

  /**
   * Because NumberFormat is not thread-safe we cannot share instances across threads. Use a
   * ThreadLocal to create one per thread as this seems to offer a significant performance
   * improvement over creating one per-thread: https://stackoverflow.com/a/1285297/2648
   * https://github.com/indeedeng/java-dogstatsd-client/issues/4
   */
  private static final ThreadLocal<NumberFormat> DECIMAL_OR_NAN = ThreadLocal.withInitial(() -> {

    // Always create the formatter for the US locale in order to avoid this bug:
    // https://github.com/indeedeng/java-dogstatsd-client/issues/3
    final NumberFormat numberFormatter = NumberFormat.getInstance(Locale.US);
    numberFormatter.setGroupingUsed(false);
    numberFormatter.setMaximumFractionDigits(6);

    // we need to specify a bucket for Double.NaN that is recognized by dogStatsD
    if (numberFormatter instanceof DecimalFormat) { // better safe than a runtime error
      final DecimalFormat decimalFormat = (DecimalFormat) numberFormatter;
      final DecimalFormatSymbols symbols = decimalFormat.getDecimalFormatSymbols();
      symbols.setNaN("NaN");
      decimalFormat.setDecimalFormatSymbols(symbols);
    }

    return numberFormatter;
  });

  private static final ThreadLocal<DecimalFormat> WHOLE_OR_DECIMAL = ThreadLocal.withInitial(() -> {
    // the following will ensure a dot ('.') as decimal separator
    DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
    return new DecimalFormat("##0.######", otherSymbols);
  });

  private static final ThreadLocal<DecimalFormat> DECIMAL = ThreadLocal.withInitial(() -> {
    // the following will ensure a dot ('.') as decimal separator
    DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
    return new DecimalFormat("##0.0#####", otherSymbols);
  });

  private DoubleFormatUtils() {
  }

  /**
   * @param d Number to format.
   * @return A stringified version of the number that uses a decimal representation or the word
   * "NaN".
   */
  public static String decimalOrNan(double d) {
    return DECIMAL_OR_NAN.get().format(d);
  }

  /**
   * @param d Number to format.
   * @return A stringified version of the number that only uses a decimal representation if the
   * number is not whole.
   */
  public static String decimal(double d) {
    return DECIMAL.get().format(d);
  }

  /**
   * @param d Number to format.
   * @return A stringified version of the number that only uses a decimal representation if the
   * number is not whole.
   */
  public static String wholeOrDecimal(double d) {
    return WHOLE_OR_DECIMAL.get().format(d);
  }
}
