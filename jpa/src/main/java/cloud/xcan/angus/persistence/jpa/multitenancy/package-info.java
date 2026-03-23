/**
 * Row-level tenant isolation via Hibernate {@link org.hibernate.annotations.Filter} and optional
 * native-SQL predicates for dynamic search repositories.
 */
@org.hibernate.annotations.FilterDef(
    name = "xcanTenantScope",
    parameters = @org.hibernate.annotations.ParamDef(name = "tenantId", type = Long.class))
package cloud.xcan.angus.persistence.jpa.multitenancy;
