
package cloud.xcan.sdf.spec.principal;

import cloud.xcan.sdf.api.enums.ApiType;
import cloud.xcan.sdf.api.enums.DataScope;
import cloud.xcan.sdf.api.enums.GrantType;
import cloud.xcan.sdf.api.enums.Platform;
import cloud.xcan.sdf.api.enums.ResourceAclType;
import cloud.xcan.sdf.spec.annotations.DoInFuture;
import cloud.xcan.sdf.spec.locale.SupportedLanguage;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Current User or System Principal.
 * <p>
 * Note that the same user and system access from different ends, the permissions are isolated and
 * different.
 */
@Setter
@Getter
@ToString(exclude = {"authorization", "authorities"})
@Accessors(chain = true)
public class Principal implements Serializable {

  public static final Long DEFAULT_TENANT_ID = -1L;
  public static final Long DEFAULT_USER_ID = -1L;
  public static final String DEFAULT_CLIENT_ID = "";

  /**
   * Tenant level setting
   */
  private SupportedLanguage defaultLanguage;
  /**
   * Platform level setting
   */
  @DoInFuture("Support tenant level setting")
  private String defaultTimeZone;

  private String requestId = "";
  private LocalDateTime requestAcceptTime;

  private String clientId = DEFAULT_CLIENT_ID;
  private String clientSource = "";
  /**
   * Cloud service edition artifact id or privation edition app code.
   */
  private String serviceCode;
  /**
   * Cloud service edition artifact name or privation edition app name.
   */
  private String serviceName;
  private String resourceCode;
  private String instanceId;
  private String method;
  private String uri;

  private String remoteAddress;
  private String userAgent;
  /**
   * Signup Device ID.
   */
  private String deviceId;

  private boolean authenticated = false;
  private GrantType grantType;
  private Platform platformScope;
  private ApiType apiType;
  @JsonIgnore
  private String authorization;
  private DataScope dataScope;
  private ResourceAclType resourceAclType;
  private String authServiceCode;

  /**
   * Current tenant
   */
  private Long tenantId = DEFAULT_TENANT_ID;
  private String tenantName = "";

  private Long userId = DEFAULT_USER_ID;
  private String fullname = "";
  private String username = "";

  private Long mainDeptId;

  //private String itc;
  private String country;

  private boolean sysAdmin;
  private boolean toUser;
  @JsonIgnore
  private List<String> permissions;

  /**
   * Operation tenant
   */
  private Long optTenantId;

  /**
   * Notice multi-tenant control switch
   */
  private boolean multiTenantCtrl = true;
  /**
   * Configure in BizTemplate, Not from token authorization
   */
  private String[] requiredToPolicy;

  private Map<String, Object> extensions = new HashMap<>();

}
