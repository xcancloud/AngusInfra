package cloud.xcan.angus.core.jpa.entity.projection;

/**
 * Used to project(projection) id and name fields when returning from a query.
 */
public interface IdAndName {

  Long getId();

  String getName();

}
