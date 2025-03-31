package cloud.xcan.angus.api.pojo.manifest;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.api.enums.EditionType;
import cloud.xcan.angus.api.enums.GoodsType;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Production implements Serializable {

  private Long id;

  private String code;

  private String name;

  private GoodsType type;

  private String version;

  private EditionType editionType;

  private String base64Icon;

  private LinkedHashSet<String> tags;

  private String introduction;

  private String information;

  private LinkedHashSet<String> features;

  private Boolean chargeFlag;

  private String orderNo;

  private Map<String, String> extras = Map.of(
      "license", "XCan Business License, Version 1.0"
      , "licenseUrl", "http://www.xcan.cloud/licenses/XCBL-1.0"
      , "organization", "XCan Company"
      , "organizationUrl", "http://www.xcan.cloud");

  private Production(Builder builder) {
    setId(builder.id);
    setCode(builder.code);
    setName(builder.name);
    setType(builder.type);
    setVersion(builder.version);
    setEditionType(builder.editionType);
    setBase64Icon(builder.base64Icon);
    setTags(builder.tags);
    setIntroduction(builder.introduction);
    setInformation(builder.information);
    setFeatures(builder.features);
    setChargeFlag(builder.chargeFlag);
    setOrderNo(builder.orderNo);
    if (isNotEmpty(builder.extras)) { // Fix:: Null is not allowed
      this.extras.putAll(builder.extras);
    }
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static Builder newBuilder(Production copy) {
    Builder builder = new Builder();
    builder.id = copy.getId();
    builder.code = copy.getCode();
    builder.name = copy.getName();
    builder.type = copy.getType();
    builder.version = copy.getVersion();
    builder.editionType = copy.getEditionType();
    builder.base64Icon = copy.getBase64Icon();
    builder.tags = copy.getTags();
    builder.introduction = copy.getIntroduction();
    builder.information = copy.getInformation();
    builder.features = copy.getFeatures();
    builder.chargeFlag = copy.getChargeFlag();
    builder.orderNo = copy.getOrderNo();
    builder.extras = copy.getExtras();
    return builder;
  }


  public static final class Builder {

    private Long id;
    private String code;
    private String name;
    private GoodsType type;
    private String version;
    private EditionType editionType;
    private String base64Icon;
    private LinkedHashSet<String> tags;
    private String introduction;
    private String information;
    private LinkedHashSet<String> features;
    private Boolean chargeFlag;
    private String orderNo;
    private Map<String, String> extras;

    private Builder() {
    }

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder code(String code) {
      this.code = code;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder type(GoodsType type) {
      this.type = type;
      return this;
    }

    public Builder version(String version) {
      this.version = version;
      return this;
    }

    public Builder editionType(EditionType editionType) {
      this.editionType = editionType;
      return this;
    }

    public Builder base64Icon(String base64Icon) {
      this.base64Icon = base64Icon;
      return this;
    }

    public Builder tags(LinkedHashSet<String> tags) {
      this.tags = tags;
      return this;
    }

    public Builder introduction(String introduction) {
      this.introduction = introduction;
      return this;
    }

    public Builder information(String information) {
      this.information = information;
      return this;
    }

    public Builder features(LinkedHashSet<String> features) {
      this.features = features;
      return this;
    }

    public Builder chargeFlag(Boolean chargeFlag) {
      this.chargeFlag = chargeFlag;
      return this;
    }

    public Builder orderNo(String orderNo) {
      this.orderNo = orderNo;
      return this;
    }

    public Builder extras(Map<String, String> extras) {
      this.extras = extras;
      return this;
    }

    public Production build() {
      return new Production(this);
    }
  }
}
