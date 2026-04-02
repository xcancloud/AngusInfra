package cloud.xcan.angus.queue.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cloud.xcan.angus.queue.entity.DeadLetterEntity;
import cloud.xcan.angus.queue.entity.MessageEntity;
import cloud.xcan.angus.queue.entity.MessageStatus;
import cloud.xcan.angus.queue.model.DeadLetterData;
import cloud.xcan.angus.queue.model.MessageData;
import cloud.xcan.angus.queue.model.SendMessage;
import cloud.xcan.angus.queue.model.StatusCount;
import cloud.xcan.angus.queue.repository.DeadLetterRepository;
import cloud.xcan.angus.queue.repository.MessageRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest(
    properties = {
        "spring.datasource.url=jdbc:h2:mem:queue-jpa-adapter;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
    }
)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@EntityScan(basePackageClasses = {MessageEntity.class, DeadLetterEntity.class})
@EnableJpaRepositories(basePackageClasses = {MessageRepository.class, DeadLetterRepository.class})
@Import(JpaRepositoryAdapter.class)
@Transactional
class JpaRepositoryAdapterTest {

  @Autowired
  JpaRepositoryAdapter adapter;

  @PersistenceContext
  EntityManager entityManager;

  @Autowired
  MessageRepository messageRepository;

  @Autowired
  DeadLetterRepository deadLetterRepository;

  @Test
  void saveMessagePersistsAndReturnsId() {
    Long id = adapter.saveMessage(SendMessage.builder()
        .topic("orders")
        .partitionKey("k1")
        .payload("{\"x\":1}")
        .headers("{\"h\":1}")
        .priority(1)
        .maxAttempts(8)
        .numPartitions(4)
        .build());
    assertNotNull(id);
    MessageEntity e = messageRepository.findById(id).orElseThrow();
    assertEquals("orders", e.getTopic());
    assertEquals(8, e.getMaxAttempts());
    assertEquals(MessageStatus.READY, e.getStatus());
  }

  @Test
  void ackAndNackReturnZeroForNullOrEmptyIds() {
    assertEquals(0, adapter.ackBatch(null));
    assertEquals(0, adapter.ackBatch(List.of()));
    assertEquals(0, adapter.nackBatch(null, 1));
    assertEquals(0, adapter.nackBatch(List.of(), 1));
  }

  @Test
  void deleteDeadLettersByIdsReturnsZeroForNullOrEmpty() {
    assertEquals(0, adapter.deleteDeadLettersByIds(null));
    assertEquals(0, adapter.deleteDeadLettersByIds(List.of()));
  }

  @Test
  void moveExceededToDeadLetterReturnsZeroWhenNone() {
    assertEquals(0, adapter.moveExceededToDeadLetter(10));
  }

  @Test
  void moveExceededToDeadLetterMovesAndDeletesMessage() {
    Instant now = Instant.now();
    MessageEntity m = new MessageEntity();
    m.setTopic("t");
    m.setPartitionId(0);
    m.setPriority(0);
    m.setPayload("{}");
    m.setHeaders(null);
    m.setStatus(MessageStatus.READY);
    m.setVisibleAt(now);
    m.setAttempts(3);
    m.setMaxAttempts(3);
    m.setCreatedAt(now);
    m.setUpdatedAt(now);
    messageRepository.saveAndFlush(m);

    int moved = adapter.moveExceededToDeadLetter(10);
    assertEquals(1, moved);
    // Native DELETE does not remove entities from the persistence context; clear so findById hits DB.
    entityManager.flush();
    entityManager.clear();
    assertTrue(messageRepository.findById(m.getId()).isEmpty());
    assertEquals(1, deadLetterRepository.findAll().size());
  }

  @Test
  void countByStatusAndReadyPerPartitionAndDlqCount() {
    adapter.saveMessage(SendMessage.builder()
        .topic("s")
        .partitionKey("a")
        .payload("{}")
        .numPartitions(2)
        .build());

    List<StatusCount> byStatus = adapter.countByStatus("s");
    assertFalse(byStatus.isEmpty());
    assertTrue(adapter.readyCountPerPartition("s").stream().mapToLong(sc -> sc.count()).sum() >= 1);
    assertEquals(0L, adapter.deadLetterCountByTopic("s"));
  }

  @Test
  void saveRecoveredMessagesInsertsReadyRows() {
    DeadLetterData d = new DeadLetterData();
    d.setTopic("rt");
    d.setPartitionId(0);
    d.setPayload("{}");
    d.setHeaders(null);
    d.setAttempts(1);
    d.setReason("r");
    d.setCreatedAt(Instant.now());

    List<Long> newIds = adapter.saveRecoveredMessages(List.of(d));
    assertEquals(1, newIds.size());
    MessageEntity inserted = messageRepository.findById(newIds.get(0)).orElseThrow();
    assertEquals("rt", inserted.getTopic());
    assertEquals(MessageStatus.READY, inserted.getStatus());
  }

  @Test
  void deleteDeadLettersByIdsAndHardPurgeByTopic() {
    Instant now = Instant.now();
    DeadLetterEntity dl = new DeadLetterEntity();
    dl.setTopic("hp");
    dl.setPartitionId(0);
    dl.setPayload("{}");
    dl.setHeaders(null);
    dl.setAttempts(0);
    dl.setReason("r");
    dl.setCreatedAt(now);
    deadLetterRepository.saveAndFlush(dl);
    assertEquals(1, adapter.deleteDeadLettersByIds(List.of(dl.getId())));
    assertTrue(deadLetterRepository.findById(dl.getId()).isEmpty());
    DeadLetterEntity dl2 = new DeadLetterEntity();
    dl2.setTopic("hp");
    dl2.setPartitionId(0);
    dl2.setPayload("{}");
    dl2.setHeaders(null);
    dl2.setAttempts(0);
    dl2.setReason("r");
    dl2.setCreatedAt(now);
    deadLetterRepository.saveAndFlush(dl2);
    assertTrue(adapter.purgeDeadLettersByTopic("hp") >= 1);
  }

  @Test
  void softDeleteDeadLettersByTopic() {
    Instant now = Instant.now();
    DeadLetterEntity dl = new DeadLetterEntity();
    dl.setTopic("st");
    dl.setPartitionId(0);
    dl.setPayload("{}");
    dl.setHeaders(null);
    dl.setAttempts(0);
    dl.setReason("x");
    dl.setCreatedAt(now);
    deadLetterRepository.saveAndFlush(dl);
    assertTrue(adapter.softDeleteDeadLettersByTopic("st") >= 1);
  }

  @Test
  void reclaimPurgeDoneAndFindDeadLettersEmpty() {
    assertEquals(0, adapter.reclaimExpiredLeases(5));
    assertEquals(0, adapter.purgeDoneBefore("no-topic", Instant.now()));
    assertTrue(adapter.findDeadLettersByTopicLimit("no-topic", 3).isEmpty());
  }

  @Test
  void leaseAckFlow() {
    Long id = adapter.saveMessage(SendMessage.builder()
        .topic("lease-t")
        .partitionKey("pk")
        .payload("{}")
        .numPartitions(2)
        .build());
    int leased = adapter.leaseBatch("lease-t", List.of(0, 1), "owner1", 60, 10);
    assertTrue(leased >= 1);
    List<MessageData> held = adapter.findLeasedByOwner("owner1", 10);
    assertFalse(held.isEmpty());
    assertEquals(1, adapter.ackBatch(List.of(id)));
  }
}
