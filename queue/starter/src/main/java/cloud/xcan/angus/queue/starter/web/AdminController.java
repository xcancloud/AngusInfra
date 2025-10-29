package cloud.xcan.angus.queue.starter.web;

import cloud.xcan.angus.queue.starter.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Queue Admin", description = "Administrative APIs for queue maintenance and DLQ management")
@RestController
@RequestMapping(path = "/api/v1/queue/admin", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminController {

  private final AdminService adminService;

  public AdminController(AdminService adminService) {
    this.adminService = adminService;
  }

  @Operation(operationId = "queueStats", summary = "Get topic stats", description = "Return status counts, DLQ count and ready-per-partition for a topic.")
  @GetMapping("/stats")
  public RestfulApiResult<Map<String, Object>> stats(@RequestParam @NotBlank String topic) {
    return RestfulApiResult.success(adminService.topicStats(topic));
  }

  @Operation(operationId = "reclaimExpiredLeases", summary = "Reclaim expired leases", description = "Set status back to READY for messages whose lease expired.")
  @PostMapping("/reclaim")
  public RestfulApiResult<Map<String, Object>> reclaim(
      @RequestParam(defaultValue = "500") int limit) {
    int n = adminService.reclaimExpired(limit);
    return RestfulApiResult.success(Map.of("reclaimed", n));
  }

  @Operation(operationId = "purgeDone", summary = "Purge DONE messages before datetime", description = "Delete DONE messages updated before the given timestamp.")
  @DeleteMapping("/purge/done")
  public RestfulApiResult<Map<String, Object>> purgeDone(@RequestParam @NotBlank String topic,
      @RequestParam("before") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant before) {
    int n = adminService.purgeDone(topic, before);
    return RestfulApiResult.success(Map.of("purged", n));
  }

  @Operation(operationId = "purgeDLQ", summary = "Purge DLQ messages by topic", description = "Hard-delete or soft-delete DLQ based on configuration.")
  @DeleteMapping("/purge/dlq")
  public RestfulApiResult<Map<String, Object>> purgeDlq(@RequestParam @NotBlank String topic) {
    int n = adminService.purgeDeadLetters(topic);
    return RestfulApiResult.success(Map.of("purged", n));
  }

  @Operation(operationId = "replayDLQ", summary = "Replay DLQ messages", description = "Replay at most 'limit' DLQ messages by topic back to READY queue.")
  @PostMapping("/dlq/replay")
  public RestfulApiResult<Map<String, Object>> replayDlq(@RequestParam @NotBlank String topic,
      @RequestParam(defaultValue = "100") int limit) {
    int n = adminService.replayFromDeadLetter(topic, limit);
    return RestfulApiResult.success(Map.of("replayed", n));
  }
}
