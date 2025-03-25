package cloud.xcan.angus.spec.locale;

import java.util.Locale;

/**
 * Strategy interface for determining the current Locale.
 *
 * <p>A LocaleContext instance can be associated with a thread
 * via the SdfLocaleHolder class.
 */
public interface LocaleContext {

  /**
   * Return the current Locale, which can be fixed or determined dynamically, depending on the
   * implementation strategy.
   *
   * @return the current Locale, or {@code null} if no specific Locale associated
   */
  Locale getLocale();

}
