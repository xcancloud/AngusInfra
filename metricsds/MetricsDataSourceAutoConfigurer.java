package cloud.xcan.angus.core.tester.infra.metricsds;

import cloud.xcan.angus.jpa.HibernateJpaConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({HibernateJpaConfiguration.class, MetricsDataSourceConfiguration.class})
@AutoConfigureAfter({DataSourceAutoConfiguration.class})
public class MetricsDataSourceAutoConfigurer {

  @Bean(initMethod = "init")
  public TableSchemaManager tableSchemaManager() {
    return new TableSchemaManager();
  }

}
