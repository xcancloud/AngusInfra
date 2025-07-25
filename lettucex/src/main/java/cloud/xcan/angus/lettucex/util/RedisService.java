package cloud.xcan.angus.lettucex.util;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

/**
 * <p>
 * A comprehensive Redis service utility class that provides high-level operations
 * for all major Redis data types including String, Hash, List, Set, and ZSet.
 * This service acts as a wrapper around Spring's RedisTemplate to provide
 * a more convenient and type-safe API for Redis operations.
 * </p>
 * 
 * <p>
 * Key features:
 * - Type-safe operations with generic support
 * - Comprehensive coverage of Redis commands
 * - Consistent API design across all data types
 * - Integration with Spring Data Redis
 * - Support for advanced operations like scanning and cursors
 * </p>
 * 
 * <p>
 * Supported Redis data types:
 * - String: Basic key-value operations with expiration support
 * - Hash: Field-value operations for structured data
 * - List: Ordered collection operations with blocking support
 * - Set: Unordered collection operations with set algebra
 * - ZSet: Sorted set operations with score-based ordering
 * </p>
 * 
 * <p>
 * Usage example:
 * <pre>
 * RedisService&lt;String&gt; redisService = new RedisService&lt;&gt;(redisTemplate);
 * 
 * // String operations
 * redisService.set("key", "value", 60, TimeUnit.SECONDS);
 * String value = redisService.get("key");
 * 
 * // Hash operations
 * redisService.hPut("user:1", "name", "John");
 * redisService.hPut("user:1", "age", "30");
 * Map&lt;Object, Object&gt; user = redisService.hGetAll("user:1");
 * </pre>
 * </p>
 * 
 * <p>
 * Thread Safety: This class is thread-safe when used with a thread-safe RedisTemplate.
 * </p>
 * 
 * @param <T> the type of values stored in Redis
 */
public class RedisService<T> {

  /**
   * The underlying RedisTemplate used for all Redis operations.
   * This template handles serialization, connection management, and low-level Redis commands.
   */
  private RedisTemplate<String, T> redisTemplate;

  /**
   * <p>
   * Default constructor for creating an uninitialized RedisService.
   * The RedisTemplate must be set using {@link #setRedisTemplate(RedisTemplate)} before use.
   * </p>
   */
  public RedisService() {
  }

