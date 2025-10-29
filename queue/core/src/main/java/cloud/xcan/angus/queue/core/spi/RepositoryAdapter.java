package cloud.xcan.angus.queue.core.spi;

import cloud.xcan.angus.queue.core.model.DeadLetterData;
import cloud.xcan.angus.queue.core.model.MessageData;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface RepositoryAdapter {

  Long saveMessage(String topic, String partitionKey, String payload, String headers, int priority,
      Instant visibleAt, String idempotencyKey, int maxAttempts, int numPartitions);

  int leaseBatch(String topic, Collection<Integer> partitions, String owner, int leaseSec,
      int limit);

  List<MessageData> findLeasedByOwner(String owner, Instant now, int limit);

  int ackBatch(Collection<Long> ids);

  int nackBatch(Collection<Long> ids, int backoffSec);

  int moveExceededToDeadLetter(int limit);

  int reclaimExpiredLeases(int limit);

  List<Object[]> countByStatus(String topic);

  List<Object[]> readyCountPerPartition(String topic);

  int purgeDoneBefore(String topic, Instant before);

  long deadLetterCountByTopic(String topic);

  List<DeadLetterData> findDeadLettersByTopicLimit(String topic, int limit);

  Long saveRecoveredMessage(DeadLetterData d);

  void deleteDeadLetterById(Long id);

  int purgeDeadLettersByTopic(String topic);
}
