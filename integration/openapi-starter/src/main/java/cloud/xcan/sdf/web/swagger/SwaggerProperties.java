
package cloud.xcan.sdf.web.swagger;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "xcan.swagger", ignoreUnknownFields = false)
@SuppressWarnings("squid:S1068")
public class SwaggerProperties {

  private Contact contact = new Contact();
  private Boolean enabled = false;
  private String version = "";
  private String description = "";
  private String license = "";
  private String licenseUrl = "";

  @Getter
  @Setter
  public static class Contact {

    private String name;
    private String url;
    private String email;

  }
}
