package cloud.xcan.angus.queue.core.service;

import cloud.xcan.angus.queue.core.model.MessageData;
import cloud.xcan.angus.queue.core.spi.RepositoryAdapter;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

public class DefaultQueueService implements cloud.xcan.angus.queue.core.service.QueueService {

  private final RepositoryAdapter adapter;

  public DefaultQueueService(RepositoryAdapter adapter) {
    this.adapter = adapter;
  }

  @Override
  public Long send(String topic, String partitionKey, String payload, String headers, int priority,
      Instant visibleAt, String idempotencyKey, int maxAttempts, int numPartitions) {
    return adapter.saveMessage(topic, partitionKey, payload, headers, priority, visibleAt,
        idempotencyKey, maxAttempts, numPartitions);
  }

  @Override
  public int lease(String topic, Collection<Integer> partitions, String owner, int leaseSec,
      int limit) {
    return adapter.leaseBatch(topic, partitions, owner, leaseSec, limit);
  }

  @Override
  public List<MessageData> listLeasedByOwner(String owner, int limit) {
    return adapter.findLeasedByOwner(owner, Instant.now(), limit);
  }

  @Override
  public int ack(Collection<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return 0;
    }
    return adapter.ackBatch(ids);
  }

  @Override
  public int nack(Collection<Long> ids, int backoffSec) {
    if (ids == null || ids.isEmpty()) {
      return 0;
    }
    return adapter.nackBatch(ids, backoffSec);
  }

  @Override
  public int moveExceededAttemptsToDeadLetter(int limit) {
    return adapter.moveExceededToDeadLetter(limit);
  }
}
