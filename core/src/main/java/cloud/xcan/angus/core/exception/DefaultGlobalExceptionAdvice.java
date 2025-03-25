package cloud.xcan.angus.core.exception;


import static cloud.xcan.angus.core.utils.EventUtils.assembleExceptionEvent;
import static cloud.xcan.angus.remote.ApiConstant.ECode.BUSINESS_ERROR_EVENT_CODE;
import static cloud.xcan.angus.remote.ApiConstant.ECode.PROTOCOL_ERROR_EVENT_CODE;
import static cloud.xcan.angus.remote.ApiConstant.ECode.QUOTA_ERROR_EVENT_CODE;
import static cloud.xcan.angus.remote.ApiConstant.ECode.SECURITY_FORBIDDEN_EVENT_CODE;
import static cloud.xcan.angus.remote.ApiConstant.ECode.SECURITY_UNAUTHORIZED_EVENT_CODE;
import static cloud.xcan.angus.remote.ApiConstant.ECode.SYSTEM_ERROR_EVENT_CODE;
import static cloud.xcan.angus.remote.ApiConstant.EXT_EKEY_NAME;
import static cloud.xcan.angus.remote.message.CommProtocolException.M.PARAM_BINDING_ERROR;
import static cloud.xcan.angus.remote.message.CommProtocolException.M.PARAM_BINDING_ERROR_KEY;
import static cloud.xcan.angus.remote.message.CommProtocolException.M.PARAM_BINDING_ERROR_T;
import static cloud.xcan.angus.remote.message.CommProtocolException.M.PARAM_BINDING_ERROR_T2;
import static cloud.xcan.angus.remote.message.CommProtocolException.M.PARAM_MISSING;
import static cloud.xcan.angus.remote.message.CommProtocolException.M.PARAM_MISSING_KEY;
import static cloud.xcan.angus.remote.message.CommProtocolException.M.PARAM_PARSING_ERROR;
import static cloud.xcan.angus.remote.message.CommProtocolException.M.PARAM_PARSING_ERROR_KEY;
import static cloud.xcan.angus.remote.message.CommProtocolException.M.PARAM_VALIDATION_ERROR;
import static cloud.xcan.angus.remote.message.CommProtocolException.M.PARAM_VALIDATION_ERROR_KEY;
import static cloud.xcan.angus.remote.message.CommProtocolException.M.PARAM_VALIDATION_ERROR_T;
import static cloud.xcan.angus.remote.message.CommSysException.M.DATABASE_ACCESS_EXCEPTION;
import static cloud.xcan.angus.remote.message.CommSysException.M.DATABASE_ACCESS_EXCEPTION_KEY;
import static cloud.xcan.angus.remote.message.CommSysException.M.DATABASE_API_EXCEPTION;
import static cloud.xcan.angus.remote.message.CommSysException.M.DATABASE_API_EXCEPTION_KEY;
import static cloud.xcan.angus.remote.message.CommSysException.M.DATABASE_INTEGRITY_EXCEPTION;
import static cloud.xcan.angus.remote.message.CommSysException.M.DATABASE_INTEGRITY_EXCEPTION_KEY;
import static cloud.xcan.angus.remote.message.UnknownException.M.UNKNOWN_ERROR;
import static cloud.xcan.angus.remote.message.UnknownException.M.UNKNOWN_ERROR_KEY;
import static cloud.xcan.angus.remote.message.http.Forbidden.M.FORBIDDEN;
import static cloud.xcan.angus.remote.message.http.Forbidden.M.FORBIDDEN_KEY;
import static cloud.xcan.angus.remote.message.http.MediaTypeNotSupported.M.MEDIA_TYPE_NOT_SUPPORTED;
import static cloud.xcan.angus.remote.message.http.MediaTypeNotSupported.M.MEDIA_TYPE_NOT_SUPPORTED_KEY;
import static cloud.xcan.angus.remote.message.http.MediaTypeNotSupported.M.MEDIA_TYPE_NOT_SUPPORTED_T;
import static cloud.xcan.angus.remote.message.http.MethodNotSupported.M.METHOD_NOT_ALLOWED;
import static cloud.xcan.angus.remote.message.http.MethodNotSupported.M.METHOD_NOT_ALLOWED_KEY;
import static cloud.xcan.angus.remote.message.http.MethodNotSupported.M.METHOD_NOT_ALLOWED_T;
import static cloud.xcan.angus.remote.message.http.ResourceNotFound.MKey.HANDLER_NOT_FOUND_KEY;
import static cloud.xcan.angus.remote.message.http.ResourceNotFound.MKey.HANDLER_NOT_FOUND_T;
import static cloud.xcan.angus.remote.message.http.Unauthorized.M.UNAUTHORIZED_KEY;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getDefaultLanguage;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;

