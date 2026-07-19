package cloud.xcan.angus.spec.locale;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import org.junit.jupiter.api.Test;

class SupportedLanguageTest {

  @Test
  void of_normalizesEnglishVariants() {
    assertThat(SupportedLanguage.of("en")).isEqualTo(SupportedLanguage.en);
    assertThat(SupportedLanguage.of("en-US")).isEqualTo(SupportedLanguage.en);
    assertThat(SupportedLanguage.of("en_US")).isEqualTo(SupportedLanguage.en);
    assertThat(SupportedLanguage.of("en-GB")).isEqualTo(SupportedLanguage.en);
    assertThat(SupportedLanguage.of("en;q=0.9")).isEqualTo(SupportedLanguage.en);
  }

  @Test
  void of_normalizesChineseVariantsIncludingTraditional() {
    assertThat(SupportedLanguage.of("zh_CN")).isEqualTo(SupportedLanguage.zh_CN);
    assertThat(SupportedLanguage.of("zh-CN")).isEqualTo(SupportedLanguage.zh_CN);
    assertThat(SupportedLanguage.of("zh")).isEqualTo(SupportedLanguage.zh_CN);
    assertThat(SupportedLanguage.of("zh-TW")).isEqualTo(SupportedLanguage.zh_CN);
    assertThat(SupportedLanguage.of("zh_HK")).isEqualTo(SupportedLanguage.zh_CN);
    assertThat(SupportedLanguage.of("zh-HK")).isEqualTo(SupportedLanguage.zh_CN);
  }

  @Test
  void of_malformedTagsFallBackToDefault() {
    assertThat(SupportedLanguage.of("en_")).isEqualTo(SupportedLanguage.en);
    assertThat(SupportedLanguage.of("_")).isEqualTo(SupportedLanguage.defaultLanguage());
    assertThat(SupportedLanguage.of(";;;")).isEqualTo(SupportedLanguage.defaultLanguage());
    assertThat(SupportedLanguage.of("")).isEqualTo(SupportedLanguage.defaultLanguage());
    assertThat(SupportedLanguage.of(null)).isEqualTo(SupportedLanguage.defaultLanguage());
  }

  @Test
  void of_unknownFallsBackToDefault() {
    assertThat(SupportedLanguage.of("fr")).isEqualTo(SupportedLanguage.defaultLanguage());
  }

  @Test
  void tryParse_distinguishesUnsupported() {
    assertThat(SupportedLanguage.tryParse("en-US")).contains(SupportedLanguage.en);
    assertThat(SupportedLanguage.tryParse("zh-TW")).contains(SupportedLanguage.zh_CN);
    assertThat(SupportedLanguage.tryParse("fr")).isEmpty();
    assertThat(SupportedLanguage.tryParse("en_")).contains(SupportedLanguage.en);
    assertThat(SupportedLanguage.tryParse("_")).isEmpty();
  }

  @Test
  void matches_acceptsBcp47WithoutTreatingUnknownAsSupported() {
    assertThat(SupportedLanguage.matches("en-US")).isTrue();
    assertThat(SupportedLanguage.matches("zh-CN")).isTrue();
    assertThat(SupportedLanguage.matches("fr")).isFalse();
    assertThat(SupportedLanguage.contain("en-US")).isFalse();
    assertThat(SupportedLanguage.contain("en")).isTrue();
  }

  @Test
  void safeLanguage_localeUsMapsToEn() {
    assertThat(SupportedLanguage.safeLanguage(Locale.US)).isEqualTo(SupportedLanguage.en);
    assertThat(SupportedLanguage.safeLanguage(Locale.ENGLISH)).isEqualTo(SupportedLanguage.en);
    assertThat(SupportedLanguage.safeLanguage(Locale.CHINA)).isEqualTo(SupportedLanguage.zh_CN);
    assertThat(SupportedLanguage.safeLanguage(Locale.TAIWAN)).isEqualTo(SupportedLanguage.zh_CN);
  }

  @Test
  void lookupFromAcceptLanguage_respectsQWeight() {
    Locale locale = SupportedLanguage.lookupFromAcceptLanguage("en;q=0.5, zh-CN;q=0.9");
    assertThat(SupportedLanguage.safeLanguage(locale)).isEqualTo(SupportedLanguage.zh_CN);
  }

  @Test
  void lookupFromAcceptLanguage_enUs() {
    Locale locale = SupportedLanguage.lookupFromAcceptLanguage("en-US,en;q=0.9");
    assertThat(SupportedLanguage.safeLanguage(locale)).isEqualTo(SupportedLanguage.en);
    assertThat(locale.getLanguage()).isEqualTo("en");
  }

  @Test
  void lookupFromAcceptLanguage_zhTwMapsToZhCn() {
    Locale locale = SupportedLanguage.lookupFromAcceptLanguage("zh-TW,zh;q=0.9");
    assertThat(SupportedLanguage.safeLanguage(locale)).isEqualTo(SupportedLanguage.zh_CN);
  }

  @Test
  void normalizeTag_stripsEmptyRegion() {
    assertThat(SupportedLanguage.normalizeTag("en_")).isEqualTo("en");
    assertThat(SupportedLanguage.normalizeTag(" en__US ;q=0.8 ")).isEqualTo("en_US");
  }
}
