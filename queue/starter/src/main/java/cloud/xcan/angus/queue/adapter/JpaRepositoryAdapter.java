package cloud.xcan.angus.queue.adapter;

import cloud.xcan.angus.queue.entity.DeadLetterEntity;
import cloud.xcan.angus.queue.entity.MessageEntity;
import cloud.xcan.angus.queue.entity.MessageStatus;
import cloud.xcan.angus.queue.model.DeadLetterData;
import cloud.xcan.angus.queue.model.MessageData;
import cloud.xcan.angus.queue.model.PartitionCount;
import cloud.xcan.angus.queue.model.SendMessage;
import cloud.xcan.angus.queue.model.StatusCount;
import cloud.xcan.angus.queue.spi.RepositoryAdapter;
import cloud.xcan.angus.queue.spi.SoftDeleteDlqSupport;
import cloud.xcan.angus.queue.util.Partitioner;
import cloud.xcan.angus.queue.repository.DeadLetterRepository;
import cloud.xcan.angus.queue.repository.MessageRepository;
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

  // P1-2: accept SendMessage DTO instead of 9 positional parameters
  @Override
  @Transactional
  public Long saveMessage(SendMessage msg) {
    MessageEntity entity = new MessageEntity();
    entity.setTopic(msg.getTopic());
    int numPartitions = msg.getNumPartitions() == null ? 1 : msg.getNumPartitions();
    int partition = Partitioner.partition(msg.getTopic(), msg.getPartitionKey(),
        Math.max(1, numPartitions));
    entity.setPartitionId(partition);
    entity.setPriority(msg.getPriority() == null ? 0 : msg.getPriority());
    entity.setPayload(msg.getPayload());
    entity.setHeaders(msg.getHeaders());
    entity.setStatus(MessageStatus.READY);   // P1-1: use enum constant
    Instant now = Instant.now();
    entity.setVisibleAt(msg.getVisibleAt() != null ? msg.getVisibleAt() : now);
    entity.setLeaseUntil(null);
    entity.setLeaseOwner(null);
    entity.setAttempts(0);
    entity.setMaxAttempts(
        msg.getMaxAttempts() == null || msg.getMaxAttempts() <= 0 ? 16 : msg.getMaxAttempts());
    entity.setIdempotencyKey(msg.getIdempotencyKey());
    entity.setCreatedAt(now);
    entity.setUpdatedAt(now);
    return messageRepository.save(entity).getId();
  }

  @Override
  @Transactional
  public int leaseBatch(String topic, Collection<Integer> partitions, String owner, int leaseSec,
      int limit) {
    Instant leaseUntil = Instant.now().plusSeconds(Math.max(0, leaseSec));
    return messageRepository.leaseBatch(topic, partitions, owner, leaseUntil, limit);
  }

  // P1-6: removed 'now' parameter — adapter calls NOW() in SQL directly
  @Override
  @Transactional(readOnly = true)
  public List<MessageData> findLeasedByOwner(String owner, int limit) {
    return messageRepository.findLeasedByOwner(owner, limit).stream()
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
    List<MessageEntity> candidates = messageRepository.findExceededAttempts(limit);
    if (candidates.isEmpty()) {
      return 0;
    }
    Instant now = Instant.now();
    List<DeadLetterEntity> dlqEntries = candidates.stream().map(m -> {
      DeadLetterEntity d = new DeadLetterEntity();
      d.setTopic(m.getTopic());
      d.setPartitionId(m.getPartitionId());
      d.setPayload(m.getPayload());
      d.setHeaders(m.getHeaders());
      d.setAttempts(m.getAttempts());
      d.setReason("max_attempts_exceeded");
      d.setCreatedAt(now);
      return d;
    }).toList();
    deadLetterRepository.saveAll(dlqEntries);
    List<Long> ids = candidates.stream().map(MessageEntity::getId).toList();
    messageRepository.deleteByIds(ids);
    return candidates.size();
  }

  @Override
  @Transactional
  public int reclaimExpiredLeases(int limit) {
    return messageRepository.reclaimExpiredLeases(limit);
  }

  // P1-4: return typed StatusCount records instead of Object[]
  @Override
  @Transactional(readOnly = true)
  public List<StatusCount> countByStatus(String topic) {
    return messageRepository.countByStatus(topic).stream()
        .map(row -> new StatusCount(((Number) row[0]).intValue(), ((Number) row[1]).longValue()))
        .toList();
  }

  // P1-4: return typed PartitionCount records instead of Object[]
  @Override
  @Transactional(readOnly = true)
  public List<PartitionCount> readyCountPerPartition(String topic) {
    return messageRepository.readyCountPerPartition(topic).stream()
        .map(row -> new PartitionCount(((Number) row[0]).intValue(), ((Number) row[1]).longValue()))
        .toList();
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

  // P1-7: map deletedAt field into the DTO
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
      d.setDeletedAt(e.getDeletedAt());
      return d;
    }).toList();
  }

  // P1-3: batch save recovered messages
  @Override
  @Transactional
  public List<Long> saveRecoveredMessages(Collection<DeadLetterData> items) {
    Instant now = Instant.now();
    List<MessageEntity> entities = items.stream().map(d -> {
      MessageEntity m = new MessageEntity();
      m.setTopic(d.getTopic());
      m.setPartitionId(d.getPartitionId());
      m.setPriority(0);
      m.setPayload(d.getPayload());
      m.setHeaders(d.getHeaders());
      m.setStatus(MessageStatus.READY);   // P1-1: use enum constant
      m.setVisibleAt(now);
      m.setLeaseOwner(null);
      m.setLeaseUntil(null);
      m.setAttempts(0);
      m.setMaxAttempts(16);
      m.setCreatedAt(now);
      m.setUpdatedAt(now);
      return m;
    }).toList();
    return messageRepository.saveAll(entities).stream().map(MessageEntity::getId).toList();
  }

  // P1-3: batch delete dead letters
  @Override
  @Transactional
  public int deleteDeadLettersByIds(Collection<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return 0;
    }
    deadLetterRepository.deleteAllById(ids);
    return ids.size();
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
