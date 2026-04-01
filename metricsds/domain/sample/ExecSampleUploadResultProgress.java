package cloud.xcan.angus.core.tester.infra.metricsds.domain.sample;

import java.text.DecimalFormat;

public interface ExecSampleUploadResultProgress {

  boolean getFinish();

  long getNodeId();

  long getUploadResultBytes();

  long getUploadResultTotalBytes();

  default String getUploadResultProgress() {
    DecimalFormat format = new DecimalFormat("0.00");
    return getUploadResultTotalBytes() > 0 ? format.format(
        (getUploadResultBytes() / (double) getUploadResultTotalBytes()) * 100) : null;
  }

}
