package cloud.xcan.angus.queue.spi;

/**
 * Optional capability for adapters that multitenancy soft-deleting DLQ records by topic.
 */
public interface SoftDeleteDlqSupport {

  int softDeleteDeadLettersByTopic(String topic);
}

