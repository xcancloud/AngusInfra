package cloud.xcan.angus.queue.starter.adapter;

import cloud.xcan.angus.queue.core.entity.DeadLetterEntity;
import cloud.xcan.angus.queue.core.entity.MessageEntity;
import cloud.xcan.angus.queue.core.model.DeadLetterData;
import cloud.xcan.angus.queue.core.model.MessageData;
import cloud.xcan.angus.queue.core.spi.RepositoryAdapter;
import cloud.xcan.angus.queue.core.spi.SoftDeleteDlqSupport;
import cloud.xcan.angus.queue.core.util.Partitioner;
import cloud.xcan.angus.queue.starter.repository.DeadLetterRepository;
import cloud.xcan.angus.queue.starter.repository.MessageRepository;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

public class JpaRepositoryAdapter implements RepositoryAdapter, SoftDeleteDlqSupport {

  private final MessageRepository messageRepository;
  private final DeadLetterRepository deadLetterRepository;

  public JpaRepositoryAdapter(MessageRepository messageRepository,
      DeadLetterRepository deadLetterRepository) {
    this.messageRepository = messageRepository;
    this.deadLetterRepository = deadLetterRepository;
  }

  private static MessageData toMessageData(MessageEntity e) {
    MessageData d = new MessageData();
    d.setId(e.getId());
    d.setTopic(e.getTopic());
    d.setPartitionId(e.getPartitionId());
    d.setPriority(e.getPriority());
    d.setPayload(e.getPayload());
    d.setHeaders(e.getHeaders());
    d.setStatus(e.getStatus());
    d.setVisibleAt(e.getVisibleAt());
    d.setLeaseUntil(e.getLeaseUntil());
    d.setLeaseOwner(e.getLeaseOwner());
    d.setAttempts(e.getAttempts());
    d.setMaxAttempts(e.getMaxAttempts());
    d.setIdempotencyKey(e.getIdempotencyKey());
    d.setCreatedAt(e.getCreatedAt());
    d.setUpdatedAt(e.getUpdatedAt());
    return d;
  }

  @Override
  @Transactional
  public Long saveMessage(String topic, String partitionKey, String payload, String headers,
      int priority, Instant visibleAt, String idempotencyKey, int maxAttempts, int numPartitions) {
    MessageEntity entity = new MessageEntity();
    entity.setTopic(topic);
    int partition = Partitioner.partition(topic, partitionKey, Math.max(1, numPartitions));
    entity.setPartitionId(partition);
    entity.setPriority(priority);
    entity.setPayload(payload);
    entity.setHeaders(headers);
    entity.setStatus(0);
    entity.setVisibleAt(visibleAt != null ? visibleAt : Instant.now());
    entity.setLeaseUntil(null);
    entity.setLeaseOwner(null);
    entity.setAttempts(0);
    entity.setMaxAttempts(maxAttempts <= 0 ? 16 : maxAttempts);
    entity.setIdempotencyKey(idempotencyKey);
    entity.setCreatedAt(Instant.now());
    entity.setUpdatedAt(Instant.now());
    return messageRepository.save(entity).getId();
  }

  @Override
  @Transactional
  public int leaseBatch(String topic, Collection<Integer> partitions, String owner, int leaseSec,
      int limit) {
    Instant leaseUntil = Instant.now().plusSeconds(Math.max(0, leaseSec));
    return messageRepository.leaseBatch(topic, partitions, owner, leaseUntil, limit);
  }

  @Override
  @Transactional(readOnly = true)
  public List<MessageData> findLeasedByOwner(String owner, Instant now, int limit) {
    return messageRepository.findLeasedByOwner(owner, now, limit).stream()
        .map(JpaRepositoryAdapter::toMessageData).toList();
  }

  @Override
  @Transactional
  public int ackBatch(Collection<Long> ids) {
    return ids == null || ids.isEmpty() ? 0 : messageRepository.ackBatch(ids);
  }

  @Override
  @Transactional
  public int nackBatch(Collection<Long> ids, int backoffSec) {
    Instant newVisibleAt = Instant.now().plusSeconds(Math.max(0, backoffSec));
    return ids == null || ids.isEmpty() ? 0 : messageRepository.nackBatch(ids, newVisibleAt);
  }

  @Override
  @Transactional
  public int moveExceededToDeadLetter(int limit) {
    List<MessageEntity> candidates = messageRepository.findAll().stream()
        .filter(m -> m.getAttempts() != null && m.getMaxAttempts() != null
            && m.getAttempts() >= m.getMaxAttempts())
        .limit(limit)
        .toList();
    int moved = 0;
    for (MessageEntity m : candidates) {
      DeadLetterEntity d = new DeadLetterEntity();
      d.setTopic(m.getTopic());
      d.setPartitionId(m.getPartitionId());
      d.setPayload(m.getPayload());
      d.setHeaders(m.getHeaders());
      d.setAttempts(m.getAttempts());
      d.setReason("max_attempts_exceeded");
      d.setCreatedAt(Instant.now());
      deadLetterRepository.save(d);
      messageRepository.deleteById(m.getId());
      moved++;
    }
    return moved;
  }

  @Override
  @Transactional
  public int reclaimExpiredLeases(int limit) {
    return messageRepository.reclaimExpiredLeases(limit);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Object[]> countByStatus(String topic) {
    return messageRepository.countByStatus(topic);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Object[]> readyCountPerPartition(String topic) {
    return messageRepository.readyCountPerPartition(topic);
  }

  @Override
  @Transactional
  public int purgeDoneBefore(String topic, Instant before) {
    return messageRepository.purgeDoneBefore(topic, before);
  }

  @Override
  @Transactional(readOnly = true)
  public long deadLetterCountByTopic(String topic) {
    return deadLetterRepository.countByTopic(topic);
  }

  @Override
  @Transactional(readOnly = true)
  public List<DeadLetterData> findDeadLettersByTopicLimit(String topic, int limit) {
    return deadLetterRepository.findByTopicLimit(topic, limit).stream().map(e -> {
      DeadLetterData d = new DeadLetterData();
      d.setId(e.getId());
      d.setTopic(e.getTopic());
      d.setPartitionId(e.getPartitionId());
      d.setPayload(e.getPayload());
      d.setHeaders(e.getHeaders());
      d.setAttempts(e.getAttempts());
      d.setReason(e.getReason());
      d.setCreatedAt(e.getCreatedAt());
      return d;
    }).toList();
  }

  @Override
  @Transactional
  public Long saveRecoveredMessage(DeadLetterData d) {
    MessageEntity m = new MessageEntity();
    m.setTopic(d.getTopic());
    m.setPartitionId(d.getPartitionId());
    m.setPriority(0);
    m.setPayload(d.getPayload());
    m.setHeaders(d.getHeaders());
    m.setStatus(0);
    m.setVisibleAt(Instant.now());
    m.setLeaseOwner(null);
    m.setLeaseUntil(null);
    m.setAttempts(0);
    m.setMaxAttempts(16);
    m.setCreatedAt(Instant.now());
    m.setUpdatedAt(Instant.now());
    return messageRepository.save(m).getId();
  }

  @Override
  @Transactional
  public void deleteDeadLetterById(Long id) {
    deadLetterRepository.deleteById(id);
  }

  @Override
  @Transactional
  public int purgeDeadLettersByTopic(String topic) {
    return deadLetterRepository.hardDeleteByTopic(topic);
  }

  @Override
  @Transactional
  public int softDeleteDeadLettersByTopic(String topic) {
    return deadLetterRepository.softDeleteByTopic(topic);
  }
}