import cloud.xcan.angus.api.enums.EventType;
import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.core.biz.exception.QuotaException;
import cloud.xcan.angus.core.disruptor.DisruptorQueueManager;
import cloud.xcan.angus.core.event.CommonEvent;
import cloud.xcan.angus.core.event.source.EventContent;
import cloud.xcan.angus.remote.ApiResult;
import cloud.xcan.angus.remote.ExceptionLevel;
import cloud.xcan.angus.remote.message.AbstractResultMessageException;
import cloud.xcan.angus.remote.message.CommBizException;
import cloud.xcan.angus.remote.message.CommProtocolException;
import cloud.xcan.angus.remote.message.CommSysException;
import cloud.xcan.angus.remote.message.UnknownException;
import cloud.xcan.angus.remote.message.http.Forbidden;
import cloud.xcan.angus.remote.message.http.GatewayTimeout;
import cloud.xcan.angus.remote.message.http.MediaTypeNotSupported;
import cloud.xcan.angus.remote.message.http.MethodNotSupported;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.message.http.ServiceUnavailable;
import cloud.xcan.angus.remote.message.http.Unauthorized;
import cloud.xcan.angus.spec.experimental.BizConstant.Header;
import cloud.xcan.angus.spec.locale.MessageHolder;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

/**
 * Global exception handling.
 *
 * @author XiaoLong Liu
 * @see ResponseEntityExceptionHandler#handleException(Exception, WebRequest)
 * @see DefaultHandlerExceptionResolver
 */
@Slf4j
@ResponseBody
@ControllerAdvice
public class DefaultGlobalExceptionAdvice {

  @Value("${xcan.trace.enabled:false}")
  private Boolean traced = false;

  @Autowired(required = false)
  private DisruptorQueueManager<CommonEvent> commonEventDisruptorQueue;

  /**
   * 200 - OK
   */
  @ResponseStatus(HttpStatus.OK)
  @ExceptionHandler(CommBizException.class)
  public ApiResult<?> handleServiceException(CommBizException e, HttpServletResponse response) {
    return buildApiResult(BUSINESS_ERROR_EVENT_CODE, getTenantLocaleMessage(e), e.getType(),
        e.getLevel(), e, e.getCode(), response);
  }

  /**
   * 200 - OK
   */
  @ResponseStatus(HttpStatus.OK)
  @ExceptionHandler(BizException.class)
  public ApiResult<?> handleCustomException(BizException e, HttpServletResponse response) {
    return buildApiResult(BUSINESS_ERROR_EVENT_CODE, getTenantLocaleMessage(e), e.getType(),
        e.getLevel(), e, e.getCode(), response);
  }

  /**
   * 200 - OK
   */
  @ResponseStatus(HttpStatus.OK)
  @ExceptionHandler(QuotaException.class)
  public ApiResult<?> handleCustomException(QuotaException e, HttpServletResponse response) {
    return buildApiResult(QUOTA_ERROR_EVENT_CODE, getTenantLocaleMessage(e), e.getType(),
        e.getLevel(), e, e.getCode(), response);
  }

  /**
   * 400 - Bad Request
   */
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(CommProtocolException.class)
  public ApiResult<?> handleCommProtocolException(CommProtocolException e,
      HttpServletResponse response) {
    return buildApiResult(PROTOCOL_ERROR_EVENT_CODE, getTenantLocaleMessage(e), e.getType(),
        e.getLevel(), e, e.getEKey(), response);
  }

