package cloud.xcan.angus.spec.utils;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

class DoubleFormatTest {

  @Test
  void decimalOrNan() {
    assertThat(DoubleFormatUtils.decimalOrNan(Double.NaN)).isEqualTo("NaN");
    assertThat(DoubleFormatUtils.decimalOrNan(123456.1234567)).isEqualTo("123456.123457");
    assertThat(DoubleFormatUtils.decimalOrNan(123456)).isEqualTo("123456");
    assertThat(DoubleFormatUtils.decimalOrNan(0.123)).isEqualTo("0.123");
  }

  @Test
  void wholeOrDecimal() {
    assertThat(DoubleFormatUtils.wholeOrDecimal(123456.1234567)).isEqualTo("123456.123457");
    assertThat(DoubleFormatUtils.wholeOrDecimal(1)).isEqualTo("1");
  }

  @Test
  void decimal() {
    assertThat(DoubleFormatUtils.decimal(123456.1234567)).isEqualTo("123456.123457");
    assertThat(DoubleFormatUtils.decimal(123456)).isEqualTo("123456.0");
    assertThat(DoubleFormatUtils.decimal(0.123)).isEqualTo("0.123");
  }

  @Test
  void noScientificNotation() {
    assertThat(DoubleFormatUtils.wholeOrDecimal(4.6875392E7)).isEqualTo("46875392");
    assertThat(DoubleFormatUtils.decimalOrNan(4.6875392E7)).isEqualTo("46875392");
  }
}
