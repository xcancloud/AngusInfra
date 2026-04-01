package cloud.xcan.angus.core.tester.infra.metricsds.domain.sample;

import java.util.Date;

public interface ExecSampleCounter {

  String getName();

  long getNodeId();

  Date getTimestamp();

  String getCounter();

}