  /**
   * <p>
   * Constructor for creating a RedisService with the specified RedisTemplate.
   * </p>
   *
   * @param redisTemplate the RedisTemplate to use for Redis operations
   */
  public RedisService(RedisTemplate<String, T> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  /**
   * <p>
   * Gets the underlying RedisTemplate instance.
   * </p>
   *
   * @return the RedisTemplate used by this service
   */
  public RedisTemplate<String, T> getRedisTemplate() {
    return this.redisTemplate;
  }

  /**
   * <p>
   * Sets the RedisTemplate to be used by this service.
   * </p>
   *
   * @param redisTemplate the RedisTemplate to use for Redis operations
   */
  public void setRedisTemplate(RedisTemplate<String, T> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  /*----------------------Key Operations-----------------------*/

  /**
   * <p>
   * Deletes the specified key from Redis.
   * This operation is atomic and will remove the key and its associated value.
   * </p>
   *
   * @param key the key to delete
   */
  public void delete(String key) {
    redisTemplate.delete(key);
  }

  /**
   * <p>
   * Deletes multiple keys from Redis in a single operation.
   * This is more efficient than deleting keys individually.
   * </p>
   *
   * @param keys the collection of keys to delete
   */
  public void delete(Collection<String> keys) {
    redisTemplate.delete(keys);
  }

  /**
   * <p>
   * Creates a serialized dump of the value stored at the specified key.
   * The dump can be restored using the RESTORE command.
   * </p>
   *
   * @param key the key to dump
   * @return serialized representation of the value
   */
  public byte[] dump(String key) {
    return redisTemplate.dump(key);
  }

  /**
   * <p>
   * Checks if the specified key exists in Redis.
   * </p>
   *
   * @param key the key to check
   * @return true if the key exists, false otherwise
   */
  public Boolean hasKey(String key) {
    return redisTemplate.hasKey(key);
  }

  /**
   * <p>
   * Sets an expiration time for the specified key.
   * After the timeout, the key will be automatically deleted.
   * </p>
   *
   * @param key the key to set expiration for
   * @param timeout the expiration timeout
   * @param unit the time unit for the timeout
   * @return true if the expiration was set successfully, false otherwise
   */
  public Boolean expire(String key, long timeout, TimeUnit unit) {
    return redisTemplate.expire(key, timeout, unit);
  }

  /**
   * <p>
   * Sets an absolute expiration time for the specified key.
   * The key will be automatically deleted at the specified date.
   * </p>
   *
   * @param key the key to set expiration for
   * @param date the absolute expiration date
   * @return true if the expiration was set successfully, false otherwise
   */
  public Boolean expireAt(String key, Date date) {
    return redisTemplate.expireAt(key, date);
  }

  /**
   * <p>
   * Finds all keys matching the specified pattern.
   * Warning: This operation can be slow on large datasets. Use with caution in production.
   * </p>
   *
   * @param pattern the pattern to match (supports wildcards like * and ?)
   * @return set of keys matching the pattern
   */
  public Set<String> keys(String pattern) {
    return redisTemplate.keys(pattern);
  }

  /**
   * <p>
   * Moves the specified key from the current database to the specified database.
   * </p>
   *
   * @param key the key to move
   * @param dbIndex the target database index
   * @return true if the key was moved successfully, false otherwise
   */
  public Boolean move(String key, int dbIndex) {
    return redisTemplate.move(key, dbIndex);
  }

  /**
   * <p>
   * Removes the expiration from the specified key, making it persistent.
   * </p>
   *
   * @param key the key to make persistent
   * @return true if the expiration was removed, false if the key doesn't exist or has no expiration
   */
  public Boolean persist(String key) {
    return redisTemplate.persist(key);
  }

  /**
   * <p>
   * Gets the remaining time-to-live for the specified key.
   * </p>
   *
   * @param key the key to check
   * @param unit the time unit for the result
   * @return the remaining TTL, or -1 if the key doesn't exist, -2 if the key has no expiration
   */
  public Long getExpire(String key, TimeUnit unit) {
    return redisTemplate.getExpire(key, unit);
  }

  /**
   * <p>
   * Gets the remaining time-to-live for the specified key in seconds.
   * </p>
   *
   * @param key the key to check
   * @return the remaining TTL in seconds, or -1 if the key doesn't exist, -2 if the key has no expiration
   */
  public Long getExpire(String key) {
    return redisTemplate.getExpire(key);
  }

  /**
   * <p>
   * Returns a random key from the currently selected database.
   * </p>
   *
   * @return a random key, or null if the database is empty
   */
  public String randomKey() {
    return redisTemplate.randomKey();
  }

  /**
   * <p>
   * Renames the specified key to a new name.
   * If the new key already exists, it will be overwritten.
   * </p>
   *
   * @param oldKey the current key name
   * @param newKey the new key name
   */
  public void rename(String oldKey, String newKey) {
    redisTemplate.rename(oldKey, newKey);
  }

  /**
   * <p>
   * Renames the specified key to a new name only if the new key doesn't exist.
   * </p>
   *
   * @param oldKey the current key name
   * @param newKey the new key name
   * @return true if the key was renamed, false if the new key already exists
   */
  public Boolean renameIfAbsent(String oldKey, String newKey) {
    return redisTemplate.renameIfAbsent(oldKey, newKey);
  }

  /**
   * <p>
   * Returns the data type of the value stored at the specified key.
   * </p>
   *
   * @param key the key to check
   * @return the data type of the value
   */
  public DataType type(String key) {
    return redisTemplate.type(key);
  }

  /* -------------------String Operations --------------------- */

  /**
   * <p>
   * Sets the value for the specified key.
   * This is the most basic Redis operation for storing data.
   * </p>
   *
   * @param key the key to set
   * @param value the value to store
   */
  public void set(String key, T value) {
    redisTemplate.opsForValue().set(key, value);
  }

  /**
   * <p>
   * Sets the value for the specified key with an expiration time.
   * The key will be automatically deleted after the specified time.
   * </p>
   *
   * @param key the key to set
   * @param value the value to store
   * @param time the expiration time
   * @param unit the time unit for the expiration
   */
  public void set(String key, T value, long time, TimeUnit unit) {
    redisTemplate.opsForValue().set(key, value, time, unit);
  }

  /**
   * <p>
   * Gets the value associated with the specified key.
   * </p>
   *
   * @param key the key to retrieve
   * @return the value associated with the key, or null if the key doesn't exist
   */
  public T get(String key) {
    return redisTemplate.opsForValue().get(key);
  }

  /**
   * <p>
   * Gets a substring of the value of the specified key between the specified start and end.
   * </p>
   *
   * @param key the key to get the substring from
   * @param start the start index (inclusive)
   * @param end the end index (exclusive)
   * @return the substring of the value, or null if the key doesn't exist
   */
  public String getRange(String key, long start, long end) {
    return redisTemplate.opsForValue().get(key, start, end);
  }

  /**
   * <p>
   * Sets the value of the specified key and returns its old value.
   * </p>
   *
   * @param key the key to set
   * @param value the new value to set
   * @return the old value associated with the key, or null if the key didn't exist
   */
  public T getAndSet(String key, T value) {
    return redisTemplate.opsForValue().getAndSet(key, value);
  }

  /**
   * <p>
   * Gets the bit value at the specified offset in the value of the specified key.
   * </p>
   *
   * @param key the key to get the bit from
   * @param offset the bit offset
   * @return true if the bit is 1, false if the bit is 0, or null if the key doesn't exist
   */
  public Boolean getBit(String key, long offset) {
    return redisTemplate.opsForValue().getBit(key, offset);
  }

  /**
   * <p>
   * Gets multiple values for the specified keys.
   * The values are returned in the order of the requested keys.
   * </p>
   *
   * @param keys the collection of keys to get values for
   * @return a list of values, or null if any key doesn't exist
   */
  public List<T> multiGet(Collection<String> keys) {
    return redisTemplate.opsForValue().multiGet(keys);
  }

  /**
   * <p>
   * Sets the bit at the specified offset in the value of the specified key.
   * </p>
   *
   * @param key the key to set the bit in
   * @param offset the bit offset
   * @param value the bit value (true for 1, false for 0)
   * @return true if the bit was set, false otherwise
   */
  public Boolean setBit(String key, long offset, boolean value) {
    return redisTemplate.opsForValue().setBit(key, offset, value);
  }

  /**
   * <p>
   * Sets the value for the specified key with an expiration time.
   * The key will be automatically deleted after the specified time.
   * </p>
   *
   * @param key the key to set
   * @param value the value to store
   * @param timeout the expiration time
   * @param unit the time unit for the expiration
   */
  public void setEx(String key, T value, long timeout, TimeUnit unit) {
    redisTemplate.opsForValue().set(key, value, timeout, unit);
  }

  /**
   * <p>
   * Sets the value for the specified key only if the key does not exist.
   * </p>
   *
   * @param key the key to set
   * @param value the value to store
   * @return true if the key was set, false if the key already exists
   */
  public Boolean setIfAbsent(String key, T value) {
    return redisTemplate.opsForValue().setIfAbsent(key, value);
  }

  /**
   * <p>
   * Sets the value for the specified key with an expiration time only if the key does not exist.
   * </p>
   *
   * @param key the key to set
   * @param value the value to store
   * @param timeout the expiration time
   * @param unit the time unit for the expiration
   * @return true if the key was set, false if the key already exists
   */
  public Boolean setIfAbsent(String key, T value, long timeout, TimeUnit unit) {
    return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
  }

  /**
   * <p>
   * Overwrites parts of the specified key starting at the specified offset with the given value.
   * </p>
   *
   * @param key the key to overwrite
   * @param value the value to overwrite with
   * @param offset the start offset
   */
  public void setRange(String key, T value, long offset) {
    redisTemplate.opsForValue().set(key, value, offset);
  }

  /**
   * <p>
   * Gets the length of the value stored at the specified key.
   * </p>
   *
   * @param key the key to get the length for
   * @return the length of the value, or null if the key doesn't exist
   */
  public Long size(String key) {
    return redisTemplate.opsForValue().size(key);
  }

  /**
   * <p>
   * Sets multiple keys to multiple values using key-value pairs provided in the map.
   * </p>
   *
   * @param maps the map of keys and values to set
   */
  public void multiSet(Map<String, T> maps) {
    redisTemplate.opsForValue().multiSet(maps);
  }

  /**
   * <p>
   * Sets multiple keys to multiple values using key-value pairs provided in the map only if
   * the provided key does not exist.
   * </p>
   *
   * @param maps the map of keys and values to set
   * @return true if all keys were set, false otherwise
   */
  public Boolean multiSetIfAbsent(Map<String, T> maps) {
    return redisTemplate.opsForValue().multiSetIfAbsent(maps);
  }

  /**
   * <p>
   * Increments the integer value stored as a string value under the specified key by the given delta.
   * </p>
   *
   * @param key the key to increment
   * @param increment the amount to increment by
   * @return the new value after incrementing, or null if the key doesn't exist
   */
  public Long incrBy(String key, long increment) {
    return redisTemplate.opsForValue().increment(key, increment);
  }

  /**
   * <p>
   * Increments the floating point number value stored as a string value under the specified key by
   * the given delta.
   * </p>
   *
   * @param key the key to increment
   * @param increment the amount to increment by
   * @return the new value after incrementing, or null if the key doesn't exist
   */
  public Double incrByFloat(String key, double increment) {
    return redisTemplate.opsForValue().increment(key, increment);
  }

  /**
   * <p>
   * Appends the specified value to the specified key.
   * </p>
   *
   * @param key the key to append to
   * @param value the value to append
   * @return the length of the new value, or null if the key doesn't exist
   */
  public Integer append(String key, String value) {
    return redisTemplate.opsForValue().append(key, value);
  }

  /* -------------------Hash Operations------------------------- */

  /**
   * <p>
   * Gets the value for the specified hash field from the hash at the specified key.
   * </p>
   *
   * @param key the key to get the hash from
   * @param field the hash field to get
   * @return the value for the field, or null if the key or field doesn't exist
   */
  public Object hGet(String key, String field) {
    return redisTemplate.opsForHash().get(key, field);
  }

  /**
   * <p>
   * Gets the entire hash stored at the specified key.
   * </p>
   *
   * @param key the key to get the hash for
   * @return a map of all fields and their values in the hash
   */
  public Map<Object, Object> hGetAll(String key) {
    return redisTemplate.opsForHash().entries(key);
  }

  /**
   * <p>
   * Gets multiple values for the specified hash fields from the hash at the specified key.
   * </p>
   *
   * @param key the key to get hash fields from
   * @param fields the collection of hash fields to get
   * @return a list of values, or null if any field doesn't exist
   */
  public List<Object> hMultiGet(String key, Collection<Object> fields) {
    return redisTemplate.opsForHash().multiGet(key, fields);
  }

  /**
   * <p>
   * Sets the value of a hash field.
   * </p>
   *
   * @param key the key to set the hash field in
   * @param hashKey the hash field to set
   * @param value the value to set
   */
  public void hPut(String key, String hashKey, String value) {
    redisTemplate.opsForHash().put(key, hashKey, value);
  }

  /**
   * <p>
   * Sets multiple hash fields to multiple values using data provided in the map.
   * </p>
   *
   * @param key the key to set hash fields in
   * @param maps the map of hash fields and values to set
   */
  public void hPutAll(String key, Map<String, String> maps) {
    redisTemplate.opsForHash().putAll(key, maps);
  }

  /**
   * <p>
   * Sets the value of a hash field only if the hash field does not exist.
   * </p>
   *
   * @param key the key to set the hash field in
   * @param hashKey the hash field to set
   * @param value the value to set
   * @return true if the field was set, false if the field already exists
   */
  public Boolean hPutIfAbsent(String key, String hashKey, String value) {
    return redisTemplate.opsForHash().putIfAbsent(key, hashKey, value);
  }

  /**
   * <p>
   * Deletes the specified hash fields.
   * </p>
   *
   * @param key the key to delete hash fields from
   * @param hashKeys the collection of hash fields to delete
   * @return the number of fields that were deleted
   */
  public Long hDelete(String key, Object... hashKeys) {
    return redisTemplate.opsForHash().delete(key, hashKeys);
  }

  /**
   * <p>
   * Determines if the specified hash field exists.
   * </p>
   *
   * @param key the key to check
   * @param hashKey the hash field to check
   * @return true if the field exists, false otherwise
   */
  public boolean hExists(String key, String hashKey) {
    return redisTemplate.opsForHash().hasKey(key, hashKey);
  }

  /**
   * <p>
   * Increments the value of a hash field by the given delta.
   * </p>
   *
   * @param key the key to increment
   * @param hashKey the hash field to increment
   * @param increment the amount to increment by
   * @return the new value after incrementing, or null if the key or field doesn't exist
   */
  public Long hIncrBy(String key, T hashKey, long increment) {
    return redisTemplate.opsForHash().increment(key, hashKey, increment);
  }

  /**
   * <p>
   * Increments the value of a hash field by the given delta.
   * </p>
   *
   * @param key the key to increment
   * @param hashKey the hash field to increment
   * @param delta the amount to increment by
   * @return the new value after incrementing, or null if the key or field doesn't exist
   */
  public Double hIncrByFloat(String key, T hashKey, double delta) {
    return redisTemplate.opsForHash().increment(key, hashKey, delta);
  }

  /**
   * <p>
   * Gets the key set (fields) of the hash at the specified key.
   * </p>
   *
   * @param key the key to get hash fields for
   * @return a set of hash fields
   */
  public Set<Object> hKeys(String key) {
    return redisTemplate.opsForHash().keys(key);
  }

  /**
   * <p>
   * Gets the size of the hash at the specified key.
   * </p>
   *
   * @param key the key to get the size for
   * @return the size of the hash, or null if the key doesn't exist
   */
  public Long hSize(String key) {
    return redisTemplate.opsForHash().size(key);
  }

  /**
   * <p>
   * Gets the entry set (values) of the hash at the specified key.
   * </p>
   *
   * @param key the key to get hash values for
   * @return a list of hash values
   */
  public List<Object> hValues(String key) {
    return redisTemplate.opsForHash().values(key);
  }

  /**
   * <p>
   * Uses a {@link Cursor} to iterate over entries in the hash at the specified key.
   * <strong>Important:</strong> Call {@link Cursor#close()} when done to avoid resource leak.
   * </p>
   *
   * @param key the key to scan
   * @param options the scan options
   * @return a cursor for iterating over hash entries
   */
  public Cursor<Entry<Object, Object>> hScan(String key, ScanOptions options) {
    return redisTemplate.opsForHash().scan(key, options);
  }

  /* ------------------------List Operations ---------------------------- */

  /**
   * <p>
   * Gets the element at the specified index from the list at the specified key.
   * </p>
   *
   * @param key the key to get the list from
   * @param index the index to get
   * @return the element at the index, or null if the key or index is invalid
   */
  public T lIndex(String key, long index) {
    return redisTemplate.opsForList().index(key, index);
  }

  /**
   * <p>
   * Gets elements between the specified start and end from the list at the specified key.
   * </p>
   *
   * @param key the key to get the list from
   * @param start the start index (inclusive)
   * @param end the end index (exclusive)
   * @return a list of elements
   */
  public List<T> lRange(String key, long start, long end) {
    return redisTemplate.opsForList().range(key, start, end);
  }

  /**
   * <p>
   * Prepends the specified value to the specified key.
   * </p>
   *
   * @param key the key to prepend to
   * @param value the value to prepend
   * @return the new length of the list, or null if the key doesn't exist
   */
  public Long lLeftPush(String key, T value) {
    return redisTemplate.opsForList().leftPush(key, value);
  }

  /**
   * <p>
   * Prepends multiple values to the specified key.
   * </p>
   *
   * @param key the key to prepend to
   * @param values the collection of values to prepend
   * @return the new length of the list, or null if the key doesn't exist
   */
  public Long lLeftPushAll(String key, Collection<T> values) {
    return redisTemplate.opsForList().leftPushAll(key, values);
  }

  /**
   * <p>
   * Prepends multiple values to the specified key only if the list exists.
   * </p>
   *
   * @param key the key to prepend to
   * @param value the value to prepend
   * @return the new length of the list, or null if the key doesn't exist
   */
  public Long lLeftPushIfPresent(String key, T value) {
    return redisTemplate.opsForList().leftPushIfPresent(key, value);
  }

  /**
   * <p>
   * Prepends the specified value to the specified key before the specified pivot value.
   * </p>
   *
   * @param key the key to prepend to
   * @param pivot the pivot value
   * @param value the value to prepend
   * @return the new length of the list, or null if the key doesn't exist
   */
  public Long lLeftPush(String key, T pivot, T value) {
    return redisTemplate.opsForList().leftPush(key, pivot, value);
  }

  /**
   * <p>
   * Appends the specified value to the specified key.
   * </p>
   *
   * @param key the key to append to
   * @param value the value to append
   * @return the new length of the list, or null if the key doesn't exist
   */
  public Long lRightPush(String key, T value) {
    return redisTemplate.opsForList().rightPush(key, value);
  }

  /**
   * <p>
   * Appends multiple values to the specified key.
   * </p>
   *
   * @param key the key to append to
   * @param values the collection of values to append
   * @return the new length of the list, or null if the key doesn't exist
   */
  public Long lRightPushAll(String key, Collection<T> values) {
    return redisTemplate.opsForList().rightPushAll(key, values);
  }

  /**
   * <p>
   * Appends multiple values to the specified key only if the list exists.
   * </p>
   *
   * @param key the key to append to
   * @param value the value to append
   * @return the new length of the list, or null if the key doesn't exist
   */
  public Long lRightPushIfPresent(String key, T value) {
    return redisTemplate.opsForList().rightPushIfPresent(key, value);
  }

  /**
   * <p>
   * Appends the specified value to the specified key before the specified pivot value.
   * </p>
   *
   * @param key the key to append to
   * @param pivot the pivot value
   * @param value the value to append
   * @return the new length of the list, or null if the key doesn't exist
   */
  public Long lRightPush(String key, T pivot, T value) {
    return redisTemplate.opsForList().rightPush(key, pivot, value);
  }

  /**
   * <p>
   * Sets the element at the specified index in the list stored at the specified key.
   * </p>
   *
   * @param key the key to set the element in
   * @param index the index to set
   * @param value the value to set
   */
  public void lSet(String key, long index, T value) {
    redisTemplate.opsForList().set(key, index, value);
  }

  /**
   * <p>
   * Removes and returns the first element from the list stored at the specified key.
   * </p>
   *
   * @param key the key to remove from
   * @return the removed element, or null if the key doesn't exist
   */
  public T lLeftPop(String key) {
    return redisTemplate.opsForList().leftPop(key);
  }

  /**
   * <p>
   * Removes and returns the first element from the lists stored at the specified key.
   * <b>Blocks connection</b> until element available or {@code timeout} reached.
   * </p>
   *
   * @param key the key to remove from
   * @param timeout the timeout
   * @param unit the time unit for the timeout
   * @return the removed element, or null if the key doesn't exist or timeout reached
   */
  public T lBLeftPop(String key, long timeout, TimeUnit unit) {
    return redisTemplate.opsForList().leftPop(key, timeout, unit);
  }

  /**
   * <p>
   * Removes and returns the last element from the list stored at the specified key.
   * </p>
   *
   * @param key the key to remove from
   * @return the removed element, or null if the key doesn't exist
   */
  public T lRightPop(String key) {
    return redisTemplate.opsForList().rightPop(key);
  }

  /**
   * <p>
   * Removes and returns the last element from the lists stored at the specified key.
   * <b>Blocks connection</b> until element available or {@code timeout} reached.
   * </p>
   *
   * @param key the key to remove from
   * @param timeout the timeout
   * @param unit the time unit for the timeout
   * @return the removed element, or null if the key doesn't exist or timeout reached
   */
  public T lBRightPop(String key, long timeout, TimeUnit unit) {
    return redisTemplate.opsForList().rightPop(key, timeout, unit);
  }

  /**
   * <p>
   * Removes the last element from the list at {@code sourceKey}, appends it to {@code destinationKey}
   * and returns its value.
   * </p>
   *
   * @param sourceKey the source key to remove from
   * @param destinationKey the destination key to append to
   * @return the value of the removed element, or null if the source key doesn't exist
   */
  public T lRightPopAndLeftPush(String sourceKey, String destinationKey) {
    return redisTemplate.opsForList().rightPopAndLeftPush(sourceKey,
        destinationKey);
  }

  /**
   * <p>
   * Removes the last element from the list at {@code srcKey}, appends it to {@code dstKey} and returns
   * its value.<br>
   * <b>Blocks connection</b> until element available or {@code timeout} reached.
   * </p>
   *
   * @param sourceKey the source key to remove from
   * @param destinationKey the destination key to append to
   * @param timeout the timeout
   * @param unit the time unit for the timeout
   * @return the value of the removed element, or null if the source key doesn't exist or timeout reached
   */
  public T lBRightPopAndLeftPush(String sourceKey, String destinationKey,
      long timeout, TimeUnit unit) {
    return redisTemplate.opsForList().rightPopAndLeftPush(sourceKey,
        destinationKey, timeout, unit);
  }

  /**
   * <p>
   * Removes the first {@code count} occurrences of {@code value} from the list stored at
   * {@code key}.
   * </p>
   *
   * @param key the key to remove from
   * @param index the index to remove from
   * @param value the value to remove
   * @return the number of elements removed, or null if the key doesn't exist
   */
  public Long lRemove(String key, long index, String value) {
    return redisTemplate.opsForList().remove(key, index, value);
  }

  /**
   * <p>
   * Trims the list at the specified key to elements between the specified start and end.
   * </p>
   *
   * @param key the key to trim
   * @param start the start index (inclusive)
   * @param end the end index (exclusive)
   */
  public void lTrim(String key, long start, long end) {
    redisTemplate.opsForList().trim(key, start, end);
  }

  /**
   * <p>
   * Gets the size of the list stored at the specified key.
   * </p>
   *
   * @param key the key to get the size for
   * @return the size of the list, or null if the key doesn't exist
   */
  public Long lLen(String key) {
    return redisTemplate.opsForList().size(key);
  }

  /* --------------------Set Operations -------------------------- */

  /**
   * <p>
   * Removes the specified values from the set at the specified key and returns the number of removed
   * elements.
   * </p>
   *
   * @param key the key to remove from
   * @param values the collection of values to remove
   * @return the number of elements removed, or null if the key doesn't exist
   */
  public Long sRemove(String key, Collection<T> values) {
    return redisTemplate.opsForSet().remove(key, values);
  }

  /**
   * <p>
   * Removes and returns a random member from the set at the specified key.
   * </p>
   *
   * @param key the key to remove from
   * @return a random member, or null if the key doesn't exist
   */
  public T sPop(String key) {
    return redisTemplate.opsForSet().pop(key);
  }

  /**
   * <p>
   * Moves the specified value from the specified key to the specified destination key.
   * </p>
   *
   * @param key the key to move from
   * @param value the value to move
   * @param destKey the destination key to move to
   * @return true if the value was moved, false otherwise
   */
  public Boolean sMove(String key, T value, String destKey) {
    return redisTemplate.opsForSet().move(key, value, destKey);
  }

  /**
   * <p>
   * Gets the size of the set at the specified key.
   * </p>
   *
   * @param key the key to get the size for
   * @return the size of the set, or null if the key doesn't exist
   */
  public Long sSize(String key) {
    return redisTemplate.opsForSet().size(key);
  }

  /**
   * <p>
   * Checks if the set at the specified key contains the specified value.
   * </p>
   *
   * @param key the key to check
   * @param value the value to check
   * @return true if the set contains the value, false otherwise
   */
  public Boolean sIsMember(String key, T value) {
    return redisTemplate.opsForSet().isMember(key, value);
  }

  /**
   * <p>
   * Returns the members intersecting all given sets at the specified key and {@code otherKey}.
   * </p>
   *
   * @param key the key to intersect
   * @param otherKey the other key to intersect
   * @return a set of intersecting members
   */
  public Set<T> sIntersect(String key, String otherKey) {
    return redisTemplate.opsForSet().intersect(key, otherKey);
  }

  /**
   * <p>
   * Returns the members intersecting all given sets at the specified key and {@code otherKeys}.
   * </p>
   *
   * @param key the key to intersect
   * @param otherKeys the collection of other keys to intersect
   * @return a set of intersecting members
   */
  public Set<T> sIntersect(String key, Collection<String> otherKeys) {
    return redisTemplate.opsForSet().intersect(key, otherKeys);
  }

  /**
   * <p>
   * Intersects all given sets at the specified key and {@code otherKey} and stores the result in
   * {@code destKey}.
   * </p>
   *
   * @param key the key to intersect
   * @param otherKey the other key to intersect
   * @param destKey the destination key to store the result
   * @return the number of elements in the resulting set
   */
  public Long sIntersectAndStore(String key, String otherKey, String destKey) {
    return redisTemplate.opsForSet().intersectAndStore(key, otherKey,
        destKey);
  }

  /**
   * <p>
   * Intersects all given sets at the specified key and {@code otherKeys} and stores the result in
   * {@code destKey}.
   * </p>
   *
   * @param key the key to intersect
   * @param otherKeys the collection of other keys to intersect
   * @param destKey the destination key to store the result
   * @return the number of elements in the resulting set
   */
  public Long sIntersectAndStore(String key, Collection<String> otherKeys,
      String destKey) {
    return redisTemplate.opsForSet().intersectAndStore(key, otherKeys,
        destKey);
  }

  /**
   * <p>
   * Unions all sets at the specified {@code keys} and {@code otherKey}.
   * </p>
   *
   * @param key the key to union
   * @param otherKeys the other key to union
   * @return a set of unioned members
   */
  public Set<T> sUnion(String key, String otherKeys) {
    return redisTemplate.opsForSet().union(key, otherKeys);
  }

  /**
   * <p>
   * Unions all sets at the specified {@code keys} and {@code otherKeys}.
   * </p>
   *
   * @param key the key to union
   * @param otherKeys the collection of other keys to union
   * @return a set of unioned members
   */
  public Set<T> sUnion(String key, Collection<String> otherKeys) {
    return redisTemplate.opsForSet().union(key, otherKeys);
  }

  /**
   * <p>
   * Unions all sets at the specified {@code key} and {@code otherKey} and stores the result in
   * {@code destKey}.
   * </p>
   *
   * @param key the key to union
   * @param otherKey the other key to union
   * @param destKey the destination key to store the result
   * @return the number of elements in the resulting set
   */
  public Long sUnionAndStore(String key, String otherKey, String destKey) {
    return redisTemplate.opsForSet().unionAndStore(key, otherKey, destKey);
  }

  /**
   * <p>
   * Unions all sets at the specified {@code key} and {@code otherKeys} and stores the result in
   * {@code destKey}.
   * </p>
   *
   * @param key the key to union
   * @param otherKeys the collection of other keys to union
   * @param destKey the destination key to store the result
   * @return the number of elements in the resulting set
   */
  public Long sUnionAndStore(String key, Collection<String> otherKeys,
      String destKey) {
    return redisTemplate.opsForSet().unionAndStore(key, otherKeys, destKey);
  }

  /**
   * <p>
   * Diffs all sets for the specified {@code key} and {@code otherKey}.
   * </p>
   *
   * @param key the key to diff
   * @param otherKey the other key to diff
   * @return a set of diffed members
   */
  public Set<T> sDifference(String key, String otherKey) {
    return redisTemplate.opsForSet().difference(key, otherKey);
  }

  /**
   * <p>
   * Diffs all sets for the specified {@code key} and {@code otherKeys}.
   * </p>
   *
   * @param key the key to diff
   * @param otherKeys the collection of other keys to diff
   * @return a set of diffed members
   */
  public Set<T> sDifference(String key, Collection<String> otherKeys) {
    return redisTemplate.opsForSet().difference(key, otherKeys);
  }

  /**
   * <p>
   * Diffs all sets for the specified {@code key} and {@code otherKey} and stores the result in
   * {@code destKey}.
   * </p>
   *
   * @param key the key to diff
   * @param otherKey the other key to diff
   * @param destKey the destination key to store the result
   * @return the number of elements in the resulting set
   */
  public Long sDifference(String key, String otherKey, String destKey) {
    return redisTemplate.opsForSet().differenceAndStore(key, otherKey,
        destKey);
  }

  /**
   * <p>
   * Diffs all sets for the specified {@code key} and {@code otherKeys} and stores the result in
   * {@code destKey}.
   * </p>
   *
   * @param key the key to diff
   * @param otherKeys the collection of other keys to diff
   * @param destKey the destination key to store the result
   * @return the number of elements in the resulting set
   */
  public Long sDifference(String key, Collection<String> otherKeys,
      String destKey) {
    return redisTemplate.opsForSet().differenceAndStore(key, otherKeys,
        destKey);
  }

  /**
   * <p>
   * Gets all elements of the set at the specified key.
   * </p>
   *
   * @param key the key to get members for
   * @return a set of all members
   */
  public Set<T> setMembers(String key) {
    return redisTemplate.opsForSet().members(key);
  }

  /**
   * <p>
   * Gets a random element from the set at the specified key.
   * </p>
   *
   * @param key the key to get a random member from
   * @return a random member, or null if the key doesn't exist
   */
  public T sRandomMember(String key) {
    return redisTemplate.opsForSet().randomMember(key);
  }

  /**
   * <p>
   * Gets {@code count} random elements from the set at the specified key.
   * </p>
   *
   * @param key the key to get random members from
   * @param count the number of random members to get
   * @return a list of random members
   */
  public List<T> sRandomMembers(String key, long count) {
    return redisTemplate.opsForSet().randomMembers(key, count);
  }

  /**
   * <p>
   * Gets {@code count} distinct random elements from the set at the specified key.
   * </p>
   *
   * @param key the key to get distinct random members from
   * @param count the number of distinct random members to get
   * @return a set of distinct random members
   */
  public Set<T> sDistinctRandomMembers(String key, long count) {
    return redisTemplate.opsForSet().distinctRandomMembers(key, count);
  }

  /**
   * <p>
   * Iterates over elements in the set at the specified key.
   * <strong>Important:</strong> Call {@link Cursor#close()} when done to avoid resource leak.
   * </p>
   *
   * @param key the key to iterate over
   * @param options the scan options
   * @return a cursor for iterating over set members
   */
  public Cursor<T> sScan(String key, ScanOptions options) {
    return redisTemplate.opsForSet().scan(key, options);
  }

  /*------------------zSet Operations --------------------------------*/

  /**
   * <p>
   * Adds the specified value to a sorted set at the specified key, or updates its score if it already
   * exists.
   * </p>
   *
   * @param key the key to add to
   * @param value the value to add
   * @param score the score to set
   * @return true if the value was added or updated, false otherwise
   */
  public Boolean zAdd(String key, T value, double score) {
    return redisTemplate.opsForZSet().add(key, value, score);
  }

  /**
   * <p>
   * Adds multiple values to a sorted set at the specified key, or updates their scores if they already
   * exist.
   * </p>
   *
   * @param key the key to add to
   * @param values the collection of typed tuples to add
   * @return the number of elements added
   */
  public Long zAdd(String key, Set<TypedTuple<T>> values) {
    return redisTemplate.opsForZSet().add(key, values);
  }

  /**
   * <p>
   * Removes the specified values from the sorted set. Returns the number of removed elements.
   * </p>
   *
   * @param key the key to remove from
   * @param values the collection of values to remove
   * @return the number of elements removed, or null if the key doesn't exist
   */
  public Long zRemove(String key, Object... values) {
    return redisTemplate.opsForZSet().remove(key, values);
  }

  /**
   * <p>
   * Increments the score of the element with the specified value in the sorted set by the given delta.
   * </p>
   *
   * @param key the key to increment
   * @param value the value to increment
   * @param delta the amount to increment by
   * @return the new score after incrementing, or null if the key or value doesn't exist
   */
  public Double zIncrementScore(String key, T value, double delta) {
    return redisTemplate.opsForZSet().incrementScore(key, value, delta);
  }

  /**
   * <p>
   * Determines the index of the element with the specified value in the sorted set.
   * </p>
   *
   * @param key the key to get the rank for
   * @param value the value to get the rank for
   * @return the rank of the value, or null if the key or value doesn't exist
   */
  public Long zRank(String key, T value) {
    return redisTemplate.opsForZSet().rank(key, value);
  }

  /**
   * <p>
   * Determines the index of the element with the specified value in the sorted set when scored high to
   * low.
   * </p>
   *
   * @param key the key to get the reverse rank for
   * @param value the value to get the reverse rank for
   * @return the reverse rank of the value, or null if the key or value doesn't exist
   */
  public Long zReverseRank(String key, T value) {
    return redisTemplate.opsForZSet().reverseRank(key, value);
  }

  /**
   * <p>
   * Gets elements between the specified start and end from the sorted set.
   * </p>
   *
   * @param key the key to get the range for
   * @param start the start index (inclusive)
   * @param end the end index (exclusive)
   * @return a set of elements
   */
  public Set<T> zRange(String key, long start, long end) {
    return redisTemplate.opsForZSet().range(key, start, end);
  }

  /**
   * <p>
   * Gets a set of {@link TypedTuple}s between the specified start and end from the sorted set.
   * </p>
   *
   * @param key the key to get the range with scores for
   * @param start the start index (inclusive)
   * @param end the end index (exclusive)
   * @return a set of typed tuples
   */
  public Set<TypedTuple<T>> zRangeWithScores(String key, long start,
      long end) {
    return redisTemplate.opsForZSet().rangeWithScores(key, start, end);
  }

  /**
   * <p>
   * Gets elements where the score is between the specified min and max from the sorted set.
   * </p>
   *
   * @param key the key to get the range by score for
   * @param min the minimum score
   * @param max the maximum score
   * @return a set of elements
   */
  public Set<T> zRangeByScore(String key, double min, double max) {
    return redisTemplate.opsForZSet().rangeByScore(key, min, max);
  }

  /**
   * <p>
   * Gets a set of {@link TypedTuple}s where the score is between the specified min and max from the
   * sorted set.
   * </p>
   *
   * @param key the key to get the range by score with scores for
   * @param min the minimum score
   * @param max the maximum score
   * @return a set of typed tuples
   */
  public Set<TypedTuple<T>> zRangeByScoreWithScores(String key,
      double min, double max) {
    return redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max);
  }

  /**
   * <p>
   * Gets a set of {@link TypedTuple}s in range from {@code start} to {@code end} where score is
   * between {@code min} and {@code max} from sorted set.
   * </p>
   *
   * @param key the key to get the range by score with scores for
   * @param min the minimum score
   * @param max the maximum score
   * @param start the start index (inclusive)
   * @param end the end index (exclusive)
   * @return a set of typed tuples
   */
  public Set<TypedTuple<T>> zRangeByScoreWithScores(String key,
      double min, double max, long start, long end) {
    return redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max,
        start, end);
  }

