package cloud.xcan.angus.web;

import static cloud.xcan.angus.spec.SpecConstant.UTF8;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.core.fegin.CustomErrorDecoder;
import cloud.xcan.angus.core.fegin.FilterQueryMapEncoder;
import cloud.xcan.angus.core.utils.GsonUtils;
import cloud.xcan.angus.remote.ApiResult;
import cloud.xcan.angus.remote.client.DynamicFeignClient;
import cloud.xcan.angus.remote.client.FeignBroadcastInvoker;
import cloud.xcan.angus.remote.client.ServiceDiscoveryHelper;
import cloud.xcan.angus.security.FeignInnerApiAuthInterceptor;
import cloud.xcan.angus.spec.experimental.BizConstant.Header;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Client;
import feign.Contract;
import feign.Feign;
import feign.Logger;
import feign.Retryer;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(Feign.class)
@ImportAutoConfiguration(FeignClientsConfiguration.class)
@AutoConfigureBefore({FeignClientsConfiguration.class, FeignAutoConfiguration.class})
public class FeignAutoConfigurer {

  @Bean
  public DynamicFeignClient dynamicFeignClient(Client client, Encoder encoder, Decoder decoder,
      Contract contract, FeignInnerApiAuthInterceptor feignInnerApiAuthInterceptor) {
    return Feign.builder().client(client)
        .encoder(encoder).decoder(decoder).contract(contract)
        .requestInterceptor(feignInnerApiAuthInterceptor)
        .target(DynamicFeignClient.class, "dynamic-feign-client");
  }

  @Bean
  public ServiceDiscoveryHelper serviceDiscoveryHelper(DiscoveryClient discoveryClient) {
    return new ServiceDiscoveryHelper(discoveryClient);
  }

  @Bean
  public FeignBroadcastInvoker broadcastInvoker(DynamicFeignClient dynamicFeignClient,
      ServiceDiscoveryHelper serviceDiscoveryHelper) {
    return new FeignBroadcastInvoker(dynamicFeignClient, serviceDiscoveryHelper);
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
  public Feign.Builder feignBuilder(OkHttpClient client, Encoder feignEncoder,
      Decoder feignDecoder, ErrorDecoder errorDecoder, Logger.Level feignLoggerLevel) {
    return Feign.builder()
        .client(new feign.okhttp.OkHttpClient(client))
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

  @Bean
  public OkHttpClient client() {
    return new OkHttpClient().newBuilder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .connectionPool(new ConnectionPool(10, 5L, TimeUnit.MINUTES))
        .addInterceptor(new BizExceptionInterceptor()).build();
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
}


class BizExceptionInterceptor implements Interceptor {

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    Response response = chain.proceed(request);
    String eKey = response.headers().get(Header.E_KEY);
    // fix bug: Solve the serialization exception caused by http status code 200 but not entering the error decoder when the business is abnormal
    // The http status code is not 2xx and will be handled by CustomErrorDecoder
    if (response.code() >= HttpStatus.OK.value()
        && response.code() <= HttpStatus.IM_USED.value() && nonNull(eKey)) {
      ResponseBody body = response.body();
      if (nonNull(body)) {
        byte[] bodyBytes = body.bytes();
        ApiResult<?> apiResult = GsonUtils.fromJson(new String(bodyBytes, UTF8),
            ApiResult.class);
        throw BizException.of(apiResult.getCode(), apiResult.getMsg());
      }
    }
    return response;
  }


}
