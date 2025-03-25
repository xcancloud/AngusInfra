package cloud.xcan.angus.api.pojo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class CorsData extends Cors {

  @Schema(description = "Enable cross domain access configuration restrictions, default false", example = "true")
  private Boolean enabledFlag = false;

}
