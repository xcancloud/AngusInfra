/**
 * Row-level tenant isolation via Hibernate {@link org.hibernate.annotations.Filter} and optional
 * native-SQL predicates for dynamic search repositories.
 *
 * <p>{@code @FilterDef(xcanTenantScope)} is declared on
 * {@link cloud.xcan.angus.persistence.jpa.entity.TenantEntity} so that it is
 * registered automatically when the entity package is scanned.
 */
package cloud.xcan.angus.persistence.jpa.multitenancy;
