package cloud.xcan.angus.web;

import static cloud.xcan.angus.core.event.repository.MemoryAndRemoteEventRepository.DEFAULT_MEMORY_CAPACITY;
import static cloud.xcan.angus.core.event.repository.MemoryAndRemoteEventRepository.DEFAULT_SEND_BUFFER_CAPACITY;
import static org.springframework.boot.web.servlet.filter.OrderedFilter.REQUEST_WRAPPER_FILTER_MAX_ORDER;

import cloud.xcan.angus.api.obf.Str0;
import cloud.xcan.angus.core.disruptor.DisruptorQueueFactory;
import cloud.xcan.angus.core.disruptor.DisruptorQueueManager;
import cloud.xcan.angus.core.event.AbstractEvent;
import cloud.xcan.angus.core.event.ApiLogEvent;
import cloud.xcan.angus.core.event.CommonEvent;
import cloud.xcan.angus.core.event.EventListener;
import cloud.xcan.angus.core.event.EventRepository;
import cloud.xcan.angus.core.event.EventSender;
import cloud.xcan.angus.core.event.EventsListener;
import cloud.xcan.angus.core.event.OperationEvent;
import cloud.xcan.angus.core.event.OperationEventListener;
import cloud.xcan.angus.core.event.remote.ApiLogEventRemote;
import cloud.xcan.angus.core.event.remote.CommonEventRemote;
import cloud.xcan.angus.core.event.remote.OperationEventRemote;
import cloud.xcan.angus.core.event.repository.MemoryAndRemoteEventRepository;
import cloud.xcan.angus.core.log.ApiLogFilter;
import cloud.xcan.angus.core.log.ApiLogProperties;
import cloud.xcan.angus.core.log.OperationLogAspect;
import cloud.xcan.angus.core.log.OperationLogProperties;
import cloud.xcan.angus.spec.thread.DefaultThreadFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Client;
import feign.Contract;
import feign.Feign;
import feign.codec.Decoder;
import feign.codec.Encoder;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;

/**
 * Request log and operation log auto-configuration for {@link AbstractEvent}.
 *
 * @author XiaoLong Liu
 * @see EnableAutoConfiguration
 */
@Configuration(proxyBeanMethods = false)
@Import(FeignClientsConfiguration.class)
//@Conditional({AuditLogAutoConfigurer.AuditLogCondition.class}) <- Fix:: Will cause CommonService#Setting to be invalid
@EnableConfigurationProperties({ApiLogProperties.class, OperationLogProperties.class})
@ConditionalOnExpression(value = "${xcan.api-log.enabled:true} || ${xcan.opt-log.enabled:false}")
public class AuditLogAutoConfigurer {

  public final static String[] RESOURCES = new String[]{
      new Str0(new long[]{0x7582756E66662C60L, 0x3414E4B58011F8F0L}).toString() /* => "/api/*" */,
      new Str0(new long[]{0x79F0996A85EB7CA4L, 0x802CF7EA9F25F197L, 0x4EFFF40A372A716DL}).toString()
      /* => "/doorapi/*" */,
      new Str0(new long[]{0xC60EDC5DD5371E7DL, 0x5B324C24FDC93F38L, 0xA438A15F9C45F104L}).toString()
      /* => "/pubapi/*" */,
      new Str0(new long[]{0xB55FCE32E26D0295L, 0x3A77FACAEC89DA49L, 0xB8679922EFED3ECFL}).toString()
      /* => "/openapi2p/*" */};

  @ConditionalOnMissingBean
  @Bean("commonEventRemote")
  public CommonEventRemote commonEventRemote(Client client, Encoder encoder,
      Decoder decoder, Contract contract, ApiLogProperties apiLogProperties) {
    return Feign.builder().client(client)
        .encoder(encoder).decoder(decoder).contract(contract)
        .target(CommonEventRemote.class, "http://" + apiLogProperties.getEventService());
  }

  @DependsOn("commonEventRemote")
  @ConditionalOnMissingBean(name = "commonEventRepository")
  @Bean("commonEventRepository")
  public EventRepository<CommonEvent> commonEventRepository(
      CommonEventRemote commonEventRemote, ObjectMapper objectMapper) {
    return new MemoryAndRemoteEventRepository(
        new Str0(new long[]{0x43E344F47217DBBBL, 0x67D381BF2FF087A1L, 0xFF7F11CDE1A30240L})
            .toString() /* => "CommonEvent" */,
        DEFAULT_MEMORY_CAPACITY, DEFAULT_SEND_BUFFER_CAPACITY, commonEventRemote, objectMapper);
  }

