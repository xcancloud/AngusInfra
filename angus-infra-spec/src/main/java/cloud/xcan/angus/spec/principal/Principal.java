package cloud.xcan.angus.spec.principal;

import cloud.xcan.angus.api.enums.ApiType;
import cloud.xcan.angus.api.enums.DataScope;
import cloud.xcan.angus.api.enums.GrantType;
import cloud.xcan.angus.api.enums.Platform;
import cloud.xcan.angus.api.enums.ResourceAclType;
import cloud.xcan.angus.api.pojo.DeviceInfo;
import cloud.xcan.angus.api.pojo.LocationInfo;
import cloud.xcan.angus.spec.locale.SupportedLanguage;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Current User or System Principal.
 * <p>
 * Note that the same user and system access from different ends, the permissions are isolated and
 * different.
 */
@Setter
@Getter
@Accessors(chain = true)
public class Principal implements Serializable {

  public static final Long DEFAULT_TENANT_ID = -1L;
  public static final Long DEFAULT_USER_ID = -1L;
  public static final String DEFAULT_CLIENT_ID = "";

  private boolean authenticated = false;

  /**
   * Tenant level setting
   */
  private SupportedLanguage defaultLanguage;

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

  private GrantType grantType;

  private Platform platform;

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

  /**
   * Operation tenant
   */
  private Long optTenantId;

  private Long userId = DEFAULT_USER_ID;
  private String fullName = "";
  private String username = "";

  private boolean sysAdmin;

  private LocationInfo locationInfo = new LocationInfo();

  private DeviceInfo deviceInfo = new DeviceInfo();

  @JsonIgnore
  private List<String> permissions;

  private boolean isUserToken = false;

  /**
   * Notice multi-tenant control switch
   */
  private boolean multiTenantCtrl = true;

  private Map<String, Object> extensions = new HashMap<>();

}
