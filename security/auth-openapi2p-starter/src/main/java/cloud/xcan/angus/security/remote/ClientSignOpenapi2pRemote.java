package cloud.xcan.angus.security.remote;

import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.security.model.remote.dto.ClientSigninDto;
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
@FeignClient(name = "ANGUSGM", url = "${xcan.cloud.gmApiUrlPrefix}")
public interface ClientSignOpenapi2pRemote {

  @Operation(
      summary = "Client signin", description = "Client signin for private and 3rd authorization.",
      operationId = "client:signin:pub"
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Signin successfully")
  })
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/pubapi/v1/client/signin")
  ApiLocaleResult<ClientSignInVo> signin(@Valid @RequestBody ClientSigninDto dto);

}
