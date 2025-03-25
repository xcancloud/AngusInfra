package cloud.xcan.angus.core.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;

public class JpaMetadataUtils {

  public static String getTableName(EntityManager em, Class<?> entityClass) {
    Metamodel metamodel = em.getMetamodel();
    EntityType<?> entityType = metamodel.entity(entityClass);
    return entityType.getName();
  }

  public static String getJpaColumnName(EntityManager em, Class<?> entityClass, String fieldName) {
    Metamodel metamodel = em.getMetamodel();
    EntityType<?> entityType = metamodel.entity(entityClass);
    return entityType.getAttribute(fieldName).getName();
  }

}
