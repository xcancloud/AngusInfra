package cloud.xcan.angus.core.event.source;

import cloud.xcan.angus.api.enums.EventType;
import cloud.xcan.angus.api.enums.NoticeType;
import cloud.xcan.angus.api.enums.ReceiveObjectType;
import cloud.xcan.angus.api.pojo.Attachment;
import cloud.xcan.angus.remote.ExceptionLevel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@ToString
public class EventContent extends SimpleSource {

  private String subject;

  /**
   * Request and Service
   */
  private String clientId;
  private String appCode;
  private String serviceCode;
  private String serviceName;
  private String instanceId;
  private String requestId;
  private String method;
  private String uri;

  /**
   * Principal
   */
  private Long tenantId;
  private String tenantName;
  private Long userId;
  private String fullName;

  /**
   * Exception
   */
  private String eKey;
  /**
   * @see ExceptionLevel
   */
  private ExceptionLevel level;
  private String cause;

  /**
   * Notice and business
   */
  private String targetType;
  private String targetId;
  private String targetName;

  /**
   * @see NoticeType
   */
  private List<NoticeType> noticeTypes;

  /**
   * ReceiveObjectType is required when sending via receiveObjectIds
   *
   * @see ReceiveObjectType
   */
  private ReceiveObjectType receiveObjectType;
  /**
   * Parameter toAddress and receiveObjectIds are required to choose one, if both are passed,
   * toAddress is used by default
   */
  private List<Long> receiveObjectIds;
  /**
   * Top policy code
   */
  private List<String> topPolicyCode;
  /**
   * Key is email address and value is template parameter, when all email template parameters are
   * the same, only one value is set.
   */
  private Map<String, String> templateParams;
  /**
   * Notice attachment
   */
  private Set<Attachment> attachments;

  public EventContent() {
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  @JsonIgnore
  public boolean isNoticeType() {
    return Objects.nonNull(getType()) && EventType.NOTICE.equals(getType());
  }

  private EventContent(Builder builder) {
    setSubject(builder.subject);
    setType(builder.type);
    setCode(builder.code);
    setDescription(builder.description);
    setTimestamp(builder.timestamp);
    setExt(builder.ext);
    setClientId(builder.clientId);
    setAppCode(builder.appCode);
    setServiceCode(builder.serviceCode);
    setServiceName(builder.serviceName);
    setInstanceId(builder.instanceId);
    setRequestId(builder.requestId);
    setMethod(builder.method);
    setUri(builder.uri);
    setTenantId(builder.tenantId);
    setTenantName(builder.tenantName);
    setUserId(builder.userId);
    setFullName(builder.fullName);
    eKey = builder.eKey;
    setLevel(builder.level);
    setCause(builder.cause);
    setTargetType(builder.targetType);
    setTargetId(builder.targetId);
    setTargetName(builder.targetName);
    setNoticeTypes(builder.noticeTypes);
    setReceiveObjectType(builder.receiveObjectType);
    setReceiveObjectIds(builder.receiveObjectIds);
    setTopPolicyCode(builder.topPolicyCode);
    setTemplateParams(builder.templateParams);
    setAttachments(builder.attachments);
  }

  public static final class Builder {

    private String subject;
    private EventType type;
    private String code;
    private String description;
    private LocalDateTime timestamp;
    private Map<String, Object> ext;
    private String clientId;
    private String appCode;
    private String serviceCode;
    private String serviceName;
    private String instanceId;
    private String requestId;
    private String method;
    private String uri;
    private Long tenantId;
    private String tenantName;
    private Long userId;
    private String fullName;
    private String eKey;
    private ExceptionLevel level;
    private String cause;
    private String targetType;
    private String targetId;
    private String targetName;
    private List<NoticeType> noticeTypes;
    private ReceiveObjectType receiveObjectType;
    private List<Long> receiveObjectIds;
    private List<String> topPolicyCode;
    private Map<String, String> templateParams;
    private Set<Attachment> attachments;

    private Builder() {
    }

    public Builder subject(String val) {
      subject = val;
      return this;
    }

    public Builder type(EventType val) {
      type = val;
      return this;
    }

    public Builder code(String val) {
      code = val;
      return this;
    }

    public Builder description(String val) {
      description = val;
      return this;
    }

    public Builder timestamp(LocalDateTime val) {
      timestamp = val;
      return this;
    }

    public Builder ext(Map<String, Object> val) {
      ext = val;
      return this;
    }

    public Builder clientId(String val) {
      clientId = val;
      return this;
    }

    public Builder appCode(String val) {
      appCode = val;
      return this;
    }

    public Builder serviceCode(String val) {
      serviceCode = val;
      return this;
    }

    public Builder serviceName(String val) {
      serviceName = val;
      return this;
    }

    public Builder instanceId(String val) {
      instanceId = val;
      return this;
    }

    public Builder requestId(String val) {
      requestId = val;
      return this;
    }

    public Builder method(String val) {
      method = val;
      return this;
    }

    public Builder uri(String val) {
      uri = val;
      return this;
    }

    public Builder tenantId(Long val) {
      tenantId = val;
      return this;
    }

    public Builder tenantName(String val) {
      tenantName = val;
      return this;
    }

    public Builder userId(Long val) {
      userId = val;
      return this;
    }

    public Builder fullName(String val) {
      fullName = val;
      return this;
    }

    public Builder eKey(String val) {
      eKey = val;
      return this;
    }

    public Builder level(ExceptionLevel val) {
      level = val;
      return this;
    }

    public Builder cause(String val) {
      cause = val;
      return this;
    }

    public Builder targetType(String val) {
      targetType = val;
      return this;
    }

    public Builder targetId(String val) {
      targetId = val;
      return this;
    }

    public Builder targetName(String val) {
      targetName = val;
      return this;
    }

    public Builder noticeTypes(List<NoticeType> val) {
      noticeTypes = val;
      return this;
    }

    public Builder receiveObjectType(ReceiveObjectType val) {
      receiveObjectType = val;
      return this;
    }

    public Builder receiveObjectIds(List<Long> val) {
      receiveObjectIds = val;
      return this;
    }

    public Builder topPolicyCode(List<String> val) {
      topPolicyCode = val;
      return this;
    }

    public Builder templateParams(Map<String, String> val) {
      templateParams = val;
      return this;
    }

    public Builder attachments(Set<Attachment> val) {
      attachments = val;
      return this;
    }

    public EventContent build() {
      return new EventContent(this);
    }
  }
}
