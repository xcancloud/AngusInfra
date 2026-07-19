package cloud.xcan.angus.core.biz;

/**
 * Contract for a persisted i18n config message row.
 *
 * <p>Lookup identity is {@link #getMessageKey()} (stable code), not display text.
 * Legacy rows may leave {@code message_key} empty and fall back to
 * {@link #getDefaultMessage()}.</p>
 *
 * @author XiaoLong Liu
 * @see I18nMessageResolver
 */
public interface I18nMessage {

  String getType();

  /**
   * Locale tag stored in DB, preferably a {@code SupportedLanguage} name such as {@code zh_CN}
   * or {@code en}.
   */
  String getLanguage();

  /**
   * Stable message key. Implementations should fall back to {@link #getDefaultMessage()} when the
   * key column is blank (legacy data).
   */
  String getMessageKey();

  /**
   * Optional default-locale display text / legacy lookup aid.
   */
  String getDefaultMessage();

  /**
   * Translated text for {@link #getLanguage()}.
   */
  String getI18nMessage();

}
