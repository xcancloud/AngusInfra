package cloud.xcan.angus.web;

import static cloud.xcan.angus.core.event.repository.MemoryAndRemoteEventRepository.DEFAULT_MEMORY_CAPACITY;
import static cloud.xcan.angus.core.event.repository.MemoryAndRemoteEventRepository.DEFAULT_SEND_BUFFER_CAPACITY;
import static org.springframework.boot.web.servlet.filter.OrderedFilter.REQUEST_WRAPPER_FILTER_MAX_ORDER;

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
import cloud.xcan.angus.observability.log.ApiLogFilter;
import cloud.xcan.angus.observability.log.ApiLogProperties;
import cloud.xcan.angus.security.FeignInnerApiAuthInterceptor;
import cloud.xcan.angus.spec.thread.DefaultThreadFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Client;
import feign.Contract;
import feign.Feign;
import feign.codec.Decoder;
import feign.codec.Encoder;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * Request log and operation log auto-configuration for {@link AbstractEvent}.
 *
 * @author XiaoLong Liu
 * @see EnableAutoConfiguration
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(FeignAutoConfigurer.class)
//@Conditional({AuditLogAutoConfigurer.AuditLogCondition.class}) <- Fix:: Will cause CommonService#Setting to be invalid
@EnableConfigurationProperties({ApiLogProperties.class})
@ConditionalOnExpression(value = "${xcan.api-log.enabled:true} || ${xcan.opt-log.enabled:false}")
public class AuditLogAutoConfigurer {

  public final static String[] RESOURCES = new String[]{
      "/api/*", "/innerapi/*", "/pubapi/*", "/openapi2p/*"};

  @ConditionalOnMissingBean
  @Bean("commonEventRemote")
  public CommonEventRemote commonEventRemote(Client client, Encoder encoder,
      Decoder decoder, Contract contract, ApiLogProperties apiLogProperties,
      FeignInnerApiAuthInterceptor feignInnerApiAuthInterceptor) {
    return Feign.builder().client(client)
        .encoder(encoder).decoder(decoder).contract(contract)
        .requestInterceptor(feignInnerApiAuthInterceptor)
        .target(CommonEventRemote.class, "http://" + apiLogProperties.getEventService());
  }

  @DependsOn("commonEventRemote")
  @ConditionalOnMissingBean(name = "commonEventRepository")
  @Bean("commonEventRepository")
  public EventRepository<CommonEvent> commonEventRepository(
      CommonEventRemote commonEventRemote, ObjectMapper objectMapper) {
    return new MemoryAndRemoteEventRepository<CommonEvent>(
        "CommonEvent",
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
        new DefaultThreadFactory("xcanDisruptorQueueExceptionEvent", Thread.NORM_PRIORITY),
        commonEventListener);
  }

  @ConditionalOnMissingBean
  @Bean("operationEventRemote")
  //@ConditionalOnProperty(prefix = "xcan.opt-log", name = "enabled", matchIfMissing = true)
  public OperationEventRemote operationEventRemote(Client client, Encoder encoder,
      Decoder decoder, Contract contract, ApiLogProperties apiLogProperties,
      FeignInnerApiAuthInterceptor feignInnerApiAuthInterceptor) {
    return Feign.builder().client(client)
        .encoder(encoder).decoder(decoder).contract(contract)
        .requestInterceptor(feignInnerApiAuthInterceptor)
        .target(OperationEventRemote.class, "http://" + apiLogProperties.getLoggerService());
  }

  @DependsOn("operationEventRemote")
  @ConditionalOnMissingBean(name = "operationEventRepository")
  @Bean("operationEventRepository")
  //@ConditionalOnProperty(prefix = "xcan.opt-log", name = "enabled", matchIfMissing = true)
  public EventRepository<OperationEvent> operationEventRepository(
      OperationEventRemote operationEventRemote, ObjectMapper objectMapper) {
    return new MemoryAndRemoteEventRepository<OperationEvent>(
        "OperationEvent",
        DEFAULT_MEMORY_CAPACITY, DEFAULT_SEND_BUFFER_CAPACITY, operationEventRemote, objectMapper);
  }

