package cloud.xcan.angus.security.handler;

import static cloud.xcan.angus.remote.ApiConstant.ECode.PROTOCOL_ERROR_CODE;
import static cloud.xcan.angus.remote.ApiConstant.EXT_EKEY_NAME;
import static cloud.xcan.angus.remote.message.http.Forbidden.M.FORBIDDEN;
import static cloud.xcan.angus.remote.message.http.Forbidden.M.FORBIDDEN_KEY;
import static cloud.xcan.angus.security.handler.CustomAuthenticationEntryPoint.writeJsonUtf8Result;

import cloud.xcan.angus.remote.ApiResult;
import cloud.xcan.angus.spec.locale.MessageHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * @author XiaoLong Liu
 * @see AccessDeniedHandler
 */
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      AccessDeniedException accessDeniedException) throws ServletException {
    Map<String, Object> ext = new HashMap<>();
    ext.put(EXT_EKEY_NAME, FORBIDDEN_KEY);
    ApiResult<?> apiResult = ApiResult.error(PROTOCOL_ERROR_CODE, MessageHolder.message(FORBIDDEN),
        accessDeniedException.getMessage(), ext);
    writeJsonUtf8Result(objectMapper, response, HttpServletResponse.SC_FORBIDDEN, apiResult);
  }
}