  /**
   * <p>
   * Gets elements in range from {@code start} to {@code end} from sorted set ordered from high to
   * low.
   * </p>
   *
   * @param key the key to get the reverse range for
   * @param start the start index (inclusive)
   * @param end the end index (exclusive)
   * @return a set of elements
   */
  public Set<T> zReverseRange(String key, long start, long end) {
    return redisTemplate.opsForZSet().reverseRange(key, start, end);
  }

  /**
   * <p>
   * Gets a set of {@link TypedTuple}s in range from {@code start} to {@code end} from sorted set
   * ordered from high to low.
   * </p>
   *
   * @param key the key to get the reverse range with scores for
   * @param start the start index (inclusive)
   * @param end the end index (exclusive)
   * @return a set of typed tuples
   */
  public Set<TypedTuple<T>> zReverseRangeWithScores(String key,
      long start, long end) {
    return redisTemplate.opsForZSet().reverseRangeWithScores(key, start,
        end);
  }

  /**
   * <p>
   * Gets elements where score is between the specified min and max from sorted set ordered from
   * high to low.
   * </p>
   *
   * @param key the key to get the reverse range by score for
   * @param min the minimum score
   * @param max the maximum score
   * @return a set of elements
   */
  public Set<T> zReverseRangeByScore(String key, double min,
      double max) {
    return redisTemplate.opsForZSet().reverseRangeByScore(key, min, max);
  }