  @DependsOn("operationEventRepository")
  @Bean("operationEventListener")
  //@ConditionalOnProperty(prefix = "xcan.opt-log", name = "enabled", matchIfMissing = true)
  public EventListener<OperationEvent> operationEventListener(
      EventRepository<OperationEvent> operationEventRepository) {
    return new OperationEventListener<>(operationEventRepository);
  }

  @DependsOn("operationEventListener")
  @Bean(EventSender.OperationQueue.QUEUE_NAME)
  //@ConditionalOnProperty(prefix = "xcan.opt-log", name = "enabled", matchIfMissing = true)
  public DisruptorQueueManager<OperationEvent> operationEventDisruptorQueue(
      EventListener<OperationEvent> operationEventListener) {
    return DisruptorQueueFactory.createWorkPoolQueue(128 * 1024, true,
        new DefaultThreadFactory("xcanDisruptorQueueOperationEvent", Thread.NORM_PRIORITY),
        operationEventListener);
  }

  @ConditionalOnMissingBean
  @Bean("apiLogEventRemote")
  //@ConditionalOnProperty(prefix = "xcan.api-log", name = "enabled", matchIfMissing = true)
  public ApiLogEventRemote apiLogEventRemote(Client client, Encoder encoder,
      Decoder decoder, Contract contract, ApiLogProperties apiLogProperties,
      FeignInnerApiAuthInterceptor feignInnerApiAuthInterceptor) {
    return Feign.builder().client(client)
        .encoder(encoder).decoder(decoder).contract(contract)
        .requestInterceptor(feignInnerApiAuthInterceptor)
        .target(ApiLogEventRemote.class, "http://" + apiLogProperties.getLoggerService());
  }

  @DependsOn("apiLogEventRemote")
  @ConditionalOnMissingBean(name = "apiLogEventRepository")
  @Bean("apiLogEventRepository")
  //@ConditionalOnProperty(prefix = "xcan.api-log", name = "enabled", matchIfMissing = true)
  public EventRepository<ApiLogEvent> apiLogEventRepository(ApiLogEventRemote apiLogEventRemote,
      ObjectMapper objectMapper) {
    return new MemoryAndRemoteEventRepository<ApiLogEvent>(
        "ApiLogEvent", DEFAULT_MEMORY_CAPACITY,
        DEFAULT_SEND_BUFFER_CAPACITY, apiLogEventRemote, objectMapper);
  }

  @DependsOn("apiLogEventRepository")
  @Bean("apiLogEventListener")
  //@ConditionalOnProperty(prefix = "xcan.api-log", name = "enabled", matchIfMissing = true)
  public EventListener<ApiLogEvent> apiLogEventListener(
      EventRepository<ApiLogEvent> apiLogEventRepository) {
    return new OperationEventListener<>(apiLogEventRepository);
  }

  @DependsOn("apiLogEventListener")
  @Bean(EventSender.ApiLogQueue.QUEUE_NAME)
  //@ConditionalOnProperty(prefix = "xcan.api-log", name = "enabled", matchIfMissing = true)
  public DisruptorQueueManager<ApiLogEvent> apiLogEventDisruptorQueue(
      EventListener<ApiLogEvent> apiLogEventListener) {
    return DisruptorQueueFactory.createWorkPoolQueue(128 * 1024, true,
        new DefaultThreadFactory("xcanDisruptorQueueApiLogEvent", Thread.NORM_PRIORITY),
        apiLogEventListener);
  }

  @Bean
  //@ConditionalOnProperty(prefix = "xcan.api-log", name = "enabled", matchIfMissing = true)
  public FilterRegistrationBean<ApiLogFilter> registrationApiLogFilterBean(
      ApiLogProperties apiLogProperties,
      DisruptorQueueManager<ApiLogEvent> apiLogEventDisruptorQueue) {
    FilterRegistrationBean<ApiLogFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setName("apiLogFilter");
    registrationBean.setFilter(new ApiLogFilter(apiLogProperties, apiLogEventDisruptorQueue));
    registrationBean.setDispatcherTypes(DispatcherType.REQUEST);
    registrationBean.addUrlPatterns(RESOURCES);
    // Must be executed after RequestContextFilter(OrderedRequestContextFilter) to prevent being overwritten
    registrationBean.setOrder(REQUEST_WRAPPER_FILTER_MAX_ORDER - 98);
    return registrationBean;
  }

}
