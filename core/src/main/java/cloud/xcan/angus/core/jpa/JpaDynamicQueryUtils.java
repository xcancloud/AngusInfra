package cloud.xcan.angus.core.jpa;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static java.util.Objects.isNull;

import cloud.xcan.angus.spec.utils.ObjectUtils;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JpaDynamicQueryUtils {

  public static List<Object[]> executeDynamicQuery(EntityManager entityManager,
      String selectQuery) {
    return entityManager.createNativeQuery(selectQuery).getResultList();
  }

  public static List executeDynamicQuery0(EntityManager entityManager, String selectQuery) {
    return entityManager.createNativeQuery(selectQuery).getResultList();
  }

  public static <T> List<T> convert(List<Object[]> rows, Function<? super Object[], T> mapper) {
    return isEmpty(rows) ? Collections.emptyList() :
        rows.stream().map(mapper).collect(Collectors.toList());
  }

  public static <T> List<T> executeDynamicQuery(EntityManager entityManager,
      String selectQuery, Function<? super Object[], T> mapper) {
    List<Object[]> values = executeDynamicQuery(entityManager, selectQuery);
    return isEmpty(values) ? null : convert(values, mapper);
  }

  public static int objectArrToInt(List<?> objects) {
    if (isNull(objects) || objects.size() == 0) {
      return 0;
    }
    return ObjectUtils.convert(objects.get(0), BigDecimal.class).intValue();
  }

}
