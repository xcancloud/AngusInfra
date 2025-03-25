package cloud.xcan.angus.core.log;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author liuxiaolong
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

  /**
   * Operation code
   */
  String code();

  /**
   * Rest resource name, eg: 'user'.
   */
  String resource() default "";

  /**
   * Operation description message key, which needs to be defined in the i18n file. eg:
   * #fullname+'added'+#name
   */
  String messageKey();

  /**
   * Whether batched data api, means that the parameter must be in the collection.
   */
  boolean batched() default true;

  /**
   * Evaluation content type.
   *
   * @see EvaluationObject
   */
  EvaluationObject evaluationObject() default EvaluationObject.NONE;

  /**
   * When the modify or delete operation requires the associated resource name, obtain it from the
   * database by specifying the repository.
   *
   * @see BaseRepository#findNameById(Object)
   * @see BaseRepository#findNameByIds(java.util.Collection)
   */
  String repositoryBeanName() default "";

}

