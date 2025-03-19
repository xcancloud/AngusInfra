
package cloud.xcan.sdf.core.log;

import static cloud.xcan.sdf.api.message.UnknownException.M.UNKNOWN_ERROR;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.isCmdRequest;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.isDeleteRequest;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.isGetRequest;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.isPatchRequest;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.isPostRequest;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.isPutRequest;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.isUserAction;
import static cloud.xcan.sdf.spec.experimental.Assert.assertNotNull;
import static cloud.xcan.sdf.spec.locale.MessageHolder.message;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.stringSafe;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.message.AbstractResultMessageException;
import cloud.xcan.sdf.core.disruptor.DisruptorQueueManager;
import cloud.xcan.sdf.core.event.OperationEvent;
import cloud.xcan.sdf.core.event.source.UserOperation;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.core.spring.SpringContextHolder;
import cloud.xcan.sdf.spec.experimental.BizConstant.UserKey;
import cloud.xcan.sdf.spec.principal.Principal;
import cloud.xcan.sdf.spec.principal.PrincipalContext;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Collect user operation logs.
 *
 * @author liuxiaolong
 */
@Aspect
@Slf4j
public class OperationLogAspect {

  static final String DELETE_UPDATE_RESOURCE_NAME = "name";

  private final DisruptorQueueManager<OperationEvent> operationEventDisruptorQueue;

  /**
   * TODO Are there performance issues? String replacement instead?
   */
  private final ExpressionParser parser;

  public OperationLogAspect(DisruptorQueueManager<OperationEvent> operationEventDisruptorQueue) {
    assertNotNull(operationEventDisruptorQueue, "OperationEvent queue cannot be null");
    this.operationEventDisruptorQueue = operationEventDisruptorQueue;
    this.parser = new SpelExpressionParser();
  }

  @Pointcut("@annotation(cloud.xcan.sdf.core.log.OperationLog)")
  public void logPointCut() {
  }

