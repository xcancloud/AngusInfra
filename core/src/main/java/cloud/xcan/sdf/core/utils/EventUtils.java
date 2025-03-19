package cloud.xcan.sdf.core.utils;

import cloud.xcan.sdf.api.ExceptionLevel;
import cloud.xcan.sdf.api.enums.EventType;
import cloud.xcan.sdf.api.enums.NoticeType;
import cloud.xcan.sdf.api.enums.ReceiveObjectType;
import cloud.xcan.sdf.core.event.source.EventContent;
import cloud.xcan.sdf.spec.principal.Principal;
import cloud.xcan.sdf.spec.principal.PrincipalContext;
import java.util.List;
import java.util.Map;

public class EventUtils {

  public static EventContent assembleExceptionEvent(EventType type, String code, String message,
      ExceptionLevel level, String eKey, Object cause) {
    Principal principal = PrincipalContext.get();
    return EventContent.newBuilder()
        .type(type).code(code).description(message)
        .clientId(principal.getClientId())
        .serviceCode(principal.getServiceCode())
        .serviceName(principal.getServiceName())
        .instanceId(principal.getInstanceId())
        .requestId(principal.getRequestId())
        .method(principal.getMethod()).uri(principal.getUri())
        .tenantId(principal.getTenantId())
        .tenantName(principal.getTenantName())
        .userId(principal.getUserId())
        .fullname(principal.getFullname())
        .eKey(eKey).level(level).cause(cause.toString())
        .build();
  }

  public static EventContent assembleNoticeEvent(String appCode, EventType type, String code,
      String message, String targetType, String targetId, String targetName,
      List<NoticeType> noticeTypes, ReceiveObjectType receiveObjectType,
      List<Long> receiveObjectIds) {
    Principal principal = PrincipalContext.get();
    return EventContent.newBuilder()
        .type(type).code(code).description(message)
        .clientId(principal.getClientId()).appCode(appCode)
        .serviceCode(principal.getServiceCode())
        .serviceName(principal.getServiceName())
        .instanceId(principal.getInstanceId())
        .requestId(principal.getRequestId()).method(principal.getMethod()).uri(principal.getUri())
        .tenantId(principal.getTenantId()).tenantName(principal.getTenantName())
        .userId(principal.getUserId()).fullname(principal.getFullname())
        //.eKey(eKey).level(level).cause(cause.toString())
        .targetId(targetId).targetName(targetName).targetType(targetType)
        .noticeTypes(noticeTypes).receiveObjectType(receiveObjectType)
        .receiveObjectIds(receiveObjectIds)
        .build();
  }

  public static EventContent assembleNoticeEvent(String appCode, EventType type, String code,
      String message, String targetType, String targetId, String targetName,
      List<NoticeType> noticeTypes, ReceiveObjectType receiveObjectType,
      List<Long> receiveObjectIds, List<String> topPolicyCode, Map<String, String> templateParams) {
    Principal principal = PrincipalContext.get();
    return EventContent.newBuilder()
        .type(type).code(code).description(message)
        .clientId(principal.getClientId()).appCode(appCode)
        .serviceCode(principal.getServiceCode())
        .serviceName(principal.getServiceName())
        .instanceId(principal.getInstanceId())
        .requestId(principal.getRequestId()).method(principal.getMethod()).uri(principal.getUri())
        .tenantId(principal.getTenantId()).tenantName(principal.getTenantName())
        .userId(principal.getUserId()).fullname(principal.getFullname())
        //.eKey(eKey).level(level).cause(cause.toString())
        .targetId(targetId).targetName(targetName).targetType(targetType)
        .noticeTypes(noticeTypes).receiveObjectType(receiveObjectType)
        .receiveObjectIds(receiveObjectIds).topPolicyCode(topPolicyCode)
        .templateParams(templateParams)
        .build();
  }

  public static EventContent assembleNoticeEventByDoor(String appCode, EventType type,
      Principal principal, String code, String message,String targetType, String targetId,
      String targetName, List<NoticeType> noticeTypes, ReceiveObjectType receiveObjectType,
      List<Long> receiveObjectIds, List<String> topPolicyCode, Map<String, String> templateParams) {
    return EventContent.newBuilder()
        .type(type).code(code).description(message)
        .clientId(principal.getClientId()).appCode(appCode)
        .serviceCode(principal.getServiceCode())
        .serviceName(principal.getServiceName())
        .instanceId(principal.getInstanceId())
        .requestId(principal.getRequestId()).method(principal.getMethod()).uri(principal.getUri())
        .tenantId(principal.getTenantId()).tenantName(principal.getTenantName())
        .userId(principal.getUserId()).fullname(principal.getFullname())
        //.eKey(eKey).level(level).cause(cause.toString())
        .targetId(targetId).targetName(targetName).targetType(targetType)
        .noticeTypes(noticeTypes).receiveObjectType(receiveObjectType)
        .receiveObjectIds(receiveObjectIds).topPolicyCode(topPolicyCode)
        .templateParams(templateParams)
        .build();
  }
}
