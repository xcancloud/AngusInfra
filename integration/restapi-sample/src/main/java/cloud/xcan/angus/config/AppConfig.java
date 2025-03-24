package cloud.xcan.angus.config;

import cloud.xcan.sdf.core.jpa.repository.BaseRepositoryImpl;
import cloud.xcan.sdf.datasource.config.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration

@EnableJpaAuditing
@EnableTransactionManagement
@EnableJpaRepositories(
    repositoryBaseClass = BaseRepositoryImpl.class,
    basePackages = {"cloud.xcan.angus.domain"
    })
public class AppConfig {

  @Bean("dataSourceProperties")
  @ConfigurationProperties(prefix = "spring.datasource")
  //@ConditionalOnProperty(name = "xcan.datasource.extra.dbType", havingValue = "mysql")
  public DataSourceProperties loadMySqlDataSourceProperties() {
    return new DataSourceProperties();
  }

}
