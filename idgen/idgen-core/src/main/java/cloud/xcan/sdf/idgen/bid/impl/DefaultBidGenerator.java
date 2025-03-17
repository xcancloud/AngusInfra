package cloud.xcan.sdf.idgen.bid.impl;

import static org.hibernate.internal.util.collections.CollectionHelper.LOAD_FACTOR;

import cloud.xcan.sdf.idgen.BidGenerator;
import cloud.xcan.sdf.idgen.bid.ConfigIdAssigner;
import cloud.xcan.sdf.idgen.bid.DateFormat;
import cloud.xcan.sdf.idgen.bid.DistributedIncrAssigner;
import cloud.xcan.sdf.idgen.bid.Format;
import cloud.xcan.sdf.idgen.bid.Mode;
import cloud.xcan.sdf.idgen.bid.Scope;
import cloud.xcan.sdf.idgen.entity.IdConfig;
import cloud.xcan.sdf.idgen.exception.IdGenerateException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class DefaultBidGenerator extends AbstractBidGenerator {

  /**
   * Lock object
   */
  private final Object monitor = new Object();

  /**
   * Spring property.
   */
  private final ConfigIdAssigner configIdAssigner;

  /**
   * Redis operations for simple values.
   */
  private final DistributedIncrAssigner distributedIncrAssigner;

  private final Map<String, IdConfig> idConfigMap = new ConcurrentHashMap<>(1024, LOAD_FACTOR, 128);
  private final Map<String, AtomicLong> idAtomicMap = new ConcurrentHashMap<>(1024, LOAD_FACTOR,
      128);

  public DefaultBidGenerator(ConfigIdAssigner configIdAssigner,
      DistributedIncrAssigner distributedIncrAssigner) {
    this.configIdAssigner = configIdAssigner;
    this.distributedIncrAssigner = distributedIncrAssigner;
  }

  @Override
  public String getId(String bizKey) {
    return getId(bizKey, BidGenerator.GLOBAL_TENANT_ID);
  }

  @Override
  public String getId(String bizKey, Long tenantId) {
    String generatorKey = getGeneratorKey(bizKey, tenantId);
    IdConfig idConfig = checkAndGetIdConfig(bizKey, tenantId, generatorKey);
    Format format = idConfig.getFormat();
    return getFormatId(generatorKey, idConfig, format);
  }

  @Override
  public List<String> getIds(String bizKey, int batchNum) {
    checkBizKeyParam(bizKey);
    checkBatchNumParam(batchNum);
    return getIds(bizKey, batchNum, BidGenerator.GLOBAL_TENANT_ID);
  }

  @Override
  public List<String> getIds(String bizKey, int batchNum, Long tenantId) {
    checkBizKeyParam(bizKey);
    checkBatchNumParam(batchNum);
    checkTenantIdParam(tenantId);
    String generatorKey = getGeneratorKey(bizKey, tenantId);
    IdConfig idConfig = checkAndGetIdConfig(bizKey, tenantId, generatorKey);
    Format format = idConfig.getFormat();
    List<String> sequences = new ArrayList<>(batchNum);
    if (Format.SEQ.equals(format)) {
      return getSequences(generatorKey, idConfig, batchNum, 0, sequences);
    } else if (Format.PREFIX_SEQ.equals(format)) {
      getSequences(generatorKey, idConfig, batchNum, 0, sequences);
      return sequences.stream().map(s -> idConfig.getPrefix() + s).collect(Collectors.toList());
    } else if (Format.DATE_SEQ.equals(format)) {
      getSequences(generatorKey, idConfig, batchNum, 0, sequences);
      return sequences.stream().map(s -> getDate(idConfig) + s).collect(Collectors.toList());
    } else {
      getSequences(generatorKey, idConfig, batchNum, 0, sequences);
      return sequences.stream().map(s -> idConfig.getPrefix() + getDate(idConfig) + s)
          .collect(Collectors.toList());
    }
  }

  private String getFormatId(String generatorKey, IdConfig idConfig, Format format) {
    if (Format.SEQ.equals(format)) {
      return getSequence(generatorKey, idConfig);
    } else if (Format.PREFIX_SEQ.equals(format)) {
      return idConfig.getPrefix() + getSequence(generatorKey, idConfig);
    } else if (Format.DATE_SEQ.equals(format)) {
      return getDate(idConfig) + getSequence(generatorKey, idConfig);
    } else {
      return idConfig.getPrefix() + getDate(idConfig) + getSequence(generatorKey, idConfig);
    }
  }

  public IdConfig checkAndGetIdConfig(String bizKey, Long tenantId, String generatorKey) {
    checkBizKeyParam(bizKey);
    IdConfig idConfig = initAndGetIdConfig(bizKey, generatorKey, tenantId);
    checkRedisInstanceParam(idConfig, distributedIncrAssigner);
    checkIdConfig(idConfig, tenantId);
    return idConfig;
  }

  public IdConfig initAndGetIdConfig(String bizKey, String generatorKey, Long tenantId) {
    IdConfig idConfig = idConfigMap.get(generatorKey);
    if (Objects.nonNull(idConfig)) {
      return idConfig;
    }
    IdConfig idConfigDB = configIdAssigner.retrieveFromIdConfig(bizKey, tenantId);
    if (!Objects.isNull(idConfigDB)) {
      return assignAndInitIdSegment(generatorKey, idConfigDB);
    } else {
      if (BidGenerator.GLOBAL_TENANT_ID.equals(tenantId)) {
        throw new IdGenerateException(
            "Configuration bizKey: " + bizKey + " tenantId: -1 not found");
      } else {
        IdConfig idConfigTemplateDB = configIdAssigner
            .retrieveFromIdConfig(bizKey, BidGenerator.GLOBAL_TENANT_ID);
        if (Objects.isNull(idConfigTemplateDB)) {
          throw new IdGenerateException(
              "Configuration bizKey: " + bizKey + " tenantId: -1 not found");
        }
        if (Scope.TENANT.equals(idConfigTemplateDB.getScope())) {
          idConfigDB = buildIdConfig(idConfigTemplateDB, tenantId);
          idConfigDB = configIdAssigner.saveAndAssignSegment(idConfigDB);
          return assignAndInitIdSegment(generatorKey, idConfigDB);
        } else {
          throw new IdGenerateException(
              "When the scope is PLATFORM, tenantId must be equal to -1");
        }
      }
    }
  }

  private String getGeneratorKey(String bizKey, Long tenantId) {
    return "idgen:" + bizKey + ":" + tenantId;
  }

  private String getDate(IdConfig idConfig) {
    DateFormat dateFormat = idConfig.getDateFormat();
    LocalDate now = LocalDate.now();
    return switch (dateFormat) {
      case YYYY -> String.valueOf(now.getYear());
      case YYYYMM -> String.format("%4d%02d", now.getYear(), now.getMonthValue());
      case YYYYMMDD ->
          String.format("%4d%02d%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
    };
  }

  private String getSequence(String generatorKey, IdConfig idConfig) {
    Integer length = idConfig.getSeqLength();
    Long sequence;
    sequence = idAtomicMap.get(generatorKey).incrementAndGet();
    if (sequence > idConfig.getMaxId()) {
      assignAndSetIdSegment(generatorKey, idConfig, sequence);
      return getSequence(generatorKey, idConfig);
    }
    return checkAndFormatSequence(idConfig, length, sequence);
  }

  private List<String> getSequences(String generatorKey, IdConfig idConfig, int batchNum,
      int nextBatchNum, List<String> sequences) {
    Long sequence;
    int currentBatchNum = nextBatchNum > 0 ? nextBatchNum : batchNum;
    for (int i = 0; i < currentBatchNum; i++) {
      sequence = idAtomicMap.get(generatorKey).incrementAndGet();
      if (sequence <= idConfig.getMaxId()) {
        if (sequences.size() <= batchNum) {
          sequences.add(checkAndFormatSequence(idConfig, idConfig.getSeqLength(), sequence));
        }
        if (sequences.size() == batchNum) {
          return sequences;
        }
      } else {
        assignAndSetIdSegment(generatorKey, idConfig, sequence);
        return getSequences(generatorKey, idConfig, batchNum, batchNum - sequences.size(),
            sequences);
      }
    }
    return null;
  }

  private IdConfig assignAndInitIdSegment(String generatorKey, IdConfig idConfig) {
    long maxId;
    if (Mode.REDIS.equals(idConfig.getMode())) {
      maxId = distributedIncrAssigner.incr(generatorKey, idConfig.getStep());
    } else {
      maxId = configIdAssigner.assignSegmentByParam(idConfig.getStep(),
          idConfig.getBizKey(), idConfig.getTenantId());
    }
    synchronized (monitor) {
      idConfig.setMaxId(maxId);
      idConfigMap.put(generatorKey, idConfig);
      idAtomicMap.put(generatorKey, new AtomicLong(maxId - idConfig.getStep()));
    }
    return idConfig;
  }

  private void assignAndSetIdSegment(String generatorKey, IdConfig idConfig, Long sequence) {
    Long maxId;
    if (Mode.REDIS.equals(idConfig.getMode())) {
      maxId = distributedIncrAssigner.incr(generatorKey, idConfig.getStep());
    } else {
      maxId = configIdAssigner
          .assignSegmentByParam(idConfig.getStep(), idConfig.getBizKey(), idConfig.getTenantId());
    }
    // At this time, if multiple threads are allocated at the same time, a continuous adjustment (jump) segment will occur.
    synchronized (monitor) {
      while (!idAtomicMap.get(generatorKey).compareAndSet(sequence,
          maxId - idConfig.getStep())) {
        break;
      }
      idConfig.setMaxId(maxId);
    }
  }

  private String checkAndFormatSequence(IdConfig idConfig, Integer seqLength,
      Long currentMaxSequence) {
    if (idConfig.getSeqLength() <= 0) {
      return String.valueOf(currentMaxSequence);
    }
    if (String.valueOf(currentMaxSequence).length() > seqLength) {
      throw new IdGenerateException(
          "The length of the generated ID value exceeds the seqLength configuration: " + idConfig
              .getSeqLength());
    }
    return String.format("%0" + seqLength + "d", currentMaxSequence);
  }

  private IdConfig buildIdConfig(IdConfig idConfigTemplateDB, Long tenantId) {
    return new IdConfig()
        .setBizKey(idConfigTemplateDB.getBizKey())
        .setFormat(idConfigTemplateDB.getFormat())
        .setPrefix(idConfigTemplateDB.getPrefix())
        .setDateFormat(idConfigTemplateDB.getDateFormat())
        .setSeqLength(idConfigTemplateDB.getSeqLength())
        .setMode(idConfigTemplateDB.getMode())
        .setScope(Scope.TENANT)
        .setTenantId(tenantId)
        .setMaxId(idConfigTemplateDB.getMode().equals(Mode.REDIS) ? -1L : 0L)
        .setStep(idConfigTemplateDB.getStep())
        .setCreateDate(LocalDateTime.now());
  }
}