  @DependsOn("commonEventRepository")
  @Bean("commonEventListener")
  public EventListener<CommonEvent> commonEventListener(
      EventRepository<CommonEvent> commonEventRepository) {
    return new EventsListener<>(commonEventRepository);
  }

  @DependsOn("commonEventListener")
  @Bean(EventSender.CommonQueue.QUEUE_NAME)
  public DisruptorQueueManager<CommonEvent> commonEventDisruptorQueue(
      EventListener<CommonEvent> commonEventListener) {
    return DisruptorQueueFactory.createWorkPoolQueue(128 * 1024, true,
        new DefaultThreadFactory(new Str0(
            new long[]{0x87DB4D15C070FC49L, 0x16D7F067641B3BC2L, 0xC036476A4FD9D470L,
                0xCCC3AB1D042159FAL, 0x5B34EB5AD8D32143L})
            .toString() /* => "xcanDisruptorQueueExceptionEvent" */
            , Thread.NORM_PRIORITY),
        commonEventListener
    );
  }

  @ConditionalOnMissingBean
  @Bean("operationEventRemote")
  //@ConditionalOnProperty(prefix = "xcan.optlog", name = "enabled", matchIfMissing = true)
  public OperationEventRemote operationEventRemote(Client client, Encoder encoder,
      Decoder decoder, Contract contract, ApiLogProperties apiLogProperties) {
    return Feign.builder().client(client)
        .encoder(encoder).decoder(decoder).contract(contract)
        .target(OperationEventRemote.class, "http://" + apiLogProperties.getLoggerService());
  }

  @DependsOn("operationEventRemote")
  @ConditionalOnMissingBean(name = "operationEventRepository")
  @Bean("operationEventRepository")
  //@ConditionalOnProperty(prefix = "xcan.optlog", name = "enabled", matchIfMissing = true)
  public EventRepository<OperationEvent> operationEventRepository(
      OperationEventRemote operationEventRemote, ObjectMapper objectMapper) {
    return new MemoryAndRemoteEventRepository(
        new Str0(new long[]{0xBDBAAE78A503E417L, 0xF6CDE1141F4B0541L, 0x48DE689DB0B2C6E9L})
            .toString() /* => "OperationEvent" */,
        DEFAULT_MEMORY_CAPACITY, DEFAULT_SEND_BUFFER_CAPACITY, operationEventRemote, objectMapper);
  }

  @DependsOn("operationEventRepository")
  @Bean("operationEventListener")
  //@ConditionalOnProperty(prefix = "xcan.optlog", name = "enabled", matchIfMissing = true)
  public EventListener<OperationEvent> operationEventListener(
      EventRepository<OperationEvent> operationEventRepository) {
    return new OperationEventListener<>(operationEventRepository);
  }

  @DependsOn("operationEventListener")
  @Bean(EventSender.OperationQueue.QUEUE_NAME)
  //@ConditionalOnProperty(prefix = "xcan.optlog", name = "enabled", matchIfMissing = true)
  public DisruptorQueueManager<OperationEvent> operationEventDisruptorQueue(
      EventListener<OperationEvent> operationEventListener) {
    return DisruptorQueueFactory.createWorkPoolQueue(128 * 1024, true,
        new DefaultThreadFactory(new Str0(
            new long[]{0xC774A2E692120AB9L, 0x4C0995852B7B9E05L, 0xC58BE7578D121B70L,
                0xE0355558E11FD33BL, 0xB640D47EDB07025AL})
            .toString() /* => "xcanDisruptorQueueOperationEvent" */
            , Thread.NORM_PRIORITY),
        operationEventListener
    );
  }

  @Bean
  //@ConditionalOnProperty(prefix = "xcan.optlog", name = "enabled", matchIfMissing = true)
  public OperationLogAspect operationLogAspect(
      DisruptorQueueManager<OperationEvent> operationEventDisruptorQueue) {
    return new OperationLogAspect(operationEventDisruptorQueue);
  }

  @ConditionalOnMissingBean
  @Bean("apiLogEventRemote")
  //@ConditionalOnProperty(prefix = "xcan.apilog", name = "enabled", matchIfMissing = true)
  public ApiLogEventRemote apiLogEventRemote(Client client, Encoder encoder,
      Decoder decoder, Contract contract, ApiLogProperties apiLogProperties) {
    return Feign.builder().client(client)
        .encoder(encoder).decoder(decoder).contract(contract)
        .target(ApiLogEventRemote.class, "http://" + apiLogProperties.getLoggerService());
  }

