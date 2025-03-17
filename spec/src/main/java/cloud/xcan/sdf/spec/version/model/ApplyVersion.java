package cloud.xcan.sdf.spec.version.model;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import cloud.xcan.sdf.spec.ValueObject;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * @author XiaoLong Liu
 */
public class ApplyVersion implements ValueObject<ApplyVersion> {

  private String beginVersion;

  private String endVersion;

  public String getBeginVersion() {
    return beginVersion;
  }

  public ApplyVersion setBeginVersion(String beginVersion) {
    this.beginVersion = beginVersion;
    return this;
  }

  public String getEndVersion() {
    return endVersion;
  }

  public ApplyVersion setEndVersion(String endVersion) {
    this.endVersion = endVersion;
    return this;
  }

  public static String getConstraint(ApplyVersion applyVersion) {
    String beginVersion = applyVersion.getBeginVersion();
    String endVersion = applyVersion.getEndVersion();
    if (isNotBlank(beginVersion) && isNotBlank(endVersion)) {
      return ">=" + beginVersion + "&<=" + endVersion;
    }
    if (StringUtils.isNotBlank(beginVersion)) {
      return ">=" + beginVersion;
    }
    if (StringUtils.isNotBlank(endVersion)) {
      return "<=" + endVersion;
    }
    return "";
  }

  @Override
  public boolean sameValueAs(ApplyVersion versionData) {
    return equals(versionData);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplyVersion that = (ApplyVersion) o;
    return Objects.equals(beginVersion, that.beginVersion) &&
        Objects.equals(endVersion, that.endVersion);
  }

  @Override
  public int hashCode() {
    return Objects.hash(beginVersion, endVersion);
  }
}
