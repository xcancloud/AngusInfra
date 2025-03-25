package org.springframework.boot.autoconfigure.jdbc;

import cloud.xcan.angus.jpa.DataSourcePoolMetadataProvidersConfiguration;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for {@link DataSource}.
 *
 * @author Dave Syer
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @author Kazuki Shimizu
 * @author XiaoLong Liu
 * @since 1.0.0
 */
@AutoConfiguration(before = SqlInitializationAutoConfiguration.class)
//@ConditionalOnClass({ DataSource.class, EmbeddedDatabaseType.class })
//@ConditionalOnMissingBean(type = "io.r2dbc.spi.ConnectionFactory")
@EnableConfigurationProperties(DataSourceProperties.class)
@Import({DataSourcePoolMetadataProvidersConfiguration.class,
    DataSourceCheckpointRestoreConfiguration.class})
public class DataSourceAutoConfiguration {

}
