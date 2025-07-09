package cloud.xcan.angus.web;

import cloud.xcan.angus.core.fegin.CustomErrorDecoder;
import cloud.xcan.angus.core.fegin.FilterQueryMapEncoder;
import cloud.xcan.angus.remote.client.FeignRemoteFactory;
import cloud.xcan.angus.remote.client.HttpBroadcastInvoker;
import cloud.xcan.angus.remote.client.ServiceDiscoveryHelper;
import cloud.xcan.angus.security.FeignInnerApiAuthInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Client;
import feign.Contract;
import feign.Feign;
import feign.Logger;
import feign.Request;
import feign.Response;
import feign.Retryer;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.net.URL;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(Feign.class)
@ImportAutoConfiguration(FeignClientsConfiguration.class)
@AutoConfigureBefore({FeignClientsConfiguration.class, FeignAutoConfiguration.class})
@AutoConfigureAfter({OkHttpFeignConfiguration.class})
public class FeignAutoConfigurer {

  @Bean
  public FeignRemoteFactory feignRemoteFactory(Client client, Encoder encoder, Decoder decoder,
      Contract contract, FeignInnerApiAuthInterceptor feignInnerApiAuthInterceptor) {
    return new FeignRemoteFactory(client, encoder, decoder, contract, feignInnerApiAuthInterceptor);
  }

  @Bean
  public ServiceDiscoveryHelper serviceDiscoveryHelper(DiscoveryClient discoveryClient) {
    return new ServiceDiscoveryHelper(discoveryClient);
  }

  @Bean
  public HttpBroadcastInvoker httpBroadcastInvoker(ServiceDiscoveryHelper serviceDiscoveryHelper) {
    return new HttpBroadcastInvoker(serviceDiscoveryHelper);
  }

  @Bean
  public Client client(OkHttpClient okHttpClient, LoadBalancerClient loadBalancer) {
    return new LoadBalancerFeignClient(loadBalancer, new feign.okhttp.OkHttpClient(okHttpClient));
  }

  @Bean
  public Feign.Builder feignBuilder(Client client,
      Encoder feignEncoder, Decoder feignDecoder, ErrorDecoder errorDecoder,
      Logger.Level feignLoggerLevel) {
    return Feign.builder()
        .client(client)
        .encoder(feignEncoder)
        .decoder(feignDecoder)
        .errorDecoder(errorDecoder)
        .logLevel(feignLoggerLevel)
        .queryMapEncoder(new FilterQueryMapEncoder())
        /* new Retryer.Default(): The maximum number of retry requests is 5(maxAttempts),
         * the initial interval time is 100ms(period), the next interval time increases by 1.5 times,
         * and the maximum interval time between retries is 1s(maxPeriod) */
        .retryer(Retryer.NEVER_RETRY);
  }

  @Profile({"local", "dev", "beta"})
  @Bean("feignLoggerLevel")
  public Logger.Level feignLoggerFull() {
    return Logger.Level.FULL;
  }

  @Profile({"prod"})
  @Bean("feignLoggerLevel")
  public Logger.Level feignLoggerBasic() {
    return Logger.Level.BASIC;
  }


  @Bean
  public Encoder feignEncoder(ObjectMapper objectMapper) {
    return new SpringEncoder(() -> new HttpMessageConverters(
        new MappingJackson2HttpMessageConverter(objectMapper)
    ));
  }

  @Bean
  public Decoder feignDecoder(ObjectMapper objectMapper) {
    return new SpringDecoder(() -> new HttpMessageConverters(
        new MappingJackson2HttpMessageConverter(objectMapper)
    ));
  }

  @Bean
  public ErrorDecoder errorDecoder(ObjectMapper objectMapper) {
    return new CustomErrorDecoder(objectMapper);
  }

  static class LoadBalancerFeignClient implements feign.Client {

    private final LoadBalancerClient loadBalancer;
    private final feign.Client delegate;

    LoadBalancerFeignClient(LoadBalancerClient loadBalancer, feign.Client delegate) {
      this.loadBalancer = loadBalancer;
      this.delegate = delegate;
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
      String serviceId = new URL(request.url()).getHost();
      ServiceInstance instance = loadBalancer.choose(serviceId);

      String reconstructedUrl = instance == null ? request.url() : request.url()
          .replace(serviceId, instance.getHost() + ":" + instance.getPort());
      Request newRequest = Request.create(
          request.httpMethod(),
          reconstructedUrl,
          request.headers(),
          request.body(),
          request.charset(),
          request.requestTemplate()
      );

      return delegate.execute(newRequest, options);
    }
  }
}
