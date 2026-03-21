package cloud.xcan.angus.queue.core.spi;

import cloud.xcan.angus.queue.core.model.DeadLetterData;
import cloud.xcan.angus.queue.core.model.MessageData;
import cloud.xcan.angus.queue.core.model.PartitionCount;
import cloud.xcan.angus.queue.core.model.SendMessage;
import cloud.xcan.angus.queue.core.model.StatusCount;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface RepositoryAdapter {

  Long saveMessage(SendMessage msg);

  int leaseBatch(String topic, Collection<Integer> partitions, String owner, int leaseSec,
      int limit);

  List<MessageData> findLeasedByOwner(String owner, int limit);

  int ackBatch(Collection<Long> ids);

  int nackBatch(Collection<Long> ids, int backoffSec);

  int moveExceededToDeadLetter(int limit);

  int reclaimExpiredLeases(int limit);

  List<StatusCount> countByStatus(String topic);

  List<PartitionCount> readyCountPerPartition(String topic);

  int purgeDoneBefore(String topic, Instant before);

  long deadLetterCountByTopic(String topic);

  List<DeadLetterData> findDeadLettersByTopicLimit(String topic, int limit);

  List<Long> saveRecoveredMessages(Collection<DeadLetterData> items);

  int deleteDeadLettersByIds(Collection<Long> ids);

  int purgeDeadLettersByTopic(String topic);
}

