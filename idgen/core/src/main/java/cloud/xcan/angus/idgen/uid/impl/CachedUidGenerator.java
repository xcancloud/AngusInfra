package cloud.xcan.angus.idgen.uid.impl;

import static cloud.xcan.angus.spec.experimental.Assert.assertNotNull;
import static cloud.xcan.angus.spec.experimental.Assert.assertTrue;

import cloud.xcan.angus.idgen.UidGenerator;
import cloud.xcan.angus.idgen.exception.IdGenerateException;
import cloud.xcan.angus.idgen.uid.BitsAllocator;
import cloud.xcan.angus.idgen.uid.buffer.BufferPaddingExecutor;
import cloud.xcan.angus.idgen.uid.buffer.RejectedPutBufferHandler;
import cloud.xcan.angus.idgen.uid.buffer.RejectedTakeBufferHandler;
import cloud.xcan.angus.idgen.uid.buffer.RingBuffer;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

/**
 * Represents a cached implementation of {@link UidGenerator} extends from
 * {@link DefaultUidGenerator}, based on a lock free {@link RingBuffer}<p>
 * <p>
 * The spring properties you can specified as below:<br>
 * <li><b>boostPower:</b> RingBuffer size boost for a power of 2, Sample: boostPower is 3, it means
 * the buffer size will be <code>({@link BitsAllocator#getMaxSequence()} + 1) &lt;&lt;
 * {@link #boostPower}</code>, Default as {@value #DEFAULT_BOOST_POWER}
 * <li><b>paddingFactor:</b> Represents a percent value of (0 - 100). When the count of rest
 * available UIDs reach the threshold, it will trigger padding buffer. Default
 * as{@link RingBuffer#DEFAULT_PADDING_PERCENT} Sample: paddingFactor=20, bufferSize=1000 ->
 * threshold=1000 * 20 /100, padding buffer will be triggered when tail-cursor<threshold
 * <li><b>scheduleInterval:</b> Padding buffer in a schedule, specify padding buffer interval,
 * ValueUnit
 * as second
 * <li><b>rejectedPutBufferHandler:</b> Policy for rejected put buffer. Default as discard put
 * request, just do logging
 * <li><b>rejectedTakeBufferHandler:</b> Policy for rejected take buffer. Default as throwing up an
 * exception
 */
public class CachedUidGenerator extends DefaultUidGenerator implements DisposableBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(CachedUidGenerator.class);
  //private static final int DEFAULT_BOOST_POWER = 3; // BufferSize(262144 << 3 = 2097152) * PaddedAtomicLong(6 Long * 8 = 48Byte) = 98MB
  private static final int DEFAULT_BOOST_POWER = 2; // BufferSize(262144 << 2 = 1048576) * PaddedAtomicLong(6 Long * 8 = 48Byte) = 48MB

  private int boostPower = DEFAULT_BOOST_POWER;
  private Long scheduleInterval;

  private RejectedPutBufferHandler rejectedPutBufferHandler;
  private RejectedTakeBufferHandler rejectedTakeBufferHandler;

  /**
   * RingBuffer
   */
  private RingBuffer ringBuffer;
  private BufferPaddingExecutor bufferPaddingExecutor;

  @Override
  public void afterPropertiesSet() throws Exception {
    // initialize instanceId & bitsAllocator
    super.afterPropertiesSet();

    // initialize RingBuffer & RingBufferPaddingExecutor
    this.initRingBuffer();
    LOGGER.info("Initialized RingBuffer successfully.");
  }

  @Override
  public long getUID() {
    try {
      return ringBuffer.take();
    } catch (Exception e) {
      LOGGER.error("Generate unique pk exception. ", e);
      throw new IdGenerateException(e);
    }
  }

  @Override
  public String parseUID(long uid) {
    return super.parseUID(uid);
  }

  @Override
  public void destroy() throws Exception {
    bufferPaddingExecutor.shutdown();
  }

  /**
   * Get the UIDs in the same specified second under the max sequence
   *
   * @return UID list, size of {@link BitsAllocator#getMaxSequence()} + 1
   */
  private List<Long> nextIdsForOneSecond(long currentSecond) {
    // Initialize result list size of (max sequence + 1)
    int listSize = (int) bitsAllocator.getMaxSequence() + 1;
    List<Long> uidList = new ArrayList<>(listSize);

    // Allocate the first sequence of the second, the others can be calculated with the offset
    long firstSeqUid = bitsAllocator.allocate(currentSecond - epochSeconds, instanceId, 0L);
    for (int offset = 0; offset < listSize; offset++) {
      uidList.add(firstSeqUid + offset);
    }

    return uidList;
  }

  /**
   * Initialize RingBuffer & RingBufferPaddingExecutor
   */
  private void initRingBuffer() {
    // initialize RingBuffer
    int bufferSize = ((int) bitsAllocator.getMaxSequence() + 1) << boostPower;
    int paddingFactor = RingBuffer.DEFAULT_PADDING_PERCENT;
    this.ringBuffer = new RingBuffer(bufferSize, paddingFactor);
    LOGGER.info("Initialized ring buffer size:{}, paddingFactor:{}", bufferSize, paddingFactor);

    // initialize RingBufferPaddingExecutor
    boolean usingSchedule = (scheduleInterval != null);
    this.bufferPaddingExecutor = new BufferPaddingExecutor(ringBuffer, this::nextIdsForOneSecond,
        usingSchedule);
    if (usingSchedule) {
      bufferPaddingExecutor.setScheduleInterval(scheduleInterval);
    }

    LOGGER.info("Initialized BufferPaddingExecutor. Using schdule:{}, interval:{}", usingSchedule,
        scheduleInterval);

    // set rejected put/take handle policy
    this.ringBuffer.setBufferPaddingExecutor(bufferPaddingExecutor);
    if (rejectedPutBufferHandler != null) {
      this.ringBuffer.setRejectedPutHandler(rejectedPutBufferHandler);
    }
    if (rejectedTakeBufferHandler != null) {
      this.ringBuffer.setRejectedTakeHandler(rejectedTakeBufferHandler);
    }

    // fill in all slots of the RingBuffer
    bufferPaddingExecutor.paddingBuffer();

    // start buffer padding threads
    bufferPaddingExecutor.start();
  }

  /**
   * Setters for spring property
   */
  public void setBoostPower(int boostPower) {
    assertTrue(boostPower > 0, "Boost power must be positive!");
    this.boostPower = boostPower;
  }

  public void setRejectedPutBufferHandler(RejectedPutBufferHandler rejectedPutBufferHandler) {
    assertNotNull(rejectedPutBufferHandler, "RejectedPutBufferHandler can't be null!");
    this.rejectedPutBufferHandler = rejectedPutBufferHandler;
  }

  public void setRejectedTakeBufferHandler(RejectedTakeBufferHandler rejectedTakeBufferHandler) {
    assertNotNull(rejectedTakeBufferHandler, "RejectedTakeBufferHandler can't be null!");
    this.rejectedTakeBufferHandler = rejectedTakeBufferHandler;
  }

  public void setScheduleInterval(long scheduleInterval) {
    assertTrue(scheduleInterval > 0, "Schedule interval must positive!");
    this.scheduleInterval = scheduleInterval;
  }

}
