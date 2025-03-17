package cloud.xcan.sdf.api.pojo.manifest;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Manifest implements Serializable {

  public static final String MANIFEST_FILE_NAME = "INSTALLATION.JSON";

  private Production production;

  private Installation installation;

  /**
   * Is it protected by license
   */
  private Boolean licenseProtection;

  /**
   * The value is null when the license is not obtained
   */
  private License license;

  private LocalDateTime date;

  private Map<String, String> extras;

  private Manifest(Builder builder) {
    setProduction(builder.production);
    setInstallation(builder.installation);
    setLicenseProtection(builder.licenseProtection);
    setLicense(builder.license);
    setDate(builder.date);
    setExtras(builder.extras);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static Builder newBuilder(Manifest copy) {
    Builder builder = new Builder();
    builder.production = copy.getProduction();
    builder.installation = copy.getInstallation();
    builder.licenseProtection = copy.getLicenseProtection();
    builder.license = copy.getLicense();
    builder.date = copy.getDate();
    builder.extras = copy.getExtras();
    return builder;
  }


  public static final class Builder {

    private Production production;
    private Installation installation;
    private Boolean licenseProtection;
    private License license;
    private LocalDateTime date;
    private Map<String, String> extras;

    private Builder() {
    }

    public Builder production(Production production) {
      this.production = production;
      return this;
    }

    public Builder installation(Installation installation) {
      this.installation = installation;
      return this;
    }

    public Builder licenseProtection(Boolean licenseProtection) {
      this.licenseProtection = licenseProtection;
      return this;
    }

    public Builder license(License license) {
      this.license = license;
      return this;
    }

    public Builder date(LocalDateTime date) {
      this.date = date;
      return this;
    }

    public Builder extras(Map<String, String> extras) {
      this.extras = extras;
      return this;
    }

    public Manifest build() {
      return new Manifest(this);
    }
  }
}
