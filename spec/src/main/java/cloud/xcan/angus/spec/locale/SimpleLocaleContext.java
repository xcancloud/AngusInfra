package cloud.xcan.angus.spec.locale;

import java.util.Locale;

/**
 * Simple implementation of the {@link LocaleContext} interface, always returning a specified
 * {@code Locale}.
 *
 * @see SdfLocaleHolder#setLocaleContext
 * @see SdfLocaleHolder#getLocale()
 * @see SimpleTimeZoneAwareLocaleContext
 */
public class SimpleLocaleContext implements LocaleContext {

  private final Locale locale;

  /**
   * Create a new SimpleLocaleContext that exposes the specified Locale. Every {@link #getLocale()}
   * call will return this Locale.
   *
   * @param locale the Locale to expose, or {@code null} for no specific one
   */
  public SimpleLocaleContext(Locale locale) {
    this.locale = locale;
  }

  @Override
  public Locale getLocale() {
    return this.locale;
  }

  @Override
  public String toString() {
    return (this.locale != null ? this.locale.toString() : "-");
  }

}
