
package cloud.xcan.sdf.lettucex.util;

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

public class RedisService<T> {

  private RedisTemplate<String, T> redisTemplate;

  public RedisService() {
  }

  public RedisService(RedisTemplate<String, T> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  public RedisTemplate<String, T> getRedisTemplate() {
    return this.redisTemplate;
  }

  public void setRedisTemplate(RedisTemplate<String, T> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  /*----------------------Key-----------------------*/

  public void delete(String key) {
    redisTemplate.delete(key);
  }

  public void delete(Collection<String> keys) {
    redisTemplate.delete(keys);
  }

  public byte[] dump(String key) {
    return redisTemplate.dump(key);
  }

  public Boolean hasKey(String key) {
    return redisTemplate.hasKey(key);
  }

  public Boolean expire(String key, long timeout, TimeUnit unit) {
    return redisTemplate.expire(key, timeout, unit);
  }

  public Boolean expireAt(String key, Date date) {
    return redisTemplate.expireAt(key, date);
  }

  public Set<String> keys(String pattern) {
    return redisTemplate.keys(pattern);
  }

  public Boolean move(String key, int dbIndex) {
    return redisTemplate.move(key, dbIndex);
  }

  public Boolean persist(String key) {
    return redisTemplate.persist(key);
  }

  public Long getExpire(String key, TimeUnit unit) {
    return redisTemplate.getExpire(key, unit);
  }

  public Long getExpire(String key) {
    return redisTemplate.getExpire(key);
  }

  public String randomKey() {
    return redisTemplate.randomKey();
  }

  public void rename(String oldKey, String newKey) {
    redisTemplate.rename(oldKey, newKey);
  }

  public Boolean renameIfAbsent(String oldKey, String newKey) {
    return redisTemplate.renameIfAbsent(oldKey, newKey);
  }

  /**
   * Return value type
   */
  public DataType type(String key) {
    return redisTemplate.type(key);
  }

  /* -------------------String --------------------- */

  /**
   * Set {@code value} for {@code key}.
   */
  public void set(String key, T value) {
    redisTemplate.opsForValue().set(key, value);
  }

  /**
   * Set the {@code value} and expiration {@code timeout} for {@code key}.
   */
  public void set(String key, T value, long time, TimeUnit unit) {
    redisTemplate.opsForValue().set(key, value, time, unit);
  }

  /**
   * Get the value of {@code key}.
   */
  public T get(String key) {
    return redisTemplate.opsForValue().get(key);
  }

  /**
   * Get a substring of value of {@code key} between {@code begin} and {@code end}.
   */
  public String getRange(String key, long start, long end) {
    return redisTemplate.opsForValue().get(key, start, end);
  }

  /**
   * Set {@code value} of {@code key} and return its old value.
   */
  public T getAndSet(String key, T value) {
    return redisTemplate.opsForValue().getAndSet(key, value);
  }

  /**
   * Get the bit value at {@code offset} of value at {@code key}.
   */
  public Boolean getBit(String key, long offset) {
    return redisTemplate.opsForValue().getBit(key, offset);
  }

  /**
   * Get multiple {@code keys}. Values are returned in the order of the requested keys.
   */
  public List<T> multiGet(Collection<String> keys) {
    return redisTemplate.opsForValue().multiGet(keys);
  }

  /**
   * Sets the bit at {@code offset} in value stored at {@code key}.
   */
  public Boolean setBit(String key, long offset, boolean value) {
    return redisTemplate.opsForValue().setBit(key, offset, value);
  }

  /**
   * Set the {@code value} and expiration {@code timeout} for {@code key}.
   */
  public void setEx(String key, T value, long timeout, TimeUnit unit) {
    redisTemplate.opsForValue().set(key, value, timeout, unit);
  }

  /**
   * Set {@code key} to hold the string {@code value} if {@code key} is absent.
   */
  public Boolean setIfAbsent(String key, T value) {
    return redisTemplate.opsForValue().setIfAbsent(key, value);
  }

  /**
   * Set the {@code value} and expiration {@code timeout} for {@code key} if {@code key} is absent.
   */
  public Boolean setIfAbsent(String key, T value, long timeout, TimeUnit unit) {
    return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
  }

  /**
   * Overwrite parts of {@code key} starting at the specified {@code offset} with given {@code
   * value}.
   */
  public void setRange(String key, T value, long offset) {
    redisTemplate.opsForValue().set(key, value, offset);
  }

  /**
   * Get the length of the value stored at {@code key}.
   */
  public Long size(String key) {
    return redisTemplate.opsForValue().size(key);
  }

  /**
   * Set multiple keys to multiple values using key-value pairs provided in {@code tuple}.
   */
  public void multiSet(Map<String, T> maps) {
    redisTemplate.opsForValue().multiSet(maps);
  }

  /**
   * Set multiple keys to multiple values using key-value pairs provided in {@code tuple} only if
   * the provided key does not exist.
   */
  public Boolean multiSetIfAbsent(Map<String, T> maps) {
    return redisTemplate.opsForValue().multiSetIfAbsent(maps);
  }

  /**
   * Increment an integer value stored as string value under {@code key} by {@code delta}.
   */
  public Long incrBy(String key, long increment) {
    return redisTemplate.opsForValue().increment(key, increment);
  }

  /**
   * Increment a floating point number value stored as string value under {@code key} by {@code
   * delta}.
   */
  public Double incrByFloat(String key, double increment) {
    return redisTemplate.opsForValue().increment(key, increment);
  }

  /**
   * Append a {@code value} to {@code key}.
   */
  public Integer append(String key, String value) {
    return redisTemplate.opsForValue().append(key, value);
  }

  /* -------------------Hash------------------------- */

  /**
   * Get value for given {@code hashKey} from hash at {@code key}.
   */
  public Object hGet(String key, String field) {
    return redisTemplate.opsForHash().get(key, field);
  }

  /**
   * Get entire hash stored at {@code key}.
   */
  public Map<Object, Object> hGetAll(String key) {
    return redisTemplate.opsForHash().entries(key);
  }

  /**
   * Get values for given {@code hashKeys} from hash at {@code key}.
   */
  public List<Object> hMultiGet(String key, Collection<Object> fields) {
    return redisTemplate.opsForHash().multiGet(key, fields);
  }

  /**
   * Set the {@code value} of a hash {@code hashKey}.
   */
  public void hPut(String key, String hashKey, String value) {
    redisTemplate.opsForHash().put(key, hashKey, value);
  }

  /**
   * Set multiple hash fields to multiple values using data provided in {@code m}.
   */
  public void hPutAll(String key, Map<String, String> maps) {
    redisTemplate.opsForHash().putAll(key, maps);
  }

  /**
   * Set the {@code value} of a hash {@code hashKey} only if {@code hashKey} does not exist.
   */
  public Boolean hPutIfAbsent(String key, String hashKey, String value) {
    return redisTemplate.opsForHash().putIfAbsent(key, hashKey, value);
  }

  /**
   * Delete given hash {@code hashKeys}.
   */
  public Long hDelete(String key, Object... hashKeys) {
    return redisTemplate.opsForHash().delete(key, hashKeys);
  }

  /**
   * Determine if given hash {@code hashKey} exists.
   */
  public boolean hExists(String key, String hashKey) {
    return redisTemplate.opsForHash().hasKey(key, hashKey);
  }

  /**
   * Increment {@code value} of a hash {@code hashKey} by the given {@code delta}.
   */
  public Long hIncrBy(String key, T hashKey, long increment) {
    return redisTemplate.opsForHash().increment(key, hashKey, increment);
  }

  /**
   * Increment {@code value} of a hash {@code hashKey} by the given {@code delta}.
   */
  public Double hIncrByFloat(String key, T hashKey, double delta) {
    return redisTemplate.opsForHash().increment(key, hashKey, delta);
  }

  /**
   * Get key set (fields) of hash at {@code key}.
   */
  public Set<Object> hKeys(String key) {
    return redisTemplate.opsForHash().keys(key);
  }

  /**
   * Get size of hash at {@code key}.
   */
  public Long hSize(String key) {
    return redisTemplate.opsForHash().size(key);
  }

  /**
   * Get entry set (values) of hash at {@code key}.
   */
  public List<Object> hValues(String key) {
    return redisTemplate.opsForHash().values(key);
  }

  /**
   * Use a {@link Cursor} to iterate over entries in hash at {@code key}. <br />
   * <strong>Important:</strong> Call {@link Cursor#close()} when done to avoid resource leak.
   */
  public Cursor<Entry<Object, Object>> hScan(String key, ScanOptions options) {
    return redisTemplate.opsForHash().scan(key, options);
  }

  /* ------------------------List ---------------------------- */

  /**
   * Get element at {@code index} form list at {@code key}.
   */
  public T lIndex(String key, long index) {
    return redisTemplate.opsForList().index(key, index);
  }

  /**
   * Get elements between {@code begin} and {@code end} from list at {@code key}.
   */
  public List<T> lRange(String key, long start, long end) {
    return redisTemplate.opsForList().range(key, start, end);
  }

  /**
   * Prepend {@code value} to {@code key}.
   */
  public Long lLeftPush(String key, T value) {
    return redisTemplate.opsForList().leftPush(key, value);
  }

  /**
   * Prepend {@code values} to {@code key}.
   */
  public Long lLeftPushAll(String key, Collection<T> value) {
    return redisTemplate.opsForList().leftPushAll(key, value);
  }

  /**
   * Prepend {@code values} to {@code key} only if the list exists.
   */
  public Long lLeftPushIfPresent(String key, T value) {
    return redisTemplate.opsForList().leftPushIfPresent(key, value);
  }

  /**
   * Prepend {@code values} to {@code key} before {@code value}.
   */
  public Long lLeftPush(String key, T pivot, T value) {
    return redisTemplate.opsForList().leftPush(key, pivot, value);
  }

  /**
   * Append {@code value} to {@code key}.
   */
  public Long lRightPush(String key, T value) {
    return redisTemplate.opsForList().rightPush(key, value);
  }

  /**
   * Append {@code values} to {@code key}.
   */
  public Long lRightPushAll(String key, Collection<T> value) {
    return redisTemplate.opsForList().rightPushAll(key, value);
  }

  /**
   * Append {@code values} to {@code key} only if the list exists.
   */
  public Long lRightPushIfPresent(String key, T value) {
    return redisTemplate.opsForList().rightPushIfPresent(key, value);
  }

  /**
   * Append {@code values} to {@code key} before {@code value}.
   */
  public Long lRightPush(String key, T pivot, T value) {
    return redisTemplate.opsForList().rightPush(key, pivot, value);
  }

  /**
   * Set the {@code value} list element at {@code index}.
   */
  public void lSet(String key, long index, T value) {
    redisTemplate.opsForList().set(key, index, value);
  }

  /**
   * Removes and returns first element in list stored at {@code key}.
   */
  public T lLeftPop(String key) {
    return redisTemplate.opsForList().leftPop(key);
  }

  /**
   * Removes and returns first element from lists stored at {@code key} . <br>
   * <b>Blocks connection</b> until element available or {@code timeout} reached.
   */
  public T lBLeftPop(String key, long timeout, TimeUnit unit) {
    return redisTemplate.opsForList().leftPop(key, timeout, unit);
  }

  /**
   * Removes and returns last element in list stored at {@code key}.
   */
  public T lRightPop(String key) {
    return redisTemplate.opsForList().rightPop(key);
  }

  /**
   * Removes and returns last element from lists stored at {@code key}. <br>
   * <b>Blocks connection</b> until element available or {@code timeout} reached.
   */
  public T lBRightPop(String key, long timeout, TimeUnit unit) {
    return redisTemplate.opsForList().rightPop(key, timeout, unit);
  }

  /**
   * Remove the last element from list at {@code sourceKey}, append it to {@code destinationKey} and
   * return its value.
   */
  public T lRightPopAndLeftPush(String sourceKey, String destinationKey) {
    return redisTemplate.opsForList().rightPopAndLeftPush(sourceKey,
        destinationKey);
  }

  /**
   * Remove the last element from list at {@code srcKey}, append it to {@code dstKey} and return its
   * value.<br>
   * <b>Blocks connection</b> until element available or {@code timeout} reached.
   */
  public T lBRightPopAndLeftPush(String sourceKey, String destinationKey,
      long timeout, TimeUnit unit) {
    return redisTemplate.opsForList().rightPopAndLeftPush(sourceKey,
        destinationKey, timeout, unit);
  }

  /**
   * Removes the first {@code count} occurrences of {@code value} from the list stored at {@code
   * key}.
   */
  public Long lRemove(String key, long index, String value) {
    return redisTemplate.opsForList().remove(key, index, value);
  }

  /**
   * Trim list at {@code key} to elements between {@code start} and {@code end}.
   */
  public void lTrim(String key, long start, long end) {
    redisTemplate.opsForList().trim(key, start, end);
  }

  /**
   * Get the size of list stored at {@code key}.
   */
  public Long lLen(String key) {
    return redisTemplate.opsForList().size(key);
  }

  /* --------------------Set -------------------------- */

  /**
   * Remove given {@code values} from set at {@code key} and return the number of removed elements.
   */
  public Long sRemove(String key, Collection<T> values) {
    return redisTemplate.opsForSet().remove(key, values);
  }

  /**
   * Remove and return a random member from set at {@code key}.
   */
  public T sPop(String key) {
    return redisTemplate.opsForSet().pop(key);
  }

  /**
   * Move {@code value} from {@code key} to {@code destKey}
   */
  public Boolean sMove(String key, T value, String destKey) {
    return redisTemplate.opsForSet().move(key, value, destKey);
  }

  /**
   * Get size of set at {@code key}.
   */
  public Long sSize(String key) {
    return redisTemplate.opsForSet().size(key);
  }

  /**
   * Check if set at {@code key} contains {@code value}.
   */
  public Boolean sIsMember(String key, T value) {
    return redisTemplate.opsForSet().isMember(key, value);
  }

  /**
   * Returns the members intersecting all given sets at {@code key} and {@code otherKey}.
   */
  public Set<T> sIntersect(String key, String otherKey) {
    return redisTemplate.opsForSet().intersect(key, otherKey);
  }

  /**
   * Returns the members intersecting all given sets at {@code key} and {@code otherKeys}.
   */
  public Set<T> sIntersect(String key, Collection<String> otherKeys) {
    return redisTemplate.opsForSet().intersect(key, otherKeys);
  }

  /**
   * Intersect all given sets at {@code key} and {@code otherKey} and store result in {@code
   * destKey}.
   */
  public Long sIntersectAndStore(String key, String otherKey, String destKey) {
    return redisTemplate.opsForSet().intersectAndStore(key, otherKey,
        destKey);
  }

  /**
   * Intersect all given sets at {@code key} and {@code otherKeys} and store result in {@code
   * destKey}.
   */
  public Long sIntersectAndStore(String key, Collection<String> otherKeys,
      String destKey) {
    return redisTemplate.opsForSet().intersectAndStore(key, otherKeys,
        destKey);
  }

  /**
   * Union all sets at given {@code keys} and {@code otherKey}.
   */
  public Set<T> sUnion(String key, String otherKeys) {
    return redisTemplate.opsForSet().union(key, otherKeys);
  }

  /**
   * Union all sets at given {@code keys} and {@code otherKeys}.
   */
  public Set<T> sUnion(String key, Collection<String> otherKeys) {
    return redisTemplate.opsForSet().union(key, otherKeys);
  }

  /**
   * Union all sets at given {@code key} and {@code otherKey} and store result in {@code destKey}.
   */
  public Long sUnionAndStore(String key, String otherKey, String destKey) {
    return redisTemplate.opsForSet().unionAndStore(key, otherKey, destKey);
  }

  /**
   * Union all sets at given {@code key} and {@code otherKeys} and store result in {@code destKey}.
   */
  public Long sUnionAndStore(String key, Collection<String> otherKeys,
      String destKey) {
    return redisTemplate.opsForSet().unionAndStore(key, otherKeys, destKey);
  }

  /**
   * Diff all sets for given {@code key} and {@code otherKey}.
   */
  public Set<T> sDifference(String key, String otherKey) {
    return redisTemplate.opsForSet().difference(key, otherKey);
  }

  /**
   * Diff all sets for given {@code key} and {@code otherKeys}.
   */
  public Set<T> sDifference(String key, Collection<String> otherKeys) {
    return redisTemplate.opsForSet().difference(key, otherKeys);
  }

  /**
   * Diff all sets for given {@code key} and {@code otherKey} and store result in {@code destKey}.
   */
  public Long sDifference(String key, String otherKey, String destKey) {
    return redisTemplate.opsForSet().differenceAndStore(key, otherKey,
        destKey);
  }

  /**
   * Diff all sets for given {@code key} and {@code otherKeys} and store result in {@code destKey}.
   */
  public Long sDifference(String key, Collection<String> otherKeys,
      String destKey) {
    return redisTemplate.opsForSet().differenceAndStore(key, otherKeys,
        destKey);
  }

  /**
   * Get all elements of set at {@code key}.
   */
  public Set<T> setMembers(String key) {
    return redisTemplate.opsForSet().members(key);
  }

  /**
   * Get random element from set at {@code key}.
   */
  public T sRandomMember(String key) {
    return redisTemplate.opsForSet().randomMember(key);
  }

  /**
   * Get {@code count} random elements from set at {@code key}.
   */
  public List<T> sRandomMembers(String key, long count) {
    return redisTemplate.opsForSet().randomMembers(key, count);
  }

  /**
   * Get {@code count} distinct random elements from set at {@code key}.
   */
  public Set<T> sDistinctRandomMembers(String key, long count) {
    return redisTemplate.opsForSet().distinctRandomMembers(key, count);
  }

  /**
   * Iterate over elements in set at {@code key}. <br />
   * <strong>Important:</strong> Call {@link Cursor#close()} when done to avoid resource leak.
   */
  public Cursor<T> sScan(String key, ScanOptions options) {
    return redisTemplate.opsForSet().scan(key, options);
  }

  /*------------------zSet --------------------------------*/

  /**
   * Add {@code value} to a sorted set at {@code key}, or update its {@code score} if it already
   * exists.
   */
  public Boolean zAdd(String key, T value, double score) {
    return redisTemplate.opsForZSet().add(key, value, score);
  }

  /**
   * Add {@code tuples} to a sorted set at {@code key}, or update its {@code score} if it already
   * exists.
   */
  public Long zAdd(String key, Set<TypedTuple<T>> values) {
    return redisTemplate.opsForZSet().add(key, values);
  }

  /**
   * Remove {@code values} from sorted set. Return number of removed elements.
   */
  public Long zRemove(String key, Object... values) {
    return redisTemplate.opsForZSet().remove(key, values);
  }

  /**
   * Increment the score of element with {@code value} in sorted set by {@code incr}.
   */
  public Double zIncrementScore(String key, T value, double delta) {
    return redisTemplate.opsForZSet().incrementScore(key, value, delta);
  }

  /**
   * Determine the index of element with {@code value} in a sorted set.
   */
  public Long zRank(String key, T value) {
    return redisTemplate.opsForZSet().rank(key, value);
  }

  /**
   * Determine the index of element with {@code value} in a sorted set when scored high to low.
   */
  public Long zReverseRank(String key, T value) {
    return redisTemplate.opsForZSet().reverseRank(key, value);
  }

  /**
   * Get elements between {@code start} and {@code end} from sorted set.
   */
  public Set<T> zRange(String key, long start, long end) {
    return redisTemplate.opsForZSet().range(key, start, end);
  }

  /**
   * Get set of {@link TypedTuple}s between {@code start} and {@code end} from sorted set.
   */
  public Set<TypedTuple<T>> zRangeWithScores(String key, long start,
      long end) {
    return redisTemplate.opsForZSet().rangeWithScores(key, start, end);
  }

  /**
   * Get elements where score is between {@code min} and {@code max} from sorted set.
   */
  public Set<T> zRangeByScore(String key, double min, double max) {
    return redisTemplate.opsForZSet().rangeByScore(key, min, max);
  }

  /**
   * Get set of {@link TypedTuple}s where score is between {@code min} and {@code max} from sorted set.
   */
  public Set<TypedTuple<T>> zRangeByScoreWithScores(String key,
      double min, double max) {
    return redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max);
  }

  /**
   * Get set of {@link TypedTuple}s in range from {@code start} to {@code end} where score is between
   * {@code min} and {@code max} from sorted set.
   */
  public Set<TypedTuple<T>> zRangeByScoreWithScores(String key,
      double min, double max, long start, long end) {
    return redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max,
        start, end);
  }

