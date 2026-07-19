package cloud.xcan.angus.spec.locale;

import static cloud.xcan.angus.spec.SpecConstant.DEFAULT_LOCALE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class SdfLocaleHolderDefaultTest {

  @AfterEach
  void reset() {
    SdfLocaleHolder.resetLocaleContext();
    SdfLocaleHolder.setDefaultLocale(DEFAULT_LOCALE);
  }

  @Test
  void getLocale_withoutThreadContext_usesFrameworkDefaultEnglish() {
    SdfLocaleHolder.resetLocaleContext();
    assertThat(SdfLocaleHolder.getLocale()).isEqualTo(Locale.ENGLISH);
  }

  @Test
  void getLocale_ignoresJvmDefaultWhenFrameworkDefaultSet() {
    Locale previousJvm = Locale.getDefault();
    try {
      Locale.setDefault(Locale.GERMANY);
      SdfLocaleHolder.resetLocaleContext();
      SdfLocaleHolder.setDefaultLocale(DEFAULT_LOCALE);
      assertThat(SdfLocaleHolder.getLocale()).isEqualTo(Locale.ENGLISH);
    } finally {
      Locale.setDefault(previousJvm);
    }
  }
}
