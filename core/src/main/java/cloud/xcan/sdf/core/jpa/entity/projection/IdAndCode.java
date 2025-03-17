package cloud.xcan.sdf.core.jpa.entity.projection;

/**
 * Used to project(projection) id and code fields when returning from a query.
 */
public interface IdAndCode {

  Long getId();

  String getCode();

}