  @DependsOn("apiLogEventRemote")
  @ConditionalOnMissingBean(name = "apiLogEventRepository")
  @Bean("apiLogEventRepository")
  //@ConditionalOnProperty(prefix = "xcan.apilog", name = "enabled", matchIfMissing = true)
  public EventRepository<ApiLogEvent> apiLogEventRepository(ApiLogEventRemote apiLogEventRemote,
      ObjectMapper objectMapper) {
    return new MemoryAndRemoteEventRepository(
        new Str0(new long[]{0xCA18AEF2FB94E0C5L, 0x3ADDFB9A25AF31EAL, 0x1F7001D2AA0ED670L})
            .toString() /* => "ApiLogEvent" */, DEFAULT_MEMORY_CAPACITY,
        DEFAULT_SEND_BUFFER_CAPACITY, apiLogEventRemote, objectMapper);
  }

  @DependsOn("apiLogEventRepository")
  @Bean("apiLogEventListener")
  //@ConditionalOnProperty(prefix = "xcan.apilog", name = "enabled", matchIfMissing = true)
  public EventListener<ApiLogEvent> apiLogEventListener(
      EventRepository<ApiLogEvent> apiLogEventRepository) {
    return new OperationEventListener<>(apiLogEventRepository);
  }

  @DependsOn("apiLogEventListener")
  @Bean(EventSender.ApiLogQueue.QUEUE_NAME)
  //@ConditionalOnProperty(prefix = "xcan.apilog", name = "enabled", matchIfMissing = true)
  public DisruptorQueueManager<ApiLogEvent> apiLogEventDisruptorQueue(
      EventListener<ApiLogEvent> apiLogEventListener) {
    return DisruptorQueueFactory.createWorkPoolQueue(128 * 1024, true,
        new DefaultThreadFactory(new Str0(
            new long[]{0x31D9C67CC1DFF1CDL, 0xA923DE3DF88AD876L, 0xF4EBDCB6350E9B18L,
                0x84E382598DD15E56L, 0x5939553721A3B9B8L})
            .toString() /* => "xcanDisruptorQueueApiLogEvent" */
            , Thread.NORM_PRIORITY),
        apiLogEventListener
    );
  }

  @Bean
  //@ConditionalOnProperty(prefix = "xcan.apilog", name = "enabled", matchIfMissing = true)
  public FilterRegistrationBean<ApiLogFilter> registrationApiLogFilterBean(
      ApiLogProperties apiLogProperties,
      DisruptorQueueManager<ApiLogEvent> apiLogEventDisruptorQueue) {
    FilterRegistrationBean<ApiLogFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setName(
        new Str0(new long[]{0x6F3C1AE288BCA61DL, 0xD15E0620F78077FDL, 0xCF8300C088496E2EL})
            .toString() /* => "apiLogFilter" */
    );
    registrationBean.setFilter(new ApiLogFilter(apiLogProperties, apiLogEventDisruptorQueue));
    registrationBean.setDispatcherTypes(DispatcherType.REQUEST);
    registrationBean.addUrlPatterns(RESOURCES);
    // Must be executed after RequestContextFilter(OrderedRequestContextFilter) to prevent being overwritten
    registrationBean.setOrder(REQUEST_WRAPPER_FILTER_MAX_ORDER - 98);
    return registrationBean;
  }

  //  @Bean
  //  @ConditionalOnClass(name = "org.springframework.security.authentication.event.AbstractAuthenticationEvent")
  //  @ConditionalOnMissingBean(AbstractAuthenticationAuditListener.class)
  //  public AuthenticationAuditListener authenticationAuditListener() throws EventContent {
  //    return new AuthenticationAuditListener();
  //  }
  //
  //  @Bean
  //  @ConditionalOnClass(name = "org.springframework.security.access.event.AbstractAuthorizationEvent")
  //  @ConditionalOnMissingBean(AbstractAuthorizationAuditListener.class)
  //  public AuthorizationAuditListener authorizationAuditListener() throws EventContent {
  //    return new AuthorizationAuditListener();
  //  }

  //  static final class AuditLogCondition implements Condition {
  //
  //    AuditLogCondition() {
  //    }
  //
  //    @Override
  //    public boolean matches(ConditionContext context, AnnotatedTypeMetadata a) {
  //      String apiLogEnabled = context.getEnvironment().getProperty("xcan.api-log.enabled");
  //      String optLogEnabled = context.getEnvironment().getProperty("xcan.opt-log.enabled");
  //      return (isNotEmpty(apiLogEnabled) && Boolean.parseBoolean(apiLogEnabled)) ||
  //          isNotEmpty(optLogEnabled) && Boolean.parseBoolean(optLogEnabled);
  //    }
  //  }

}
