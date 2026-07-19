package cloud.xcan.angus.spec.locale;

import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_LANGUAGE;
import static org.assertj.core.api.Assertions.assertThat;

import cloud.xcan.angus.spec.SpecConstant;
import org.junit.jupiter.api.Test;

/**
 * Guard against default-language drift across BizConstant / SpecConstant / SupportedLanguage.
 */
class DefaultLanguageSourcesAlignedTest {

  @Test
  void defaultLanguageSources_areAligned() {
    assertThat(SupportedLanguage.contain(DEFAULT_LANGUAGE))
        .as("BizConstant.DEFAULT_LANGUAGE must be a SupportedLanguage enum name")
        .isTrue();

    SupportedLanguage fromBiz = SupportedLanguage.defaultLanguage();
    assertThat(fromBiz.getValue()).isEqualTo(DEFAULT_LANGUAGE);
    assertThat(SupportedLanguage.defaultLocale()).isEqualTo(fromBiz.toLocale());
    assertThat(SpecConstant.DEFAULT_LOCALE)
        .as("SpecConstant.DEFAULT_LOCALE must derive from SupportedLanguage.defaultLanguage()")
        .isEqualTo(fromBiz.toLocale());
  }
}
