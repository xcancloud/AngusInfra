package cloud.xcan.angus.idgen.bid;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BidEnumsTest {

  @Test
  void formatValues() {
    assertThat(Format.valueOf("SEQ")).isEqualTo(Format.SEQ);
    assertThat(Format.valueOf("PREFIX_SEQ")).isEqualTo(Format.PREFIX_SEQ);
    assertThat(Format.valueOf("DATE_SEQ")).isEqualTo(Format.DATE_SEQ);
    assertThat(Format.valueOf("PREFIX_DATE_SEQ")).isEqualTo(Format.PREFIX_DATE_SEQ);
  }

  @Test
  void modeScopeDateFormat() {
    assertThat(Mode.DB).isEqualTo(Mode.valueOf("DB"));
    assertThat(Mode.REDIS).isEqualTo(Mode.valueOf("REDIS"));
    assertThat(Scope.PLATFORM).isEqualTo(Scope.valueOf("PLATFORM"));
    assertThat(Scope.TENANT).isEqualTo(Scope.valueOf("TENANT"));
    assertThat(DateFormat.YYYY).isEqualTo(DateFormat.valueOf("YYYY"));
    assertThat(DateFormat.YYYYMM).isEqualTo(DateFormat.valueOf("YYYYMM"));
    assertThat(DateFormat.YYYYMMDD).isEqualTo(DateFormat.valueOf("YYYYMMDD"));
  }
}
