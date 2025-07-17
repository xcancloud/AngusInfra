package cloud.xcan.angus.security;

import static cloud.xcan.angus.spec.experimental.BizConstant.AuthKey.BEARER_TOKEN_TYPE;
import static cloud.xcan.angus.spec.experimental.BizConstant.AuthKey.INNER_API_TOKEN_CLIENT_SCOPE;
import static cloud.xcan.angus.spec.experimental.BizConstant.Header.AUTHORIZATION;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.api.obf.Str0;
import cloud.xcan.angus.remote.message.SysException;
import cloud.xcan.angus.security.model.remote.dto.ClientSignInDto;
import cloud.xcan.angus.security.model.remote.vo.ClientSignInVo;
import cloud.xcan.angus.security.remote.ClientSignInnerApiRemote;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;

@Slf4j
public class FeignInnerApiAuthInterceptor implements RequestInterceptor {

  private String innerApiToken;
  private long lastedAuthTime = 0;
  /**
   * NOTE: The validity period of the registered client token cannot be less than 15 minutes.
   */
  private static final long maxAuthTimeInterval = 15 * 60 * 1000;

  private final ClientSignInnerApiRemote clientSignInnerApiRemote;
  private final ConfigurableEnvironment configurableEnvironment;

  public FeignInnerApiAuthInterceptor(ClientSignInnerApiRemote clientSignInnerApiRemote,
      ConfigurableEnvironment configurableEnvironment) {
    this.clientSignInnerApiRemote = clientSignInnerApiRemote;
    this.configurableEnvironment = configurableEnvironment;
  }

  @Override
  public void apply(RequestTemplate template) {
    if (template.path().startsWith(
        new Str0(new long[]{0x5AEF5BBB0A956300L, 0x10635E7DEFBB4F34L,
            0x843A47A2DCB5427FL}).toString() /* => "/innerapi" */)) {
      template.header(AUTHORIZATION, getToken());
    }
  }

  private synchronized String getToken() {
    if (this.innerApiToken != null
        // Control to obtain once every 15 minutes
        && System.currentTimeMillis() - this.lastedAuthTime <= maxAuthTimeInterval) {
      return this.innerApiToken;
    }

    try {
      String clientId = configurableEnvironment.getProperty(new Str0(
              new long[]{0xA4A3DFFE63E793EDL, 0x42A229398298E137L, 0x89C39B017468BA6CL,
                  0xEEBECE43F1DD8FECL,
                  0xFD5889DFB33A78C6L}).toString() /* => "OAUTH2_INNER_API_CLIENT_ID" */,
          configurableEnvironment.getProperty(new Str0(
              new long[]{0x608746A7C72E6744L, 0xEE1865EE85F5402FL, 0x150C960220F4CDE7L,
                  0xBCD2D68C7EC35ADEL,
                  0x5AF747C4DD38CF8CL}).toString() /* => "OAUTH2_INTROSPECT_CLIENT_ID" */));
      String clientSecret = configurableEnvironment.getProperty(new Str0(
              new long[]{0x63AA8BE08F802DECL, 0xB34AD15C5DD57F6FL, 0xE668234D712462E1L,
                  0x366530F64EED189BL,
                  0x2E03BD2E29019BF8L}).toString() /* => "OAUTH2_INNER_API_CLIENT_SECRET" */,
          configurableEnvironment.getProperty(new Str0(
              new long[]{0xC34A5C33A818AAFAL, 0x7DFB824060C68CB0L, 0x34BEA2C11E7D0913L,
                  0x14C5027BF1A09458L,
                  0xF44CD289EEBF6322L}).toString() /* => "OAUTH2_INTROSPECT_CLIENT_SECRET" */));

      if (isEmpty(clientId) || isEmpty(clientSecret)) {
        throw new SysException(new Str0(
            new long[]{0x59531F583F786268L, 0x624DEB1A94D38574L, 0x619A40DC42E7AF09L,
                0x602D638522A61D1CL, 0xEB82552077A2AF0CL, 0x687721806D40E6E6L, 0x9387D19B815A2FC3L,
                0xDECBF158CCF816FFL}).toString() /* => "The inner API authentication client is not configured" */);
      }

      ClientSignInVo result = clientSignInnerApiRemote.signin(
          new ClientSignInDto().setClientId(clientId).setClientSecret(clientSecret)
              .setScope(INNER_API_TOKEN_CLIENT_SCOPE)).orElseContentThrow();
      this.innerApiToken = BEARER_TOKEN_TYPE + " " + result.getAccessToken();
      this.lastedAuthTime = System.currentTimeMillis();
      return this.innerApiToken;
    } catch (Exception e) {
      log.error(new Str0(new long[]{0x92DEC6DBFDA72EB2L, 0x3EF58D3DA0284DF5L,
          0x9302A2107A4C3AE2L, 0x7540DE3CF4E60FCAL,
          0xD982E729C5D60A7DL}).toString() /* => "Inner API authentication failed" */, e);
    }
    return "";
  }

}