  @Around("logPointCut()")
  public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
    if (!isUserAction()) {
      return joinPoint.proceed();
    }
    return log(joinPoint);
  }

  private Object log(ProceedingJoinPoint joinPoint) throws Throwable {
    //    HttpMethod method = ((MethodSignature) joinPoint.getSignature()).getMethod();
    //    method.getParameters()[0].getName()
    OperationLog optLog = ((MethodSignature) joinPoint.getSignature()).getMethod()
        .getAnnotation(OperationLog.class);

    String message = message(optLog.messageKey());
    if (null == message) {
      throw new IllegalArgumentException(
          "Operation message " + optLog.messageKey() + " not found");
    }

    Exception exception = null;
    ApiLocaleResult<?> apiResult = null;
    Object[] args = joinPoint.getArgs();
    Principal principal = PrincipalContext.get();
    UserOperation operationTemplate = getBasicOperation(optLog, principal);
    if (!optLog.batched()) {
      String description;
      if (!isGetRequest(principal)) {
        description = getOperationDescription(args, optLog, message);
        if (StringUtils.isEmpty(description)) {
          return joinPoint.proceed();
        }
        operationTemplate.setDescription(description);
      }
      try {
        apiResult = (ApiLocaleResult<?>) joinPoint.proceed();
        if (isGetRequest(principal)) {
          description = getOperationDescription(apiResult, optLog, message);
          if (StringUtils.isEmpty(description)) {
            return joinPoint.proceed();
          }
          operationTemplate.setDescription(description);
        }
        setSuccessResult(apiResult, operationTemplate);
      } catch (Exception e) {
        exception = e;
        setExceptionResult(operationTemplate, e);
      }
      operationEventDisruptorQueue.add(new OperationEvent(operationTemplate));
    } else {
      List<String> descriptions = new ArrayList<>();
      if (!isGetRequest(principal)) {
        descriptions = getOperationDescriptions(joinPoint.getArgs(), optLog, message);
        if (CollectionUtils.isEmpty(descriptions)) {
          return joinPoint.proceed();
        }
      }
      try {
        apiResult = (ApiLocaleResult<?>) joinPoint.proceed();
        setSuccessResult(apiResult, operationTemplate);
      } catch (Exception e) {
        exception = e;
        setExceptionResult(operationTemplate, e);
      }
      if (isGetRequest(principal)) {
        // Batch query（list and search） only records one log
        String description = getOperationDescription(message);
        operationTemplate.setDescription(description);
        operationEventDisruptorQueue.add(new OperationEvent(operationTemplate));
      } else {
        UserOperation operation;
        for (String description : descriptions) {
          operation = getBasicOperation(optLog, principal);
          operation.setDescription(description);
          operation.setSuccess(operationTemplate.getSuccess());
          operation.setFailureReason(stringSafe(operationTemplate.getFailureReason()));
          operation.setExt(operationTemplate.getExt());
          operationEventDisruptorQueue.add(new OperationEvent(operation));
        }
      }
    }
    if (Objects.nonNull(exception)) {
      throw exception;
    }
    return apiResult;
  }

  private void setSuccessResult(ApiLocaleResult<?> apiResult, UserOperation operationTemplate) {
    // Http 204
    if (Objects.isNull(apiResult)) {
      operationTemplate.setSuccess(true);
      return;
    }
    if (apiResult.isSuccess()) {
      operationTemplate.setSuccess(true);
    } else {
      operationTemplate.setSuccess(false).setFailureReason(apiResult.getMsg());
    }
  }

  private void setExceptionResult(UserOperation operationTemplate, Exception e) {
    if (e instanceof AbstractResultMessageException) {
      AbstractResultMessageException me = (AbstractResultMessageException) e;
      operationTemplate.setSuccess(false).setFailureReason(me.getMessage());
    } else {
      operationTemplate.getExt().put("cause", e.getMessage());
      operationTemplate.setSuccess(false).setFailureReason(stringSafe(message(UNKNOWN_ERROR)));
    }
  }

  private String getOperationDescription(ApiLocaleResult<?> apiResult, OperationLog optLog,
      String message) {
    StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
    evaluationContext.setVariable(UserKey.USER_NAME, PrincipalContext.getUserName());
    if (!EvaluationObject.NONE.equals(optLog.evaluationObject())) {
      evaluationContext.setRootObject(apiResult.getData());
    }
    Expression expression = parser.parseExpression(message);
    return expression.getValue(evaluationContext, String.class);
  }

  private String getOperationDescription(String message) {
    StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
    evaluationContext.setVariable(UserKey.USER_NAME, PrincipalContext.getUserName());
    Expression expression = parser.parseExpression(message);
    return expression.getValue(evaluationContext, String.class);
  }

  private String getOperationDescription(Object[] args, OperationLog optLog, String message) {
    StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
    evaluationContext.setVariable(UserKey.USER_NAME, PrincipalContext.getUserName());
    if (EvaluationObject.NONE.equals(optLog.evaluationObject())) {
      Expression expression = parser.parseExpression(message);
      return expression.getValue(String.class);
    }

    if (ArrayUtils.isEmpty(args)) {
      throw new IllegalArgumentException("Evaluation object arg is empty");
    }

    // Find the first arg as DTO
    Object dto = args[0];
    if (isPostRequest() || isPutRequest() || (isPatchRequest()
        && EvaluationObject.DTO.equals(optLog.evaluationObject()))) {
      // Resource name is required in dto
      evaluationContext.setRootObject(dto);
      Expression expression = parser.parseExpression(message);
      return expression.getValue(evaluationContext, String.class);
    } else if (isDeleteRequest() || isPatchRequest()) {
      // Resource name is not required in dto
      Object repository = SpringContextHolder.getBean(optLog.repositoryBeanName());
      if (repository instanceof BaseRepository) {
        BaseRepository baseRepository = (BaseRepository) repository;
        String name = baseRepository.findNameById(dto);
        if (StringUtils.isNotEmpty(name)) {
          evaluationContext.setVariable(DELETE_UPDATE_RESOURCE_NAME, name);
          Expression expression = parser.parseExpression(message);
          return expression.getValue(evaluationContext, String.class);
        }
      }
    }
    Expression expression = parser.parseExpression(message);
    return expression.getValue(String.class);
  }

  @SneakyThrows
  private List<String> getOperationDescriptions(Object[] args, OperationLog optLog,
      String message) {
    if (isCmdRequest() && ArrayUtils.isEmpty(args)) {
      throw new IllegalArgumentException("No batched operation DTO parameters found");
    }
    Collection<?> dto = null;
    // Find the first Collection type as DTO
    for (Object arg : args) {
      if (arg instanceof Collection) {
        dto = (Collection<?>) arg;
        break;
      }
    }
    if (Objects.isNull(dto)) {
      throw new IllegalArgumentException("No batched operation parameter is of collection type");
    }

    StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
    evaluationContext.setVariable(UserKey.USER_NAME, PrincipalContext.getUserName());
    List<String> descriptions = new ArrayList<>(dto.size());

    // Only load operation user
    if (EvaluationObject.NONE.equals(optLog.evaluationObject())) {
      Expression expression = parser.parseExpression(message);
      String optMessage = expression.getValue(String.class);
      for (int i = 0; i < dto.size(); i++) {
        descriptions.add(optMessage);
      }
      return descriptions;
    }

    // Load operation resource name
    // Resource name is required
    if (isPostRequest() || isPutRequest() || (isPatchRequest()
        && EvaluationObject.DTO.equals(optLog.evaluationObject()))) {
      for (Object object : dto) {
        evaluationContext.setRootObject(object);
        Expression expression = parser.parseExpression(message);
        descriptions.add(expression.getValue(evaluationContext, String.class));
      }
      return descriptions;
    } else if (isPatchRequest()) {
      Object repository = SpringContextHolder.getBean(optLog.repositoryBeanName());
      List<Object> ids = new ArrayList<>();
      Field idField = null;
      for (Object object : dto) {
        if (Objects.isNull(idField)) {
          idField = getIdField(object);
        }
        if (Objects.isNull(idField)) {
          throw new IllegalArgumentException("Resource id field not found");
        }
        idField.setAccessible(true);
        ids.add(idField.get(object));
        //ids.add(getIdValue(object));
      }
      if (repository instanceof BaseRepository) {
        if (getBatchMessage(message, ids, evaluationContext, descriptions,
            (BaseRepository) repository)) {
          return descriptions;
        }
      }
    } else if (isDeleteRequest()) {
      Object repository = SpringContextHolder.getBean(optLog.repositoryBeanName());
      if (repository instanceof BaseRepository) {
        if (getBatchMessage(message, dto, evaluationContext, descriptions,
            (BaseRepository) repository)) {
          return descriptions;
        }
      }
    }
    return null;
  }

  private boolean getBatchMessage(String message, Collection<?> dto,
      StandardEvaluationContext evaluationContext, List<String> descriptions,
      BaseRepository repository) {
    List<String> names = repository.findNameByIdIn(dto);
    if (!CollectionUtils.isEmpty(names)) {
      for (String name : names) {
        evaluationContext.setVariable(DELETE_UPDATE_RESOURCE_NAME, name);
        //evaluationContext.setRootObject(name);
        Expression expression = parser.parseExpression(message);
        descriptions.add(expression.getValue(evaluationContext, String.class));
      }
      return true;
    }
    return false;
  }

  @SneakyThrows
  private Field getIdField(Object object) {
    Field[] fields = object.getClass().getDeclaredFields();
    if (!ArrayUtils.isEmpty(fields)) {
      for (Field field : fields) {
        if ("id".equals(field.getName())) {
          return field;
        }
      }
    }
    return null;
  }

  private UserOperation getBasicOperation(OperationLog optLog, Principal principal) {
    return UserOperation.newBuilder()
        .tenantId(principal.getTenantId())
        .tenantName(principal.getTenantName())
        .code(optLog.code())
        .resourceName(optLog.resource())
        .clientId(principal.getClientId())
        .requestId(principal.getRequestId())
        .userId(principal.getUserId())
        .fullname(principal.getFullname())
        .operationDate(principal.getRequestAcceptTime())
        .build();
  }
}
