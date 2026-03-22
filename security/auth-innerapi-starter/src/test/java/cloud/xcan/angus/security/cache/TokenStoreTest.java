package cloud.xcan.angus.security.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cloud.xcan.angus.security.model.cache.DistributedTokenStore;
import cloud.xcan.angus.security.model.cache.LocalTokenStore;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TokenStoreTest {

  private static final String KEY = "auth:innerapi:token";
  private static final String TOKEN = "Bearer eyJhbGciOiJSUzI1NiJ9.test-token";

  @Nested
  @DisplayName("LocalTokenStore")
  class LocalTokenStoreTests {

    private LocalTokenStore store;

    @BeforeEach
    void setUp() {
      store = new LocalTokenStore();
    }

    @Test
    @DisplayName("store and retrieve a token")
    void storeAndRetrieve() {
      store.store(KEY, TOKEN, 60);

      Optional<String> result = store.retrieve(KEY);

      assertThat(result).isPresent().contains(TOKEN);
    }

    @Test
    @DisplayName("retrieve returns empty when no token stored")
    void retrieveReturnsEmptyWhenNoToken() {
      Optional<String> result = store.retrieve(KEY);

      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("store with TTL - token expires after TTL")
    void tokenExpiresAfterTtl() throws InterruptedException {
      store.store(KEY, TOKEN, 1);

      // Token should be available immediately
      assertThat(store.retrieve(KEY)).isPresent();

      // Wait for the token to expire (1s TTL + 100ms buffer)
      Thread.sleep(1100);

      assertThat(store.retrieve(KEY)).isEmpty();
    }

    @Test
    @DisplayName("remove clears the token")
    void removeClearsToken() {
      store.store(KEY, TOKEN, 60);
      assertThat(store.retrieve(KEY)).isPresent();

      store.remove(KEY);

      assertThat(store.retrieve(KEY)).isEmpty();
    }

    @Test
    @DisplayName("exists returns true when token present, false when absent")
    void existsReturnsTrueWhenPresent() {
      assertThat(store.exists(KEY)).isFalse();

      store.store(KEY, TOKEN, 60);

      assertThat(store.exists(KEY)).isTrue();
    }

    @Test
    @DisplayName("exists returns false after token expires")
    void existsReturnsFalseAfterExpiry() throws InterruptedException {
      store.store(KEY, TOKEN, 1);
      assertThat(store.exists(KEY)).isTrue();

      Thread.sleep(1100);

      assertThat(store.exists(KEY)).isFalse();
    }

    @Test
    @DisplayName("overwrite existing token")
    void overwriteExistingToken() {
      String newToken = "Bearer new-token-value";

      store.store(KEY, TOKEN, 60);
      assertThat(store.retrieve(KEY)).contains(TOKEN);

      store.store(KEY, newToken, 120);

      assertThat(store.retrieve(KEY)).isPresent().contains(newToken);
    }
  }

  @Nested
  @DisplayName("DistributedTokenStore")
  class DistributedTokenStoreTests {

    private MockDistributedCache mockCache;
    private DistributedTokenStore store;

    @BeforeEach
    void setUp() {
      mockCache = new MockDistributedCache();
      store = new DistributedTokenStore(mockCache);
    }

    @Test
    @DisplayName("store and retrieve via distributed cache")
    void storeAndRetrieve() {
      store.store(KEY, TOKEN, 300);

      Optional<String> result = store.retrieve(KEY);

      assertThat(result).isPresent().contains(TOKEN);
      assertThat(mockCache.data).containsEntry(KEY, TOKEN);
    }

    @Test
    @DisplayName("retrieve returns empty when cache miss")
    void retrieveReturnsEmptyOnCacheMiss() {
      Optional<String> result = store.retrieve(KEY);

      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("remove delegates to distributed cache")
    void removeDelegatesToCache() {
      store.store(KEY, TOKEN, 300);
      assertThat(mockCache.data).containsKey(KEY);

      store.remove(KEY);

      assertThat(mockCache.data).doesNotContainKey(KEY);
      assertThat(store.retrieve(KEY)).isEmpty();
    }

    @Test
    @DisplayName("exists returns correct values")
    void existsReturnsCorrectValues() {
      assertThat(store.exists(KEY)).isFalse();

      store.store(KEY, TOKEN, 300);

      assertThat(store.exists(KEY)).isTrue();
    }

    @Test
    @DisplayName("store throws RuntimeException when cache fails")
    void storeThrowsOnCacheFailure() {
      DistributedTokenStore failingStore =
          new DistributedTokenStore(new FailingDistributedCache());

      assertThatThrownBy(() -> failingStore.store(KEY, TOKEN, 60))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Distributed token store failure");
    }

    @Test
    @DisplayName("retrieve returns empty when cache throws exception")
    void retrieveReturnsEmptyOnCacheError() {
      DistributedTokenStore failingStore =
          new DistributedTokenStore(new FailingDistributedCache());

      Optional<String> result = failingStore.retrieve(KEY);

      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("remove handles cache exception gracefully")
    void removeHandlesExceptionGracefully() {
      DistributedTokenStore failingStore =
          new DistributedTokenStore(new FailingDistributedCache());

      // Should not throw - remove() catches exceptions
      failingStore.remove(KEY);
    }
  }
}