  /**
   * 400 - Bad Request
   */
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ApiResult<?> handleMissingServletRequestParameterException(
      MissingServletRequestParameterException e, HttpServletResponse response) {
    String message = MessageHolder.message(PARAM_MISSING,
        getDefaultLanguage().toLocale()) + ": " + e.getMessage();
    return buildApiResult(PROTOCOL_ERROR_EVENT_CODE, message, EventType.PROTOCOL,
        ExceptionLevel.IGNORABLE, e, PARAM_MISSING_KEY, response);
  }

  /**
   * 400 - Bad Request
   */
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ApiResult<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e,
      HttpServletResponse response) {
    String message = MessageHolder.message(PARAM_PARSING_ERROR,
        getDefaultLanguage().toLocale()) + ": " + e.getMessage();
    return buildApiResult(PROTOCOL_ERROR_EVENT_CODE, message, EventType.PROTOCOL,
        ExceptionLevel.IGNORABLE, e, PARAM_PARSING_ERROR_KEY, response);
  }

  /**
   * 400 - Bad Request
   */
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ApiResult<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e,
      HttpServletResponse response) {
    FieldError fe = e.getBindingResult().getFieldError();
    if (nonNull(fe)) {
      return buildApiResult(PROTOCOL_ERROR_EVENT_CODE,
          MessageHolder.message(PARAM_VALIDATION_ERROR_T,
              new Object[]{fe.getField(), MessageHolder.message(fe.getDefaultMessage()),
                  getDefaultLanguage().toLocale()}), EventType.PROTOCOL, ExceptionLevel.IGNORABLE,
          e, PARAM_VALIDATION_ERROR_KEY, response);
    }
    return buildApiResult(PROTOCOL_ERROR_EVENT_CODE, MessageHolder.message(PARAM_VALIDATION_ERROR,
            getDefaultLanguage().toLocale()), EventType.PROTOCOL, ExceptionLevel.IGNORABLE, e,
        PARAM_VALIDATION_ERROR_KEY, response);
  }

  /**
   * 400 - Bad Request
   * <p>
   * Thrown when the request parameter binding to the java bean fails.
   */
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(BindException.class)
  public ApiResult<?> handleBindException(BindException e, HttpServletResponse response) {
    FieldError fe = e.getFieldError();
    if (nonNull(fe)) {
      String error = isNotEmpty(fe.getDefaultMessage())
          ? MessageHolder.message(PARAM_BINDING_ERROR_T2, new Object[]{fe.getField(),
          fe.getDefaultMessage()}, getDefaultLanguage().toLocale())
          : MessageHolder.message(PARAM_BINDING_ERROR_T, new Object[]{fe.getField(),
              fe.getRejectedValue()}, getDefaultLanguage().toLocale());
      return buildApiResult(PROTOCOL_ERROR_EVENT_CODE, error, EventType.PROTOCOL,
          ExceptionLevel.IGNORABLE, e, PARAM_BINDING_ERROR_KEY, response);
    }
    return buildApiResult(PROTOCOL_ERROR_EVENT_CODE, MessageHolder.message(PARAM_BINDING_ERROR,
            getDefaultLanguage().toLocale()), EventType.PROTOCOL, ExceptionLevel.IGNORABLE, e,
        PARAM_BINDING_ERROR_KEY, response);
  }

  /**
   * 400 - Bad Request
   */
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(ConstraintViolationException.class)
  public ApiResult<?> handleServiceException(ConstraintViolationException e,
      HttpServletResponse response) {
    Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
    ConstraintViolation<?> violation = violations.iterator().next();
    return buildApiResult(PROTOCOL_ERROR_EVENT_CODE, MessageHolder.message(PARAM_VALIDATION_ERROR_T,
            new Object[]{violation.getPropertyPath(), violation.getMessage(),
                getDefaultLanguage().toLocale()}), EventType.PROTOCOL, ExceptionLevel.IGNORABLE, e,
        PARAM_VALIDATION_ERROR_KEY, response);
  }

  /**
   * 400 - Bad Request
   */
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(ValidationException.class)
  public ApiResult<?> handleValidationException(ValidationException e,
      HttpServletResponse response) {
    return buildApiResult(PROTOCOL_ERROR_EVENT_CODE, MessageHolder.message(PARAM_VALIDATION_ERROR,
            getDefaultLanguage().toLocale()), EventType.PROTOCOL, ExceptionLevel.IGNORABLE, e,
        PARAM_VALIDATION_ERROR_KEY, response);
  }

  /**
   * 401 - Unauthorized
   */
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  @ExceptionHandler(Unauthorized.class)
  public ApiResult<?> handleAuthenticationException(Unauthorized e, HttpServletResponse response) {
    return buildApiResult(SECURITY_UNAUTHORIZED_EVENT_CODE, getTenantLocaleMessage(e),
        EventType.SECURITY, ExceptionLevel.IGNORABLE, e, UNAUTHORIZED_KEY, response);
  }

  /**
   * 403 - Forbidden
   */
  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ExceptionHandler(Forbidden.class)
  public ApiResult<?> handleForbiddenException(Forbidden e, HttpServletResponse response) {
    return buildApiResult(SECURITY_FORBIDDEN_EVENT_CODE, getTenantLocaleMessage(e),
        EventType.SECURITY, ExceptionLevel.WARNING, e, FORBIDDEN_KEY, response);
  }

  /**
   * 403 - AccessDenied
   */
  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ExceptionHandler(AccessDeniedException.class)
  public ApiResult<?> handleAccessDeniedException(AccessDeniedException e,
      HttpServletResponse response) {
    return buildApiResult(SECURITY_FORBIDDEN_EVENT_CODE, MessageHolder.message(FORBIDDEN,
            getDefaultLanguage().toLocale()), EventType.SECURITY, ExceptionLevel.WARNING, e,
        FORBIDDEN_KEY, response);
  }

  /**
   * 404 - Handler not found
   */
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(NoHandlerFoundException.class)
  public ApiResult<?> handleHandlerNotFoundException(NoHandlerFoundException e,
      HttpServletResponse response) {
    return buildApiResult(PROTOCOL_ERROR_EVENT_CODE, MessageHolder.message(HANDLER_NOT_FOUND_T,
            new Object[]{e.getHttpMethod(), e.getRequestURL()}, getDefaultLanguage().toLocale()),
        EventType.PROTOCOL, ExceptionLevel.WARNING, e, HANDLER_NOT_FOUND_KEY, response);
  }

  /**
   * 404 - Resource not found
   */
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(ResourceNotFound.class)
  public ApiResult<?> handleResourceNotFoundException(ResourceNotFound e,
      HttpServletResponse response) {
    return buildApiResult(PROTOCOL_ERROR_EVENT_CODE, MessageHolder.message(e.getMsg(), e.getArgs(),
            getDefaultLanguage().toLocale()), e.getType(), e.getLevel(), e, METHOD_NOT_ALLOWED_KEY,
        response);
  }

  /**
   * 405 - Mode Not Allowed
   */
  @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ApiResult<?> handleMethodException(HttpRequestMethodNotSupportedException e,
      HttpServletResponse response) {
    return buildApiResult(PROTOCOL_ERROR_EVENT_CODE, MessageHolder.message(METHOD_NOT_ALLOWED_T,
            new Object[]{e.getMethod(), Arrays.toString(e.getSupportedMethods())},
            getDefaultLanguage().toLocale()), EventType.PROTOCOL, ExceptionLevel.IGNORABLE, e,
        METHOD_NOT_ALLOWED_KEY, response);
  }

  /**
   * 405 - Mode Not Allowed
   */
  @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
  @ExceptionHandler(MethodNotSupported.class)
  public ApiResult<?> handleMethodException(MethodNotSupported e, HttpServletResponse response) {
    return buildApiResult(PROTOCOL_ERROR_EVENT_CODE,
        MessageHolder.message(METHOD_NOT_ALLOWED, new Object[]{e.getNotSupportedMethod()},
            getDefaultLanguage().toLocale()), EventType.PROTOCOL, ExceptionLevel.IGNORABLE, e,
        METHOD_NOT_ALLOWED_KEY, response);
  }

  /**
   * 409 - Conflict
   */
  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(ResourceExisted.class)
  public ApiResult<?> handleMethodException(ResourceExisted e, HttpServletResponse response) {
    return buildApiResult(PROTOCOL_ERROR_EVENT_CODE, MessageHolder.message(e.getMsg(), e.getArgs(),
        getDefaultLanguage().toLocale()), e.getType(), e.getLevel(), e, e.getEKey(), response);
  }

  /**
   * 415 - Unsupported Media Type
   */
  @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ApiResult<?> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e,
      HttpServletResponse response) {
    return buildApiResult(PROTOCOL_ERROR_EVENT_CODE,
        MessageHolder.message(MEDIA_TYPE_NOT_SUPPORTED_T, new Object[]{e.getContentType()},
            getDefaultLanguage().toLocale()), EventType.PROTOCOL, ExceptionLevel.IGNORABLE, e,
        MEDIA_TYPE_NOT_SUPPORTED_KEY, response);
  }

  /**
   * 415 - Unsupported Media Type
   */
  @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
  @ExceptionHandler(MediaTypeNotSupported.class)
  public ApiResult<?> handleMediaTypeNotSupportedException(MediaTypeNotSupported e,
      HttpServletResponse response) {
    return buildApiResult(PROTOCOL_ERROR_EVENT_CODE, MessageHolder.message(MEDIA_TYPE_NOT_SUPPORTED,
            getDefaultLanguage().toLocale()), EventType.PROTOCOL, ExceptionLevel.IGNORABLE, e,
        MEDIA_TYPE_NOT_SUPPORTED_KEY, response);
  }

  //  /**
  //   * 429 - Too Many Requests
  //   */
  //  @ResponseStatus(HttpStatusSeries.TOO_MANY_REQUESTS)
  //  @ExceptionHandler(MediaTypeNotSupported.class)
  //  public ApiResult<?> handleMediaTypeNotSupportedException(MediaTypeNotSupported e,
  //      HttpServletResponse request) {
  //    return buildApiResult(PROTOCOL_ERROR_EVENT_CODE, MessageHolder.message(MEDIA_TYPE_NOT_SUPPORTED),
  //        e, request);
  //  }

  /**
   * 500 - Internal Server Error
   */
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(InvalidDataAccessApiUsageException.class)
  public ApiResult<?> handleDataApiException(InvalidDataAccessApiUsageException e,
      HttpServletResponse response) {
    return buildApiResult(SYSTEM_ERROR_EVENT_CODE, MessageHolder.message(DATABASE_API_EXCEPTION,
            getDefaultLanguage().toLocale()), EventType.SYSTEM, ExceptionLevel.URGENT, e,
        DATABASE_API_EXCEPTION_KEY, response);
  }

  /**
   * 500 - Internal Server Error
   */
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(DataAccessException.class)
  public ApiResult<?> handleDataAccessException(DataAccessException e,
      HttpServletResponse response) {
    return buildApiResult(SYSTEM_ERROR_EVENT_CODE, MessageHolder.message(DATABASE_ACCESS_EXCEPTION,
            getDefaultLanguage().toLocale()), EventType.SYSTEM, ExceptionLevel.URGENT, e,
        DATABASE_ACCESS_EXCEPTION_KEY, response);
  }

  /**
   * 500 - Internal Server Error
   */
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ApiResult<?> handleDataAccessException(DataIntegrityViolationException e,
      HttpServletResponse response) {
    return buildApiResult(SYSTEM_ERROR_EVENT_CODE,
        MessageHolder.message(DATABASE_INTEGRITY_EXCEPTION,
            getDefaultLanguage().toLocale()), EventType.SYSTEM, ExceptionLevel.URGENT, e,
        DATABASE_INTEGRITY_EXCEPTION_KEY, response);
  }

  /**
   * 500 - Internal Server Error
   */
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(CommSysException.class)
  public ApiResult<?> handleCommSysException(CommSysException e, HttpServletResponse response) {
    return buildApiResult(e.getCode(), getTenantLocaleMessage(e), e.getType(), e.getLevel(),
        e, e.getEKey(), response);
  }

  /**
   * 500 - Internal Server Error
   */
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(Exception.class)
  public ApiResult<?> handleException(Exception e, HttpServletResponse response) {
    return buildApiResult(SYSTEM_ERROR_EVENT_CODE, MessageHolder.message(UNKNOWN_ERROR,
            getDefaultLanguage().toLocale()), EventType.SYSTEM, ExceptionLevel.URGENT, e,
        UNKNOWN_ERROR_KEY, response);
  }

  /**
   * 500 - Internal Server Error
   */
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(UnknownException.class)
  public ApiResult<?> handleException(UnknownException e, HttpServletResponse response) {
    return buildApiResult(SYSTEM_ERROR_EVENT_CODE, getTenantLocaleMessage(e), e.getType(),
        e.getLevel(), e, e.getEKey(), response);
  }

  /**
   * 503 - Service Unavailable
   */
  @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
  @ExceptionHandler(ServiceUnavailable.class)
  public ApiResult<?> handleServiceUnavailableException(ServiceUnavailable e,
      HttpServletResponse response) {
    return buildApiResult(SYSTEM_ERROR_EVENT_CODE, getTenantLocaleMessage(e), e.getType(),
        e.getLevel(),
        e, e.getEKey(), response);
  }

  /**
   * 504 - Gateway Timeout
   */
  @ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
  @ExceptionHandler(GatewayTimeout.class)
  public ApiResult<?> handleGatewayTimeoutException(GatewayTimeout e,
      HttpServletResponse response) {
    return buildApiResult(SYSTEM_ERROR_EVENT_CODE, getTenantLocaleMessage(e), e.getType(),
        e.getLevel(),
        e, e.getEKey(), response);
  }

  private ApiResult<?> buildApiResult(String code, String message, EventType type,
      ExceptionLevel level, Exception e, String eKey, HttpServletResponse response) {
    log.error("----- GlobalExceptionAdvice -----", e);
    Map<String, Object> apiExt = new HashMap<>();

    // SimpleSource eKey Higher priority
    if (e instanceof AbstractResultMessageException) {
      AbstractResultMessageException me = (AbstractResultMessageException) e;
      if (nonNull(me.getEKey())) {
        eKey = me.getEKey();
      }
    }
    if (StringUtils.isBlank(eKey)) {
      eKey = "unknown";
    }

    response.setHeader(Header.E_KEY, eKey);
    apiExt.put(EXT_EKEY_NAME, eKey);

    Object userDefinedMessage = getUserDefinedMessage(e);
    if (Objects.isNull(userDefinedMessage)) {
      userDefinedMessage = this.traced ? getStackTrace(getRootCause(e)) : e.getMessage();
    }

    if (isPushEvent(level)) {
      EventContent event = assembleExceptionEvent(type, code, message, level, eKey,
          userDefinedMessage);
      commonEventDisruptorQueue.add(new CommonEvent(event));
    }
    return ApiResult.error(code, message, userDefinedMessage, apiExt);
  }

  private Object getUserDefinedMessage(Exception e) {
    Object userDefinedMessage = null;
    if (e instanceof BizException) {
      if (nonNull(((BizException) e).getData())) {
        userDefinedMessage = ((BizException) e).getData();
      }
    }
    if (Objects.isNull(userDefinedMessage) && e instanceof AbstractResultMessageException) {
      if (nonNull(((AbstractResultMessageException) e).getCauseMessage())) {
        userDefinedMessage = ((AbstractResultMessageException) e).getCauseMessage();
      }
    }
    return userDefinedMessage;
  }

  private boolean isPushEvent(ExceptionLevel level) {
    return nonNull(commonEventDisruptorQueue) && !ExceptionLevel.IGNORABLE.equals(level);
  }

  private String getTenantLocaleMessage(Exception e) {
    if (e instanceof AbstractResultMessageException) {
      AbstractResultMessageException me = (AbstractResultMessageException) e;
      return MessageHolder.message(me.getMessage(), me.getArgs(), getDefaultLanguage().toLocale());
    }
    return e.getMessage();
  }
}
