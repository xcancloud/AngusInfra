package cloud.xcan.angus.remote.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SimpleStatisticsDto {

  @Schema(description = "统计开始日期，可选，格式: yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss", example = "2024-11-01")
  private String startDate;

  @Schema(description = "统计结束日期，可选，格式: yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss", example = "2024-11-30")
  private String endDate;

}
