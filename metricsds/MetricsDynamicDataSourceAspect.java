package cloud.xcan.angus.core.tester.infra.metricsds;

import static cloud.xcan.angus.core.tester.infra.metricsds.MetricsDataSourceConfiguration.METRICS_DATASOURCE_PREFIX;
import static cloud.xcan.angus.core.tester.infra.metricsds.MetricsDataSourceConfiguration.METRICS_DATASOURCE_SUFFIX;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.safeStringValue;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.persistence.jpa.entity.TenantEntity;
import cloud.xcan.angus.core.tester.infra.metricsds.MetricsDataSourceContextHolder.Shard;
import cloud.xcan.angus.core.tester.infra.metricsds.config.MetricsDataSourceProperties;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * Shard DB with tenant ID, Shard table with {@link Sharding#tableField}.
 */
@Slf4j
@Aspect
@Component
public class MetricsDynamicDataSourceAspect {

  @Resource
  private MetricsDataSourceProperties properties;

  public final static String DEFAULT_SHARDING_KEY = "tenantId";

  @Pointcut("execution(* cloud.xcan.angus.core.tester.infra.metricsds.domain..*Repo.*(..))")
  private void aspect() {
  }

  @Around("aspect()")
  public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
    try {
      Shard shard = getShardInfo(joinPoint);
      MetricsDataSourceContextHolder.setShard(shard);
      return joinPoint.proceed();
    } finally {
      MetricsDataSourceContextHolder.clear();
    }
  }

  private Shard getShardInfo(ProceedingJoinPoint joinPoint) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    Sharding shardingAnnotation = method.getAnnotation(Sharding.class);
    if (shardingAnnotation == null) {
      log.warn("Metrics Repo method missing @Sharding annotation, falling back to master: {}.{}",
          method.getDeclaringClass().getSimpleName(), method.getName());
      return new Shard()
          .dataSource(MetricsDataSourceConfiguration.MASTER_DATASOURCE_KEY)
          .tableSecondIndex(-1);
    }

    // Sharding
    Map<String, Long> params = getShardingParam(joinPoint, shardingAnnotation);
    long tenantId = params.containsKey(DEFAULT_SHARDING_KEY)
        ? params.get(DEFAULT_SHARDING_KEY) : getOptTenantId();
    return new Shard()
        .dataSource(METRICS_DATASOURCE_PREFIX + calcDbIndex(tenantId) + METRICS_DATASOURCE_SUFFIX)
        .tableSecondIndex(calcTableSecondIndex(params, shardingAnnotation))
        .tenantId(tenantId);
  }

  private Map<String, Long> getShardingParam(ProceedingJoinPoint joinPoint, Sharding anno) {
    Map<String, Long> map = new HashMap<>();
    Object[] values = joinPoint.getArgs();

    if (isEmpty(values)) {
      return map;
    }

    // Multiple tables are not supported, Only a single shard table data can be operated on at a time.
    if (isEmpty(map)) {
      for (int i = 0; i < values.length; i++) {
        if (isEmpty(values[i])) {
          continue;
        }

        // Find in first collection
        if (values[i] instanceof TenantEntity && anno.tableField().equals(DEFAULT_SHARDING_KEY)) {
          map.put(anno.tableField(), ((TenantEntity) values[i]).getTenantId());
          return map;
        }

        // Find in first collection
        if (values[i] instanceof Iterable iterable) {
          if (iterable.iterator().hasNext()) {
            Object next = iterable.iterator().next();
            if (next instanceof SearchCriteria sc) {
              if (sc.getKey().equals(anno.tableField()) && nonNull(sc.getValue())) {
                map.put(anno.tableField(),
                    Long.parseLong(safeStringValue(sc.getValue().toString())));
                return map;
              } else {
                for (Object o : iterable) {
                  sc = (SearchCriteria) o;
                  if (sc.getKey().equals(anno.tableField()) && nonNull(sc.getValue())) {
                    map.put(anno.tableField(),
                        Long.parseLong(safeStringValue(sc.getValue().toString())));
                    return map;
                  }
                }
              }
            }
            try {
              Field shardField = FieldUtils.getField(next.getClass(), anno.tableField(), true);
              map.put(anno.tableField(), Long.valueOf(shardField.get(next).toString()));
              return map;
            } catch (Exception e) {
              // Continue find
            }
          }
          // Find in first filter
        } else if (values[i] instanceof GenericSpecification gs) {
          Set<SearchCriteria> criteries = gs.getCriteria();
          if (isNotEmpty(criteries)) {
            Optional<SearchCriteria> criteriaOptional = criteries.stream()
                .filter(c -> c.getKey().equals(anno.tableField())).findFirst();
            criteriaOptional.ifPresent(criteria -> map
                .put(anno.tableField(), Long.valueOf(criteria.getValue().toString())));
            return map;
          }
        }
      }
    }

    // Find first long type
    if (isEmpty(map)) {
      for (Object value : values) {
        if (value instanceof Long) {
          map.put(anno.tableField(), Long.valueOf(value.toString()));
          return map;
        }
      }
    }

    // Note: Exchange server handler is not user action
    // Note: Job can also inject tenant id
    Long optTenantId = getOptTenantId();
    if (isEmpty(map) && DEFAULT_SHARDING_KEY.equals(anno.tableField())
        && nonNull(optTenantId) && optTenantId > 0) {
      map.put(anno.tableField(), getOptTenantId());
    }
    return map;
  }

  private long calcDbIndex(Long tenantId) {
    return tenantId % properties.getShardDbNum();
  }

  private long calcTableSecondIndex(Map<String, Long> params, Sharding shardingAnnotation) {
    Long shardTableSecondField = params.get(shardingAnnotation.tableField());
    return !properties.getEnableTabledSecondIndex() || isNull(shardTableSecondField)
        || shardTableSecondField < 0 ? -1
        : shardTableSecondField % properties.getShardTabledNum();
  }

}
