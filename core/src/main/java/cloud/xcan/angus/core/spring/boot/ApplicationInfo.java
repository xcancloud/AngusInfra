package cloud.xcan.angus.core.spring.boot;

import static cloud.xcan.angus.core.spring.boot.ApplicationBanner.DECORATION_CHARD;
import static cloud.xcan.angus.spec.SpecConstant.DEFAULT_TIME_ZONE;

import cloud.xcan.angus.api.enums.EditionType;
import cloud.xcan.angus.api.pojo.instance.InstanceType;
import java.util.Objects;
import java.util.TimeZone;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Slf4j
@Getter
@Setter
@ConfigurationProperties(prefix = "info.app", ignoreUnknownFields = false)
public class ApplicationInfo {

  public static boolean APP_READY = false;

  /**
   * Cloud service edition artifact name or privation edition app name.
   */
  private String name = "";
  /**
   * Cloud service edition artifact id or privation edition app code.
   */
  private String artifactId = "";
  private String instanceId = "";
  private String editionType = "";
  private String version = "";
  private String description = "";
  private String profile = "";
  private InstanceType runtime;
  private String timezone;
  private String organization;
  private String organizationUrl;
  private String license;
  private String licenseUrl;

  public boolean isCloudServiceEdition() {
    return isEdition(EditionType.CLOUD_SERVICE);
  }

  public boolean isPrivateEdition() {
    return isDatacenterEdition() || isEnterpriseEdition() || isCommunityEdition();
  }

  public boolean isDatacenterEdition() {
    return isEdition(EditionType.DATACENTER);
  }

  public boolean isEnterpriseEdition() {
    return isEdition(EditionType.ENTERPRISE);
  }

  public boolean isCommunityEdition() {
    return isEdition(EditionType.COMMUNITY);
  }

  public boolean isEdition(EditionType editionType) {
    return editionType.getValue().equalsIgnoreCase(this.editionType);
  }

  public void printAppInfo() {
    try {
      StringBuilder sb = new StringBuilder();
      sb.append(DECORATION_CHARD).append("\n\n");
      sb.append("\t\tApplication        name : ").append(this.getName()).append("\n");
      sb.append("\t\tApplication  artifactId : ").append(this.getArtifactId()).append("\n");
      sb.append("\t\tApplication  instanceId : ").append(this.getInstanceId()).append("\n");
      sb.append("\t\tApplication versionType : ").append(this.getEditionType()).append("\n");
      sb.append("\t\tApplication     version : ").append(this.getVersion()).append("\n");
      sb.append("\t\tApplication description : ").append(this.getDescription()).append("\n");
      sb.append("\t\tApplication profile env : ").append(this.getProfile()).append("\n");
      sb.append("\t\tApplication runtime env : ").append(this.getRuntime()).append("\n");
      if (Objects.nonNull(this.getTimezone())) {
        TimeZone.setDefault(TimeZone.getTimeZone(this.getTimezone()));
      } else {
        TimeZone.setDefault(DEFAULT_TIME_ZONE);
      }
      sb.append("\t\tApplication    timezone : ").append(TimeZone.getDefault().getID())
          .append("\n");

      if (Objects.nonNull(this.getOrganization())) {
        sb.append("\t\tApplication    provider : ").append(this.getOrganization())
            .append("  @_@ ").append(this.getOrganizationUrl()).append("\n");
      }
      if (Objects.nonNull(this.getLicense())) {
        sb.append("\t\tApplication     license : ").append(this.getLicense())
            .append("  @_@ ").append(this.getLicenseUrl()).append("\n");
      }
      log.info(sb.toString());
    } catch (Exception e) {
      log.error("Print application ready information exception: {}", e.getMessage());
    }
  }
}
