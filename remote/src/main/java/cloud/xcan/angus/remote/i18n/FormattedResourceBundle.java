package cloud.xcan.angus.remote.i18n;

import static java.util.Objects.requireNonNull;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Wraps a {@link ResourceBundle} in order to format strings with
 * {@link MessageFormat#format(String, Object[])}. This class is immutable.
 */
public final class FormattedResourceBundle {

  private final ResourceBundle bundle;

  private FormattedResourceBundle(final ResourceBundle bundle) {
    this.bundle = bundle;
  }

  /**
   * Returns a formatted resource bundle for the given base name.
   *
   * @param baseName the base name of the resource bundle.
   */
  public static FormattedResourceBundle bundle(String baseName) {
    return bundle(baseName, Locale.getDefault());
  }

  /**
   * Returns a formatted resource bundle for the given base name with the given locale.
   *
   * @param baseName the base name of the resource bundle.
   * @param locale   the locale to use for the lookup.
   */
  public static FormattedResourceBundle bundle(String baseName, Locale locale) {
    return new FormattedResourceBundle(ResourceBundle.getBundle(baseName, locale));
  }

  /**
   * Wraps the given resource bundle for formatting strings.
   *
   * @param bundle the resource bundle to wrap.
   */
  public static FormattedResourceBundle wrap(ResourceBundle bundle) {
    return new FormattedResourceBundle(requireNonNull(bundle));
  }

  /**
   * Looks up a string resource identified by {@code key} in this resource bundle and formats it
   * with the given {@code args} using {@link String#format(String, Object[])}.
   */
  public String format(String key, Object... args) {
    return MessageFormat.format(lookup(key), args);
  }

  /**
   * Looks up a string resource identified by {@code key} in this resource bundle.
   */
  public String lookup(String key) {
    return bundle.getString(key);
  }
}
