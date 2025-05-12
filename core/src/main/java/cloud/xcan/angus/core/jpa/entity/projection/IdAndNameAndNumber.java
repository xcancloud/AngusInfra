package cloud.xcan.angus.core.jpa.entity.projection;

/**
 * Used to project(projection) id、name and num fields when returning from a query.
 */
public interface IdAndNameAndNumber {

  Long getId();

  String getName();

  Long getNum();

}
