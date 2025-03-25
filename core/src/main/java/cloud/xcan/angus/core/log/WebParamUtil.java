package cloud.xcan.angus.core.log;

import static cloud.xcan.angus.spec.SpecConstant.UTF8;

import cloud.xcan.angus.core.utils.GsonUtils;
import cloud.xcan.angus.spec.experimental.BizConstant.AuthKey;
import cloud.xcan.angus.spec.http.ContentType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StreamUtils;

/**
 * @author liuxiaolong
 */
@Slf4j
public class WebParamUtil {

  public static Map<String, Object> getReqParams(HttpServletRequest request,
      String[] ignoreParams) {
    Map<String, Object> map = new HashMap<String, Object>();
    Enumeration paramNames = request.getParameterNames();
    String paramName;
    String[] paramValues;
    while (paramNames.hasMoreElements()) {
      paramName = (String) paramNames.nextElement();
      paramValues = request.getParameterValues(paramName);

      // 脱敏
      // 默认脱敏参数
      if (AuthKey.ACCESS_TOKEN.equals(paramName)
          && !ObjectUtils.isEmpty(paramValues)) {
        map.put(paramName, paramValues[0]
            .replace(paramValues[0].substring(0, paramValues[0].lastIndexOf("-")),
                "********-****-****-****"));
        continue;
      }
      if (AuthKey.PASSD.equals(paramName)
          || AuthKey.CLIENT_SECRET.equals(paramName)) {
        map.put(paramName, "********");
        continue;
      }
      // 自定义脱敏参数
      if (!ObjectUtils.isEmpty(ignoreParams)) {
        if (Arrays.binarySearch(ignoreParams, paramName) > 0) {
          map.put(paramName, "********");
          continue;
        }
      }

      if (!ObjectUtils.isEmpty(paramValues)) {
        String paramValue = paramValues[0];
        if (paramValue.length() != 0) {
          map.put(paramName, paramValue);
        }
      }
    }
    return map;
  }

  public static Object getReqBody(ProceedingJoinPoint joinPoint) {
    if (joinPoint.getArgs().length > 0) {
      List<Object> args = new ArrayList<>(joinPoint.getArgs().length);
      for (Object o : joinPoint.getArgs()) {
        // 获取参数, 只取自定义的参数
        if (o instanceof HttpServletRequest || o instanceof HttpServletResponse) {
          continue;
        }
        args.add(o);
      }
      return args;
    }
    return null;
  }

  /**
   * getInputStream() has already been called for this request
   */
  @Deprecated
  public static Object getReqBody(HttpServletRequest request) {
    if (isFileContentType(request)) {
      return null;
    }
    String reqBody = "";
    try {
      reqBody = StreamUtils.copyToString(request.getInputStream(), UTF8);
    } catch (Exception e) {
      log.info("", e);
    }
    return GsonUtils.fromJson(reqBody, Object.class);
  }

  public static boolean isFileContentType(HttpServletRequest request) {
    return ObjectUtils.isEmpty(request.getContentType()) || request.getContentType()
        .startsWith(ContentType.TYPE_FORM_DATA);
  }

  public static boolean isFileContentType(HttpServletResponse response) {
    return ObjectUtils.isEmpty(response.getContentType()) || response.getContentType()
        .startsWith(ContentType.TYPE_OCTET_STREAM);
  }
}
