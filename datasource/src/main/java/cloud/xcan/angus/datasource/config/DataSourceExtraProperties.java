package cloud.xcan.angus.datasource.config;

import cloud.xcan.angus.api.enums.SupportedDbType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Extended configuration properties for database connections that supplement
 * Spring Boot's standard DataSource configuration. This class provides additional
 * database-specific settings and entity scanning configuration.
 * </p>
 * 
 * <p>
 * Key features:
 * - Database type specification for driver and dialect selection
 * - Database deployment mode configuration (single, master-slave)
 * - JPA entity package scanning configuration
 * - Support for multiple database types and architectures
 * </p>
 * 
 * <p>
 * Configuration example:
 * <pre>
 * xcan:
 *   datasource:
 *     extra:
 *       db-type: MYSQL
 *       db-mode: master-slave
 *       entity-packages:
 *         - com.example.domain.user
 *         - com.example.domain.product
 * </pre>
 * </p>
 * 
 * <p>
 * This configuration works in conjunction with Spring Boot's standard
 * DataSource properties to provide a complete database setup.
 * </p>
 * 
 * @see org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
 * @see SupportedDbType
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "xcan.datasource.extra")
public class DataSourceExtraProperties {

  /**
   * <p>
   * Specifies the type of database being used.
   * This setting influences driver selection, SQL dialect configuration,
   * and database-specific optimizations.
   * </p>
   * 
   * <p>
   * Supported database types:
   * - MYSQL: MySQL database with optimized configurations
   * - POSTGRES: PostgreSQL database with specific dialect settings
   * </p>
   * 
   * <p>
   * The database type affects:
   * - JDBC driver selection and configuration
   * - JPA/Hibernate dialect configuration
   * - SQL syntax and feature availability
   * - Performance optimization strategies
   * - Connection pool settings
   * </p>
   */
  private SupportedDbType dbType;

  /**
   * <p>
   * Specifies the database deployment architecture and connection strategy.
   * This setting determines how the application connects to and utilizes
   * the database infrastructure.
   * </p>
   * 
   * <p>
   * Supported deployment modes:
   * - "single": Single database instance for simple deployments
   * - "master-slave": Master-slave replication setup for read/write separation
   * </p>
   * 
   * <p>
   * Master-slave mode enables:
   * - Read operations directed to slave instances
   * - Write operations directed to master instance
   * - Improved read performance through load distribution
   * - Enhanced availability through replication
   * </p>
   */
  private String dbMode;

  /**
   * <p>
   * Array of package names to scan for JPA entity classes.
   * These packages will be automatically scanned for @Entity annotated classes
   * during application startup.
   * </p>
   * 
   * <p>
   * Benefits of explicit entity package configuration:
   * - Faster startup time by limiting scanning scope
   * - Better control over which entities are included
   * - Clearer separation of domain models
   * - Reduced classpath scanning overhead
   * </p>
   * 
   * <p>
   * Example package structure:
   * - com.example.user.entity: User-related entities
   * - com.example.product.entity: Product catalog entities
   * - com.example.order.entity: Order management entities
   * </p>
   */
  private String[] entityPackages;
}
