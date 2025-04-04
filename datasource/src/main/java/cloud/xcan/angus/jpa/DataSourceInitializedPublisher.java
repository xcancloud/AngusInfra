package cloud.xcan.angus.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.spi.PersistenceProvider;
import jakarta.persistence.spi.PersistenceUnitInfo;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.orm.jpa.JpaDialect;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

/**
 * {@link BeanPostProcessor} used to fire {@link DataSourceSchemaCreatedEvent}s. Should only be
 * registered via the inner {@link Registrar} class.
 */
class DataSourceInitializedPublisher implements BeanPostProcessor {

  private DataSource dataSource;

  private JpaProperties jpaProperties;

  private HibernateProperties hibernateProperties;

  private DataSourceSchemaCreatedPublisher schemaCreatedPublisher;

  private DataSourceInitializationCompletionListener initializationCompletionListener;

  DataSourceInitializedPublisher(DataSourceInitializationCompletionListener completionListener) {
    this.initializationCompletionListener = completionListener;
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName)
      throws BeansException {
    if (bean instanceof LocalContainerEntityManagerFactoryBean) {
      LocalContainerEntityManagerFactoryBean factory = (LocalContainerEntityManagerFactoryBean) bean;
      if (factory.getBootstrapExecutor() != null && factory.getJpaVendorAdapter() != null) {
        this.schemaCreatedPublisher = new DataSourceSchemaCreatedPublisher(factory);
        factory.setJpaVendorAdapter(this.schemaCreatedPublisher);
      }
    }
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    if (bean instanceof DataSource) {
      // Normally this will be the right DataSource
      this.dataSource = (DataSource) bean;
    }
    if (bean instanceof JpaProperties) {
      this.jpaProperties = (JpaProperties) bean;
    }
    if (bean instanceof HibernateProperties) {
      this.hibernateProperties = (HibernateProperties) bean;
    }
    if (bean instanceof LocalContainerEntityManagerFactoryBean
        && this.schemaCreatedPublisher == null) {
      LocalContainerEntityManagerFactoryBean factoryBean = (LocalContainerEntityManagerFactoryBean) bean;
      EntityManagerFactory entityManagerFactory = factoryBean.getNativeEntityManagerFactory();
      publishEventIfRequired(factoryBean, entityManagerFactory);
    }
    return bean;
  }

  private void publishEventIfRequired(LocalContainerEntityManagerFactoryBean factoryBean,
      EntityManagerFactory entityManagerFactory) {
    DataSource dataSource = findDataSource(factoryBean, entityManagerFactory);
    if (dataSource != null && isInitializingDatabase(dataSource)) {
      // this.applicationContext.publishEvent(new DataSourceSchemaCreatedEvent(dataSource));
    }
  }

  private DataSource findDataSource(LocalContainerEntityManagerFactoryBean factoryBean,
      EntityManagerFactory entityManagerFactory) {
    Object dataSource = entityManagerFactory.getProperties()
        .get("javax.persistence.nonJtaDataSource");
    if (dataSource == null) {
      dataSource = factoryBean.getPersistenceUnitInfo().getNonJtaDataSource();
    }
    return (dataSource instanceof DataSource) ? (DataSource) dataSource : this.dataSource;
  }

  private boolean isInitializingDatabase(DataSource dataSource) {
    if (this.jpaProperties == null || this.hibernateProperties == null) {
      return true; // better safe than sorry
    }
    Supplier<String> defaultDdlAuto = () -> (EmbeddedDatabaseConnection.isEmbedded(dataSource)
        ? "create-drop" : "none");
    Map<String, Object> hibernate = this.hibernateProperties.determineHibernateProperties(
        this.jpaProperties.getProperties(), new HibernateSettings().ddlAuto(defaultDdlAuto));
    return hibernate.containsKey("hibernate.hbm2ddl.auto");
  }

  /**
   * {@link ApplicationListener} that, upon receiving {@link ContextRefreshedEvent}, blocks until
   * any asynchronous DataSource initialization has completed.
   */
  static class DataSourceInitializationCompletionListener
      implements ApplicationListener<ContextRefreshedEvent>, Ordered, ApplicationContextAware {

    private volatile ApplicationContext applicationContext;

    private volatile Future<?> dataSourceInitialization;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
      if (!event.getApplicationContext().equals(this.applicationContext)) {
        return;
      }
      Future<?> dataSourceInitialization = this.dataSourceInitialization;
      if (dataSourceInitialization != null) {
        try {
          dataSourceInitialization.get();
        } catch (Exception ex) {
          throw new RuntimeException(ex);
        }
      }
    }

