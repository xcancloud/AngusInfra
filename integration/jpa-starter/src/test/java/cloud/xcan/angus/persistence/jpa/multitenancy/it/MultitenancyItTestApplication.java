package cloud.xcan.angus.persistence.jpa.multitenancy.it;

import cloud.xcan.angus.persistence.jpa.multitenancy.TenantEntity;
import cloud.xcan.angus.persistence.jpa.multitenancy.TenantJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 仅用于多租户 H2 集成测试：扫描 IT 实体包，并包含 {@link TenantEntity} 所在包以便 Hibernate 加载同包 {@code package-info} 中的
 * {@code @FilterDef(xcanTenantScope)}。
 */
@SpringBootApplication
@EntityScan(basePackageClasses = {TenantEntity.class, MtItDepartment.class})
@EnableJpaRepositories(basePackageClasses = MtItDepartmentRepository.class)
@Import(TenantJpaAutoConfiguration.class)
public class MultitenancyItTestApplication {

}
