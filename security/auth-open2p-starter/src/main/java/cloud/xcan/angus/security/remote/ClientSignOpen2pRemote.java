package cloud.xcan.angus.security.remote;

import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.security.remote.dto.ClientSigninDto;
import cloud.xcan.angus.security.remote.vo.ClientSignVo;
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
@FeignClient(name = "XCAN-GM.BOOT", url = "${xcan.cloud.gmApiUrlPrefix}")
public interface ClientSignOpen2pRemote {

  @Operation(
      summary = "Client signin", description = "Client signin for private and 3rd authorization.",
      operationId = "client:signin:pub"
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Signin successfully")
  })
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/pubapi/v1/client/signin")
  ApiLocaleResult<ClientSignVo> signin(@Valid @RequestBody ClientSigninDto dto);

}
