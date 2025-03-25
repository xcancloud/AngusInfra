package cloud.xcan.angus.security;

import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isCloudServiceEdition;
import static cloud.xcan.angus.spec.experimental.BizConstant.AuthKey.BEARER_TOKEN_TYPE;
import static cloud.xcan.angus.spec.experimental.BizConstant.Header.AUTHORIZATION;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getAuthorization;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.app.verify.ver.Guard;
import cloud.xcan.angus.security.remote.ClientSignOpen2pRemote;
import cloud.xcan.angus.security.remote.dto.ClientSigninDto;
import cloud.xcan.angus.security.remote.vo.ClientSignVo;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.typelevel.v.Str0;

@Slf4j
public class FeignOpen2pAuthInterceptor implements RequestInterceptor {

  private String open2pToken;

  private final ClientSignOpen2pRemote clientSign2pOpenRemote;

  public FeignOpen2pAuthInterceptor(ClientSignOpen2pRemote clientSign2pOpenRemote) {
    this.clientSign2pOpenRemote = clientSign2pOpenRemote;
  }

  @Override
  public void apply(RequestTemplate template) {
    if (template.path().startsWith(
        new Str0(new long[]{0x3B4B5579260CAC87L, 0x7D6D17E8233465F6L, 0xBD91B361BD0DAC9CL})
            .toString() /* => "/openapi2p" */)) {
      template.header(AUTHORIZATION, getToken());
    }
  }

  private String getToken() {
    // Test support for cloud service edition
    if (isCloudServiceEdition() && isNotEmpty(getAuthorization())) {
      return getAuthorization();
    }

    if (this.open2pToken != null) {
      return this.open2pToken;
    }

    try {
      Guard guard = new Guard(System.getProperty(
          new Str0(new long[]{0x54790D0FA7B76F38L, 0xB9E46AF8A78E78BAL, 0xD12C1E2252DFD93BL})
              .toString() /* => "LICENSE_PASS_KEY" */) + new Str0(
          new long[]{0xA9BC583DF4856B4L, 0x7BB5D94B7DB1A12CL, 0x96B1F6DBA646B3E0L,
              0xE4C61A9E7EFF86EEL}).toString() /* SALT => ".435E9A3AB63ED118" */,
          System.getProperty(new Str0(
              new long[]{0x5CC49F98C5B79423L, 0x9E229ED62365552BL, 0x6CB5ECDA361155EFL,
                  0x5EC1A30F4C2E0F61L}).toString() /* => "MAIN_LICENSE_PATH" */));
      try {
        ClientSignVo result = clientSign2pOpenRemote.signin(
            new ClientSigninDto().setClientId(guard.var126())
                .setClientSecret(guard.var127())
                .setScope(new Str0(
                    new long[]{0xF6A8996A31956709L, 0xEB88405CAAFD11A3L, 0xECCBFA5DAF2B5F55L})
                    .toString() /* => "2private_trust" */)).orElseContentThrow();
        this.open2pToken = BEARER_TOKEN_TYPE + " " + result.getAccessToken();
        return this.open2pToken;
      } catch (Exception e) {
        log.warn(new Str0(new long[]{0x5FCCC37F83496ECL, 0xC7AEB45FB3DEAEE8L, 0xD38753E33A84D57FL,
                0xC7DEFD7034DA5473L, 0x7E4454ABF8B67762L, 0x77CD090011F8B14L})
                .toString() /* => "Query store application data is abnormal" */
            , e);
      }
    } catch (Exception e) {
      log.error(new Str0(new long[]{0x93C47F86E38AEA6BL, 0xF2C06EA77E870C80L, 0xF684A787EA7B256CL,
          0x82B425F78FBA5344L, 0xE80F7A2FA48F626AL, 0x18F5098C9CB94D6EL, 0x63379206FD03401BL,
          0x7C549DB933E7916EL, 0x51F860E9597D0AC8L})
          .toString() /* => "Authorized access to store to obtain credentials is abnormal" */ + e);
    }
    return null;
  }

}
