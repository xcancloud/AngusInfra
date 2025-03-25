package cloud.xcan.angus.core.jpa.repository.summary;

import cloud.xcan.angus.spec.annotations.DoInFuture;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author liuxiaolong
 * <p>
 * It is recommended to annotate the business query, and it is prohibited to use it in the api
 * package.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SummaryQueryRegister {

  String DEFAULT_GROUP_DATE_COLUMN = "createdDate";
  String DEFAULT_AGGREGATE_COLUMN = "id";

  String name();

  String table();

  @DoInFuture("Unimplemented")
  boolean hasSysAdmin() default false;

  @DoInFuture("Unimplemented")
  String[] hasAnyAuthority() default {};

  @DoInFuture("Unimplemented")
  String[] hasAuthority() default {};

  /**
   * Whether it is a multi-tenant control table
   */
  boolean isMultiTenantCtrl() default true;

  /**
   * Whether to query all at the operation client and optTenantId is null
   */
  boolean multiTenantAutoCtrlWhenOpClient() default true;

  boolean ignoreDeleted() default false;

  String topAuthority() default "";

  /**
   * When grouping multiple fields, a union index must be created, otherwise the all table will be
   * queried.
   * <p>
   * Multiple fields group must be consistent with the order of the union index.
   * <p>
   * Note: When the union index is not created, EXPLAIN can see that it is a all table query only
   * when there is a large amount of data.
   */
  String[] groupByColumns() default {
      DEFAULT_GROUP_DATE_COLUMN
  };

  String[] aggregateColumns() default {
      DEFAULT_AGGREGATE_COLUMN
  };
}
