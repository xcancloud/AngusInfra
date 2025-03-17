package cloud.xcan.sdf.spec.locale;

import java.util.Locale;
import java.util.TimeZone;

/**
 * Simple implementation of the {@link TimeZoneAwareLocaleContext} interface, always returning a
 * specified {@code Locale} and {@code TimeZone}.
 *
 * <p>Note: Prefer the use of {@link SimpleLocaleContext} when only setting
 * a Locale but no TimeZone.
 *
 * @see SdfLocaleHolder#setLocaleContext
 * @see SdfLocaleHolder#getTimeZone()
 */
public class SimpleTimeZoneAwareLocaleContext extends SimpleLocaleContext implements
    TimeZoneAwareLocaleContext {

  private final TimeZone timeZone;

  /**
   * Create a new SimpleTimeZoneAwareLocaleContext that exposes the specified Locale and TimeZone.
   * Every {@link #getLocale()} call will return the given Locale, and every {@link #getTimeZone()}
   * call will return the given TimeZone.
   *
   * @param locale   the Locale to expose
   * @param timeZone the TimeZone to expose
   */
  public SimpleTimeZoneAwareLocaleContext(Locale locale, TimeZone timeZone) {
    super(locale);
    this.timeZone = timeZone;
  }

  @Override
  public TimeZone getTimeZone() {
    return this.timeZone;
  }

  @Override
  public String toString() {
    return super.toString() + " " + (this.timeZone != null ? this.timeZone.toString() : "-");
  }

}
