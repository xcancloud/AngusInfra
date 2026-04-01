package cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.network;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.domain.Page;

@Getter
@Setter
@Accessors(chain = true)
public class NetDeviceUsage {

  private String deviceName;

  private Page<NetUsage> deviceUsage;

}
