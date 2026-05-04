package cloud.xcan.angus.core.utils;

import cloud.xcan.angus.api.enums.NoticeType;
import cloud.xcan.angus.api.enums.ReceiveObjectType;
import cloud.xcan.angus.core.event.source.EventContent;
import cloud.xcan.angus.remote.ExceptionLevel;
import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import java.util.List;
import java.util.Map;

public final class EventUtils {

  private EventUtils() {
  }

  public static EventContent assembleExceptionEvent(String type, String code, String message,
      ExceptionLevel level, String eKey, Object cause) {
    Principal principal = PrincipalContext.get();
    String causeText = cause == null ? null : String.valueOf(cause);
    return fillPrincipal(EventContent.newBuilder(), principal)
        .type(type).code(code).description(message)
        .eKey(eKey).level(level).cause(causeText)
        .build();
  }

  public static EventContent assembleNoticeEvent(String appCode, String type, String code,
      String message, String targetType, String targetId, String targetName,
      List<NoticeType> noticeTypes, ReceiveObjectType receiveObjectType,
      List<Long> receiveObjectIds) {
    return fillPrincipal(EventContent.newBuilder(), PrincipalContext.get())
        .type(type).code(code).description(message)
        .appCode(appCode)
        .targetId(targetId).targetName(targetName).targetType(targetType)
        .noticeTypes(noticeTypes).receiveObjectType(receiveObjectType)
        .receiveObjectIds(receiveObjectIds)
        .build();
  }

  public static EventContent assembleNoticeEvent(String appCode, String type, String code,
      String message, String targetType, String targetId, String targetName,
      List<NoticeType> noticeTypes, ReceiveObjectType receiveObjectType,
      List<Long> receiveObjectIds, List<String> topPolicyCode, Map<String, String> templateParams) {
    return fillPrincipal(EventContent.newBuilder(), PrincipalContext.get())
        .type(type).code(code).description(message)
        .appCode(appCode)
        .targetId(targetId).targetName(targetName).targetType(targetType)
        .noticeTypes(noticeTypes).receiveObjectType(receiveObjectType)
        .receiveObjectIds(receiveObjectIds).topPolicyCode(topPolicyCode)
        .templateParams(templateParams)
        .build();
  }

  public static EventContent assembleNoticeEventByDoor(String appCode, String type,
      Principal principal, String code, String message, String targetType, String targetId,
      String targetName, List<NoticeType> noticeTypes, ReceiveObjectType receiveObjectType,
      List<Long> receiveObjectIds, List<String> topPolicyCode, Map<String, String> templateParams) {
    return fillPrincipal(EventContent.newBuilder(), principal)
        .type(type).code(code).description(message)
        .appCode(appCode)
        .targetId(targetId).targetName(targetName).targetType(targetType)
        .noticeTypes(noticeTypes).receiveObjectType(receiveObjectType)
        .receiveObjectIds(receiveObjectIds).topPolicyCode(topPolicyCode)
        .templateParams(templateParams)
        .build();
  }

  private static EventContent.Builder fillPrincipal(EventContent.Builder builder,
      Principal principal) {
    return builder
        .clientId(principal.getClientId())
        .serviceCode(principal.getServiceCode())
        .serviceName(principal.getServiceName())
        .instanceId(principal.getInstanceId())
        .requestId(principal.getRequestId())
        .method(principal.getMethod())
        .uri(principal.getUri())
        .tenantId(principal.getTenantId())
        .tenantName(principal.getTenantName())
        .userId(principal.getUserId())
        .fullName(principal.getFullName());
  }
}
