package cloud.xcan.angus.core.biz;

import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_LANGUAGE;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for DB-backed config-data i18n.
 *
 * <p>Prefix: {@code angus.i18n.message}</p>
 *
 * @author XiaoLong Liu
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "angus.i18n.message")
public class I18nMessageProperties {

  /**
   * Master switch for resolver and {@code @MessageJoin} aspect.
   */
  private boolean enabled = true;

  /**
   * Application default locale name ({@code SupportedLanguage} enum name). Defaults to
   * {@link cloud.xcan.angus.spec.experimental.BizConstant#DEFAULT_LANGUAGE} ({@code en});
   * override via {@code angus.i18n.message.default-locale}.
   */
  private String defaultLocale = DEFAULT_LANGUAGE;

  /**
   * When {@code true}, {@code @MessageJoin} skips work if the request locale equals
   * {@link #defaultLocale} (VO already holds default-locale text). Set {@code false} when VO
   * fields hold message keys that must always be resolved.
   */
  private boolean skipDefaultLocale = true;

  private Cache cache = new Cache();

  @Getter
  @Setter
  public static class Cache {

    /**
     * Enable type-level Caffeine cache. Field-level {@code enabledCache=false} still bypasses
     * cache for that field.
     */
    private boolean enabled = true;

    private long maximumSize = 2048L;

    private long expireAfterWriteMinutes = 30L;

    private long expireAfterAccessMinutes = 10L;
  }
}