  /**
   * Get elements in range from {@code start} to {@code end} from sorted set ordered from high to
   * low.
   */
  public Set<T> zReverseRange(String key, long start, long end) {
    return redisTemplate.opsForZSet().reverseRange(key, start, end);
  }

  /**
   * Get set of {@link TypedTuple}s in range from {@code start} to {@code end} from sorted set ordered
   * from high to low.
   */
  public Set<TypedTuple<T>> zReverseRangeWithScores(String key,
      long start, long end) {
    return redisTemplate.opsForZSet().reverseRangeWithScores(key, start,
        end);
  }

  /**
   * Get elements where score is between {@code min} and {@code max} from sorted set ordered from
   * high to low.
   */
  public Set<T> zReverseRangeByScore(String key, double min,
      double max) {
    return redisTemplate.opsForZSet().reverseRangeByScore(key, min, max);
  }

  /**
   * Get set of {@link TypedTuple} where score is between {@code min} and {@code max} from sorted set
   * ordered from high to low.
   */
  public Set<TypedTuple<T>> zReverseRangeByScoreWithScores(
      String key, double min, double max) {
    return redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key,
        min, max);
  }

  /**
   * Get elements in range from {@code start} to {@code end} where score is between {@code min} and
   * {@code max} from sorted set ordered high -> low.
   */
  public Set<T> zReverseRangeByScore(String key, double min,
      double max, long start, long end) {
    return redisTemplate.opsForZSet().reverseRangeByScore(key, min, max,
        start, end);
  }

  /**
   * Count number of elements within sorted set with scores between {@code min} and {@code max}.
   */
  public Long zCount(String key, double min, double max) {
    return redisTemplate.opsForZSet().count(key, min, max);
  }

  /**
   * Returns the number of elements of the sorted set stored with given {@code key}.
   */
  public Long zSize(String key) {
    return redisTemplate.opsForZSet().size(key);
  }

  /**
   * Get the size of sorted set with {@code key}.
   */
  public Long zZCard(String key) {
    return redisTemplate.opsForZSet().zCard(key);
  }

  /**
   * Get the score of element with {@code value} from sorted set with key {@code key}.
   */
  public Double zScore(String key, T value) {
    return redisTemplate.opsForZSet().score(key, value);
  }

  /**
   * Remove elements in range between {@code start} and {@code end} from sorted set with {@code
   * key}.
   */
  public Long zRemoveRange(String key, long start, long end) {
    return redisTemplate.opsForZSet().removeRange(key, start, end);
  }

  /**
   * Remove elements with scores between {@code min} and {@code max} from sorted set with {@code
   * key}.
   */
  public Long zRemoveRangeByScore(String key, double min, double max) {
    return redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
  }

  /**
   * Union sorted sets at {@code key} and {@code otherKeys} and store result in destination {@code
   * destKey}.
   */
  public Long zUnionAndStore(String key, String otherKey, String destKey) {
    return redisTemplate.opsForZSet().unionAndStore(key, otherKey, destKey);
  }

  /**
   * Union sorted sets at {@code key} and {@code otherKeys} and store result in destination {@code
   * destKey}.
   */
  public Long zUnionAndStore(String key, Collection<String> otherKeys,
      String destKey) {
    return redisTemplate.opsForZSet().unionAndStore(key, otherKeys, destKey);
  }

  /**
   * Intersect sorted sets at {@code key} and {@code otherKey} and store result in destination
   * {@code destKey}.
   */
  public Long zIntersectAndStore(String key, String otherKey,
      String destKey) {
    return redisTemplate.opsForZSet().intersectAndStore(key, otherKey,
        destKey);
  }

  /**
   * Intersect sorted sets at {@code key} and {@code otherKeys} and store result in destination
   * {@code destKey}.
   */
  public Long zIntersectAndStore(String key, Collection<String> otherKeys,
      String destKey) {
    return redisTemplate.opsForZSet().intersectAndStore(key, otherKeys,
        destKey);
  }

  /**
   * Iterate over elements in zset at {@code key}. <br />
   */
  public Cursor<TypedTuple<T>> zScan(String key, ScanOptions options) {
    return redisTemplate.opsForZSet().scan(key, options);
  }

}
