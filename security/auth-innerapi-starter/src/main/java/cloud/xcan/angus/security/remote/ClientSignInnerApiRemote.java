package cloud.xcan.angus.security.remote;

import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.security.model.remote.dto.ClientSignInDto;
import cloud.xcan.angus.security.model.remote.vo.ClientSignInVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author XiaoLong Liu
 */
@FeignClient(name = "ClientSignInnerApi", url = "${GM_APIS_URL_PREFIX}")
public interface ClientSignInnerApiRemote {

  @Operation(
      summary = "Client sign-in", description = "Client sign-in for private and 3rd authorization.",
      operationId = "client:signin:pub"
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Sign-in successfully")
  })
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/pubapi/v1/auth/client/signin")
  ApiLocaleResult<ClientSignInVo> signin(@Valid @RequestBody ClientSignInDto dto);

}
