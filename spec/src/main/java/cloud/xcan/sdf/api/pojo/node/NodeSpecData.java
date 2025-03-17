package cloud.xcan.sdf.api.pojo.node;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.nonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class NodeSpecData {

  private Long nodeId;

  private Long nodeNum;

  @Schema(example = "8")
  private Integer cpu;

  @Schema(example = "16")
  private Integer memory; // G

  @Schema(example = "cloud_essd")
  private String sysDiskCategory;

  @Schema(example = "2048")
  private Integer sysDisk; // GB

  @Schema(example = "PayByTraffic")
  private String networkChargeType;

  @Schema(example = "1")
  private Integer network; // MB

  public String getShowLabel() {
    String label = nullSafe(cpu, 0) + "C/" + nullSafe(memory, 0) + "GB/"
        + nullSafe(sysDisk, 0) + "GB";
    if (nonNull(network)){
      label += "/" + network + "Mbps";
    }
    return label;
  }

  @JsonIgnore
  public boolean is4C8G() {
    return Integer.valueOf(4).equals(cpu) && Integer.valueOf(8).equals(memory);
  }

  @JsonIgnore
  public boolean is8C16G() {
    return Integer.valueOf(8).equals(cpu) && Integer.valueOf(16).equals(memory);
  }

  @JsonIgnore
  public boolean is16C32G() {
    return Integer.valueOf(16).equals(cpu) && Integer.valueOf(32).equals(memory);
  }

  @JsonIgnore
  public boolean is32C64G() {
    return Integer.valueOf(32).equals(cpu) && Integer.valueOf(64).equals(memory);
  }

  @JsonIgnore
  public boolean is64C128G() {
    return Integer.valueOf(64).equals(cpu) && Integer.valueOf(128).equals(memory);
  }

}
