package cloud.xcan.angus.api.pojo.manifest;

import cloud.xcan.angus.api.enums.EditionType;
import cloud.xcan.angus.spec.version.model.ApplyVersion;
import java.io.Serializable;
import java.util.LinkedHashSet;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Restriction implements Serializable {

  private LinkedHashSet<EditionType> productionEditionTypes;

  /**
   * Applicable application and service codes
   */
  private LinkedHashSet<String> productionCodes;

  private LinkedHashSet<ApplyVersion> productionVersions;

  private LinkedHashSet<ApplyVersion> productionDownwardVersions;

  private Restriction(Builder builder) {
    setProductionEditionTypes(builder.productionEditionTypes);
    setProductionCodes(builder.productionCodes);
    setProductionVersions(builder.productionVersions);
    setProductionDownwardVersions(builder.productionDownwardVersions);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static Builder newBuilder(Restriction copy) {
    Builder builder = new Builder();
    builder.productionEditionTypes = copy.getProductionEditionTypes();
    builder.productionCodes = copy.getProductionCodes();
    builder.productionVersions = copy.getProductionVersions();
    builder.productionDownwardVersions = copy.getProductionDownwardVersions();
    return builder;
  }


  public static final class Builder {

    private LinkedHashSet<EditionType> productionEditionTypes;
    private LinkedHashSet<String> productionCodes;
    private LinkedHashSet<ApplyVersion> productionVersions;
    private LinkedHashSet<ApplyVersion> productionDownwardVersions;

    private Builder() {
    }

    public Builder productionEditionTypes(LinkedHashSet<EditionType> productionEditionTypes) {
      this.productionEditionTypes = productionEditionTypes;
      return this;
    }

    public Builder productionCodes(LinkedHashSet<String> productionCodes) {
      this.productionCodes = productionCodes;
      return this;
    }

    public Builder productionVersions(LinkedHashSet<ApplyVersion> productionVersions) {
      this.productionVersions = productionVersions;
      return this;
    }

    public Builder productionDownwardVersions(
        LinkedHashSet<ApplyVersion> productionDownwardVersions) {
      this.productionDownwardVersions = productionDownwardVersions;
      return this;
    }

    public Restriction build() {
      return new Restriction(this);
    }
  }
}