    @Override
    public int getOrder() {
      return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
      this.applicationContext = applicationContext;
    }

  }

  /**
   * {@link ImportBeanDefinitionRegistrar} to register the {@link DataSourceInitializedPublisher}
   * without causing early bean instantiation issues.
   */
  static class Registrar implements ImportBeanDefinitionRegistrar {

    private static final String PUBLISHER_BEAN_NAME = "dataSourceInitializedPublisher";

    private static final String COMPLETION_LISTENER_BEAN_BEAN = DataSourceInitializationCompletionListener.class
        .getName();

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
        BeanDefinitionRegistry registry) {
      if (!registry.containsBeanDefinition(PUBLISHER_BEAN_NAME)) {
        DataSourceInitializationCompletionListener completionListener = new DataSourceInitializationCompletionListener();
        DataSourceInitializedPublisher publisher = new DataSourceInitializedPublisher(
            completionListener);
        AbstractBeanDefinition publisherDefinition = BeanDefinitionBuilder
            .genericBeanDefinition(DataSourceInitializedPublisher.class, () -> publisher)
            .getBeanDefinition();
        publisherDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        // We don't need this one to be post processed otherwise it can cause a
        // cascade of bean instantiation that we would rather avoid.
        publisherDefinition.setSynthetic(true);
        registry.registerBeanDefinition(PUBLISHER_BEAN_NAME, publisherDefinition);
        AbstractBeanDefinition listenerDefinition = BeanDefinitionBuilder.genericBeanDefinition(
                DataSourceInitializationCompletionListener.class, () -> completionListener)
            .getBeanDefinition();
        listenerDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        // We don't need this one to be post processed otherwise it can cause a
        // cascade of bean instantiation that we would rather avoid.
        listenerDefinition.setSynthetic(true);
        registry.registerBeanDefinition(COMPLETION_LISTENER_BEAN_BEAN, listenerDefinition);
      }
    }

  }

  final class DataSourceSchemaCreatedPublisher implements JpaVendorAdapter {

    private final LocalContainerEntityManagerFactoryBean factoryBean;

    private final JpaVendorAdapter delegate;

    private DataSourceSchemaCreatedPublisher(LocalContainerEntityManagerFactoryBean factoryBean) {
      this.factoryBean = factoryBean;
      this.delegate = factoryBean.getJpaVendorAdapter();
    }

    @Override
    public PersistenceProvider getPersistenceProvider() {
      return this.delegate.getPersistenceProvider();
    }

    @Override
    public String getPersistenceProviderRootPackage() {
      return this.delegate.getPersistenceProviderRootPackage();
    }

    @Override
    public Map<String, ?> getJpaPropertyMap(PersistenceUnitInfo pui) {
      return this.delegate.getJpaPropertyMap(pui);
    }

    @Override
    public Map<String, ?> getJpaPropertyMap() {
      return this.delegate.getJpaPropertyMap();
    }

    @Override
    public JpaDialect getJpaDialect() {
      return this.delegate.getJpaDialect();
    }

    @Override
    public Class<? extends EntityManagerFactory> getEntityManagerFactoryInterface() {
      return this.delegate.getEntityManagerFactoryInterface();
    }

    @Override
    public Class<? extends EntityManager> getEntityManagerInterface() {
      return this.delegate.getEntityManagerInterface();
    }

    @Override
    public void postProcessEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
      this.delegate.postProcessEntityManagerFactory(entityManagerFactory);
      AsyncTaskExecutor bootstrapExecutor = this.factoryBean.getBootstrapExecutor();
      if (bootstrapExecutor != null) {
        DataSourceInitializedPublisher.this.initializationCompletionListener.dataSourceInitialization = bootstrapExecutor
            .submit(
                () -> DataSourceInitializedPublisher.this.publishEventIfRequired(this.factoryBean,
                    entityManagerFactory));
      }
    }

  }

}
