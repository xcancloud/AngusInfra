package cloud.xcan.sdf.core.jpa.entity.projection;

/**
 * Used to project(projection) id and num fields when returning from a query.
 */
public interface IdAndNumber {

  Long getId();

  Long getNum();

}
