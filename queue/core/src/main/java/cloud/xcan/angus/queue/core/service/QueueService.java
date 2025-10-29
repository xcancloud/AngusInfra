package cloud.xcan.angus.queue.core.service;

import cloud.xcan.angus.queue.core.model.LeaseMessages;
import cloud.xcan.angus.queue.core.model.MessageData;
import cloud.xcan.angus.queue.core.model.SendMessage;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface QueueService {

  Long send(String topic, String partitionKey, String payload, String headers, int priority,
      Instant visibleAt, String idempotencyKey, int maxAttempts, int numPartitions);

  default Long send(SendMessage req) {
    return send(
        req.getTopic(),
        req.getPartitionKey(),
        req.getPayload(),
        req.getHeaders(),
        req.getPriority() == null ? 0 : req.getPriority(),
        req.getVisibleAt(),
        req.getIdempotencyKey(),
        req.getMaxAttempts() == null ? 16 : req.getMaxAttempts(),
        req.getNumPartitions() == null ? 1 : req.getNumPartitions()
    );
  }

  int lease(String topic, Collection<Integer> partitions, String owner, int leaseSec, int limit);

  default int lease(LeaseMessages req) {
    return lease(
        req.getTopic(),
        req.getPartitions(),
        req.getOwner(),
        req.getLeaseSeconds() == null ? 30 : req.getLeaseSeconds(),
        req.getLimit() == null ? 100 : req.getLimit()
    );
  }

  List<MessageData> listLeasedByOwner(String owner, int limit);

  int ack(Collection<Long> ids);

  int nack(Collection<Long> ids, int backoffSec);

  int moveExceededAttemptsToDeadLetter(int limit);
}
