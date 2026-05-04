package cloud.xcan.angus.persistence.jpa.multitenancy.it;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 项目内 {@link org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration} 不注册
 * {@link DataSource}，集成测试需显式提供数据源，否则 {@code HibernateJpaAutoConfiguration} 无法创建
 * {@link jakarta.persistence.EntityManagerFactory}，多租户切面也无法注入。
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(DataSourceProperties.class)
public class MultitenancyItDataSourceConfiguration {

  @Bean
  @Primary
  public DataSource dataSource(DataSourceProperties properties) {
    return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
  }
}
