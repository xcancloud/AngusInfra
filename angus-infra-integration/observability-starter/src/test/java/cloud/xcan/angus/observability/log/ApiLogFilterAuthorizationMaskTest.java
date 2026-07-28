package cloud.xcan.angus.observability.log;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApiLogFilterAuthorizationMaskTest {

  @Test
  void masksJwtBearerKeepingHeaderAndSignature() {
    assertThat(ApiLogFilter.maskAuthorization("Bearer aaa.bbb.ccc"))
        .isEqualTo("Bearer aaa.********.ccc");
  }

  @Test
  void masksOpaqueBearer() {
    assertThat(ApiLogFilter.maskAuthorization("Bearer opaque-token-value"))
        .isEqualTo("Bearer ********");
  }

  @Test
  void masksBasicCredentials() {
    assertThat(ApiLogFilter.maskAuthorization("Basic dXNlcjpwYXNz"))
        .isEqualTo("Basic ********");
  }

  @Test
  void masksBareCredential() {
    assertThat(ApiLogFilter.maskAuthorization("only-token"))
        .isEqualTo("********");
  }
}
