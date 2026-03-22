package cloud.xcan.angus.core.cache;

import java.util.Collection;
import org.springframework.cache.Cache;

/**
 * Cache manager SPI.
 *
 * <p>Allows for retrieving named {@link Cache} regions.
 */
public interface CacheManagerClear {

  default void clearLocal(String name, Object key) {
  }

  //default void clearLocal(String name, Collection<Object> keys) {};

  default void evict(String name, Collection<Object> keys) {
  }

}
