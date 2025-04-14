package cloud.xcan.angus.remote.to;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_CODE_LENGTH_X2;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@Accessors(chain = true)
public class LicenseServerTo implements Serializable {

  @Length(max = MAX_CODE_LENGTH_X2)
  @Schema(description = "Allowable the IP address", example = "192.168.1.12,192.168.1.13")
  private String ipAddress;

  //@NotBlank
  @Length(max = MAX_CODE_LENGTH_X2)
  @Schema(description = "Allowable the mac address", example = "ac:de:48:00:11:22,34:29:8f:71:4e:b8")
  private String macAddress;

  @Length(max = MAX_CODE_LENGTH_X2)
  @Schema(description = "Allowable the CPU serial number", example = "bfebfbff000906ed")
  private String cpuSerialNumber;

  @Length(max = MAX_CODE_LENGTH_X2)
  @Schema(description = "Allowed the motherboard serial number", example = "C02FT0E7MD6W")
  private String mainBoardSerial;

  //@NotBlank
  @Schema(description = "Allowed the machine code", example = "72-B1-32-6A")
  @Length(max = MAX_CODE_LENGTH_X2)
  private String machineCode;

}
