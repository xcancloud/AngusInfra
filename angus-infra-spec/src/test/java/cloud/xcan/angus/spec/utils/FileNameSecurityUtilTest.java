package cloud.xcan.angus.spec.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FileNameSecurityUtilTest {

  @Test
  void shouldKeepChineseJapaneseKoreanAndOtherUnicodeLanguageCharacters() {
    String originalFileName = "报告-資料_テスト-테스트-résumé.txt";

    String sanitizedFileName = FileNameSecurityUtil.sanitizeFileName(originalFileName);

    assertThat(sanitizedFileName).isEqualTo(originalFileName);
  }

  @Test
  void shouldExtractFileNameAndReplaceUnsafeCharacters() {
    String sanitizedFileName = FileNameSecurityUtil.sanitizeFileName("/tmp/upload/报告<2026>?.txt");

    assertThat(sanitizedFileName).isEqualTo("报告_2026__.txt");
  }

  @Test
  void shouldNormalizeFullWidthCharacters() {
    String sanitizedFileName = FileNameSecurityUtil.sanitizeFileName("ＡＢＣ１２３－报告.txt");

    assertThat(sanitizedFileName).isEqualTo("ABC123_报告.txt");
  }

  @Test
  void shouldReplaceEmojiAsUnsafeCharacterByCodePoint() {
    String sanitizedFileName = FileNameSecurityUtil.sanitizeFileName("报告😀.txt");

    assertThat(sanitizedFileName).isEqualTo("报告_.txt");
  }
}
