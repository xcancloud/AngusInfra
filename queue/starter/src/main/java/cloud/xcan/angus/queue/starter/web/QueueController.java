package cloud.xcan.angus.queue.starter.web;

import cloud.xcan.angus.queue.core.model.LeaseMessages;
import cloud.xcan.angus.queue.core.model.MessageData;
import cloud.xcan.angus.queue.core.model.SendMessage;
import cloud.xcan.angus.queue.core.service.QueueService;
import cloud.xcan.angus.queue.starter.autoconfigure.QueueProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Queue Management", description = "APIs to send, poll and manage queue messages")
@RestController
@RequestMapping(path = "/api/v1/queue", produces = MediaType.APPLICATION_JSON_VALUE)
public class QueueController {

  private final QueueService queueService;
  private final QueueProperties properties;

  public QueueController(QueueService queueService, QueueProperties properties) {
    this.queueService = queueService;
    this.properties = properties;
  }

  @Operation(operationId = "sendMessage", summary = "Send message", description = "Send a message to a topic with optional partition key, headers, priority and idempotency key.",
      requestBody = @RequestBody(required = true, description = "Send request payload",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = SendRequest.class))))
  @PostMapping("/send")
  public RestfulApiResult<SendResponse> send(
      @Valid @org.springframework.web.bind.annotation.RequestBody SendRequest req) {
    SendMessage request = SendMessage.builder()
        .topic(req.getTopic())
        .partitionKey(req.getPartitionKey())
        .payload(req.getPayload())
        .headers(req.getHeaders())
        .priority(Optional.ofNullable(req.getPriority()).orElse(0))
        .visibleAt(Optional.ofNullable(req.getVisibleAt()).orElse(Instant.now()))
        .idempotencyKey(req.getIdempotencyKey())
        .maxAttempts(Optional.ofNullable(req.getMaxAttempts()).orElse(16))
        .numPartitions(properties.getPartitions())
        .build();
    Long id = queueService.send(request);
    return RestfulApiResult.success(new SendResponse(id));
  }

  @Operation(operationId = "pollMessages", summary = "Poll messages with lease", description = "Lease READY messages and return the leased messages for the given owner.",
      requestBody = @RequestBody(required = true, description = "Poll request payload",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = PollRequest.class))))
  @PostMapping("/poll")
  public RestfulApiResult<List<MessageData>> poll(
      @Valid @org.springframework.web.bind.annotation.RequestBody PollRequest req) {
    List<Integer> partitions = (req.getPartitions() == null || req.getPartitions().isEmpty())
        ? java.util.stream.IntStream.range(0, properties.getPartitions()).boxed().toList()
        : req.getPartitions();
    int leased = queueService.lease(
        LeaseMessages.builder()
            .topic(req.getTopic())
            .partitions(partitions)
            .owner(req.getOwner())
            .leaseSeconds(
                Optional.ofNullable(req.getLeaseSeconds()).orElse(properties.getLeaseSeconds()))
            .limit(Optional.ofNullable(req.getLimit()).orElse(properties.getPollBatch()))
            .build()
    );
    if (leased == 0) {
      return RestfulApiResult.success(List.of());
    }
    return RestfulApiResult.success(queueService.listLeasedByOwner(req.getOwner(), leased));
  }

  @Operation(operationId = "ackMessages", summary = "Acknowledge messages", description = "Mark messages as DONE.",
      requestBody = @RequestBody(required = true, description = "Ack request",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = IdsRequest.class))))
  @PostMapping("/ack")
  public RestfulApiResult<AckResponse> ack(
      @Valid @org.springframework.web.bind.annotation.RequestBody IdsRequest req) {
    List<Long> ids = req.toIdList();
    int n = queueService.ack(ids);
    return RestfulApiResult.success(new AckResponse(n));
  }

  @Operation(operationId = "nackMessages", summary = "Negative acknowledge (nack) messages", description = "Return messages to READY with backoff seconds.",
      requestBody = @RequestBody(required = true, description = "Nack request",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = NackRequest.class))))
  @PostMapping("/nack")
  public RestfulApiResult<NackResponse> nack(
      @Valid @org.springframework.web.bind.annotation.RequestBody NackRequest req) {
    List<Long> ids = req.toIdList();
    int n = queueService.nack(ids, Optional.ofNullable(req.getBackoffSeconds()).orElse(5));
    return RestfulApiResult.success(new NackResponse(n));
  }

  // ===== DTOs =====

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SendRequest {

    @NotBlank
    @Schema(description = "Topic name")
    private String topic;
    @Schema(description = "Partition key (optional)")
    private String partitionKey;
    @NotBlank
    @Schema(description = "Payload body")
    private String payload;
    @Schema(description = "Headers JSON string (optional)")
    private String headers;
    @Schema(description = "Message priority (default 0)")
    private Integer priority;
    @Schema(description = "Visible at timestamp (ISO-8601). Default: now")
    private Instant visibleAt;
    @Schema(description = "Idempotency key (optional)")
    private String idempotencyKey;
    @Schema(description = "Max attempts (default 16)")
    private Integer maxAttempts;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SendResponse {

    private Long id;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PollRequest {

    @NotBlank
    @Schema(description = "Topic name")
    private String topic;
    @NotBlank
    @Schema(description = "Lease owner")
    private String owner;
    @Schema(description = "Partitions to poll (optional). Default: all")
    private List<Integer> partitions;
    @Schema(description = "Lease seconds (optional)")
    private Integer leaseSeconds;
    @Schema(description = "Max rows to lease (optional)")
    private Integer limit;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class IdsRequest {

    @Schema(description = "Message id list")
    @NotEmpty
    private List<Long> ids;

    public List<Long> toIdList() {
      return ids == null ? List.of() : ids;
    }
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class NackRequest {

    @Schema(description = "Message id list")
    @NotEmpty
    private List<Long> ids;
    @Schema(description = "Backoff seconds (optional). Default 5")
    private Integer backoffSeconds;

    public List<Long> toIdList() {
      return ids == null ? List.of() : ids;
    }
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class AckResponse {

    private int acked;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class NackResponse {

    private int nacked;
  }
}
