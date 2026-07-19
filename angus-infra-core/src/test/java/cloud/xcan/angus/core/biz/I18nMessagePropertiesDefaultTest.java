package cloud.xcan.angus.core.biz;

import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_LANGUAGE;
import static org.assertj.core.api.Assertions.assertThat;

import cloud.xcan.angus.spec.locale.SupportedLanguage;
import org.junit.jupiter.api.Test;

class I18nMessagePropertiesDefaultTest {

  @Test
  void defaultLocale_followsBizConstant() {
    I18nMessageProperties properties = new I18nMessageProperties();
    assertThat(properties.getDefaultLocale()).isEqualTo(DEFAULT_LANGUAGE);
    assertThat(SupportedLanguage.contain(properties.getDefaultLocale())).isTrue();
  }
}