  /**
   * <p>
   * Gets a set of {@link TypedTuple} where score is between the specified min and max from sorted
   * set ordered from high to low.
   * </p>
   *
   * @param key the key to get the reverse range by score with scores for
   * @param min the minimum score
   * @param max the maximum score
   * @return a set of typed tuples
   */
  public Set<TypedTuple<T>> zReverseRangeByScoreWithScores(
      String key, double min, double max) {
    return redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key,
        min, max);
  }

  /**
   * <p>
   * Gets elements in range from {@code start} to {@code end} where score is between {@code min} and
   * {@code max} from sorted set ordered high -> low.
   * </p>
   *
   * @param key the key to get the reverse range by score for
   * @param min the minimum score
   * @param max the maximum score
   * @param start the start index (inclusive)
   * @param end the end index (exclusive)
   * @return a set of elements
   */
  public Set<T> zReverseRangeByScore(String key, double min,
      double max, long start, long end) {
    return redisTemplate.opsForZSet().reverseRangeByScore(key, min, max,
        start, end);
  }

  /**
   * <p>
   * Counts the number of elements within the sorted set with scores between the specified min and max.
   * </p>
   *
   * @param key the key to count in
   * @param min the minimum score
   * @param max the maximum score
   * @return the number of elements within the range
   */
  public Long zCount(String key, double min, double max) {
    return redisTemplate.opsForZSet().count(key, min, max);
  }

  /**
   * <p>
   * Returns the number of elements of the sorted set stored with the specified key.
   * </p>
   *
   * @param key the key to get the size for
   * @return the number of elements in the sorted set, or null if the key doesn't exist
   */
  public Long zSize(String key) {
    return redisTemplate.opsForZSet().size(key);
  }

  /**
   * <p>
   * Gets the size of the sorted set with the specified key.
   * </p>
   *
   * @param key the key to get the zCard for
   * @return the size of the sorted set, or null if the key doesn't exist
   */
  public Long zZCard(String key) {
    return redisTemplate.opsForZSet().zCard(key);
  }

  /**
   * <p>
   * Gets the score of the element with the specified value from the sorted set with the specified key.
   * </p>
   *
   * @param key the key to get the score for
   * @param value the value to get the score for
   * @return the score of the value, or null if the key or value doesn't exist
   */
  public Double zScore(String key, T value) {
    return redisTemplate.opsForZSet().score(key, value);
  }

  /**
   * <p>
   * Removes elements in range between the specified start and end from the sorted set with
   * the specified key.
   * </p>
   *
   * @param key the key to remove from
   * @param start the start index (inclusive)
   * @param end the end index (exclusive)
   * @return the number of elements removed, or null if the key doesn't exist
   */
  public Long zRemoveRange(String key, long start, long end) {
    return redisTemplate.opsForZSet().removeRange(key, start, end);
  }

  /**
   * <p>
   * Removes elements with scores between the specified min and max from the sorted set with
   * the specified key.
   * </p>
   *
   * @param key the key to remove from
   * @param min the minimum score
   * @param max the maximum score
   * @return the number of elements removed, or null if the key doesn't exist
   */
  public Long zRemoveRangeByScore(String key, double min, double max) {
    return redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
  }

  /**
   * <p>
   * Unions sorted sets at the specified {@code key} and {@code otherKeys} and stores the result in
   * the destination {@code destKey}.
   * </p>
   *
   * @param key the key to union
   * @param otherKey the other key to union
   * @param destKey the destination key to store the result
   * @return the number of elements in the resulting set
   */
  public Long zUnionAndStore(String key, String otherKey, String destKey) {
    return redisTemplate.opsForZSet().unionAndStore(key, otherKey, destKey);
  }

  /**
   * <p>
   * Unions sorted sets at the specified {@code key} and {@code otherKeys} and stores the result in
   * the destination {@code destKey}.
   * </p>
   *
   * @param key the key to union
   * @param otherKeys the collection of other keys to union
   * @param destKey the destination key to store the result
   * @return the number of elements in the resulting set
   */
  public Long zUnionAndStore(String key, Collection<String> otherKeys,
      String destKey) {
    return redisTemplate.opsForZSet().unionAndStore(key, otherKeys, destKey);
  }

  /**
   * <p>
   * Intersects sorted sets at the specified {@code key} and {@code otherKey} and stores the result in
   * the destination {@code destKey}.
   * </p>
   *
   * @param key the key to intersect
   * @param otherKey the other key to intersect
   * @param destKey the destination key to store the result
   * @return the number of elements in the resulting set
   */
  public Long zIntersectAndStore(String key, String otherKey,
      String destKey) {
    return redisTemplate.opsForZSet().intersectAndStore(key, otherKey,
        destKey);
  }

  /**
   * <p>
   * Intersects sorted sets at the specified {@code key} and {@code otherKeys} and stores the result in
   * the destination {@code destKey}.
   * </p>
   *
   * @param key the key to intersect
   * @param otherKeys the collection of other keys to intersect
   * @param destKey the destination key to store the result
   * @return the number of elements in the resulting set
   */
  public Long zIntersectAndStore(String key, Collection<String> otherKeys,
      String destKey) {
    return redisTemplate.opsForZSet().intersectAndStore(key, otherKeys,
        destKey);
  }

  /**
   * <p>
   * Iterates over elements in the zset at the specified key.
   * </p>
   *
   * @param key the key to iterate over
   * @param options the scan options
   * @return a cursor for iterating over zset elements
   */
  public Cursor<TypedTuple<T>> zScan(String key, ScanOptions options) {
    return redisTemplate.opsForZSet().scan(key, options);
  }

}
