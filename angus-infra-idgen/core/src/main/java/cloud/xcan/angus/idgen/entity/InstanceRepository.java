package cloud.xcan.angus.idgen.entity;

/**
 * Persistence operations for {@link Instance}. Core module only declares jpa capabilities, while
 * starter module provides Spring Data JPA implementation.
 */
public interface InstanceRepository {

  /**
   * Save instance row.
   */
  Instance save(Instance instance);

  /**
   * Get {@link Instance} by host and port.
   */
  Instance findByHostAndPort(String host, String port);

  /**
   * Increment instance id by pk and current id.
   */
  int incrementId(String pk, Long id);
}
