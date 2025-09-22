package cloud.xcan.angus.core.jpa;

import cloud.xcan.angus.spec.utils.ReflectionUtils;
import cloud.xcan.angus.spec.utils.StringUtils;
import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import java.lang.reflect.Field;
import org.hibernate.metamodel.model.domain.internal.EntityTypeImpl;

public class JpaMetadataUtils {

  public static String getTableName(EntityManager em, Class<?> entityClass) {
    // 1. Check @Table annotation
    Table tableAnnotation = entityClass.getAnnotation(Table.class);
    if (tableAnnotation != null && !tableAnnotation.name().isEmpty()) {
      return tableAnnotation.name();
    }

    // 2. Get name from JPA Metamodel
    Metamodel metamodel = em.getMetamodel();
    EntityType<?> entityType = metamodel.entity(entityClass);
    return entityType != null ? StringUtils.camelToUnder(entityType.getName()) : "NOT_FOUND_TABLE";
  }

  /**
   * Get database column name.
   *
   * @param em          JPA Entity Manager
   * @param entityClass Entity class
   * @param fieldName   Field name
   * @return Database column name
   */
  public static String getColumnName(EntityManager em, Class<?> entityClass, String fieldName) {
    // 1. Check @Column annotation
    // Will searches all superclasses
    Field javaField = ReflectionUtils.findField(entityClass, fieldName);
    if (javaField == null) {
      return fieldName;
    }
    Column columnAnnotation = javaField.getAnnotation(Column.class);
    if (columnAnnotation != null && !columnAnnotation.name().isEmpty()) {
      return columnAnnotation.name();
    }

    // 2. Get attribute from JPA Metamodel
    Metamodel metamodel = em.getMetamodel();
    EntityTypeImpl<?> entityType = (EntityTypeImpl<?>) metamodel.entity(entityClass);
    Attribute<?, ?> attribute = entityType.findAttribute(fieldName);
    return attribute != null ? StringUtils.camelToUnder(attribute.getName()) : null;
  }

  public static <T> boolean hasAttribute(EntityType<T> entityType, String propertyName) {
    return entityType.getDeclaredAttributes().stream()
        .anyMatch(attribute -> attribute.getName().equals(propertyName));
  }
}
