package cloud.xcan.angus.web;

import static cloud.xcan.angus.spec.SpecConstant.UTF8;

import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.core.fegin.CustomErrorDecoder;
import cloud.xcan.angus.core.fegin.FilterQueryMapEncoder;
import cloud.xcan.angus.core.utils.GsonUtils;
import cloud.xcan.angus.remote.ApiResult;
import cloud.xcan.angus.spec.annotations.NonNullable;
import cloud.xcan.angus.spec.experimental.BizConstant.Header;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.Logger;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(Feign.class)
@AutoConfigureBefore(FeignAutoConfiguration.class)
public class FeignAutoConfigurer {

  /*@Bean
  @Primary
  public Encoder feignEncoder(ObjectMapper objectMapper) {
    return new JacksonEncoder(objectMapper);
  }

  @Bean
  @Primary
  public Decoder feignDecoder(ObjectMapper objectMapper) {
    return new JacksonDecoder(objectMapper);
  }*/

  @Bean
  @ConditionalOnMissingClass
  public ErrorDecoder errorDecoder(ObjectMapper objectMapper) {
    return new CustomErrorDecoder(objectMapper);
  }

  @Profile({"local", "dev", "beta"})
  @Bean
  Logger.Level feignLoggerFull() {
    return Logger.Level.FULL;
  }

  @Profile({"pre", "prod"})
  @Bean
  Logger.Level feignLoggerBasic() {
    return Logger.Level.BASIC;
  }

  @Bean
  public Feign.Builder feignBuilder() {
    return Feign.builder()
        .queryMapEncoder(new FilterQueryMapEncoder())
        /* new Retryer.Default(): The maximum number of retry requests is 5(maxAttempts),
         * the initial interval time is 100ms(period), the next interval time increases by 1.5 times,
         * and the maximum interval time between retries is 1s(maxPeriod) */
        .retryer(Retryer.NEVER_RETRY);
  }

  @Bean
  @ConditionalOnMissingClass
  public OkHttpClient client() {
    return new OkHttpClient().newBuilder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .connectionPool(new ConnectionPool(10, 5L, TimeUnit.MINUTES))
        .addInterceptor(new BizExceptionInterceptor()).build();
  }
}

class BizExceptionInterceptor implements Interceptor {

  @NotNull
  @Override
  public Response intercept(@NonNullable Chain chain) throws IOException {
    Request request = chain.request();
    Response response = chain.proceed(request);
    String eKey = response.headers().get(Header.E_KEY);
    // fix bug: Solve the serialization exception caused by http status code 200 but not entering the error decoder when the business is abnormal
    // The http status code is not 2xx and will be handled by CustomErrorDecoder
    if (response.code() >= HttpStatus.OK.value() && response.code() <= HttpStatus.ALREADY_REPORTED
        .value() && Objects.nonNull(eKey)) {
      ResponseBody body = response.body();
      if (Objects.nonNull(body)) {
        byte[] bodyBytes = body.bytes();
        ApiResult<?> apiResult = GsonUtils.fromJson(new String(bodyBytes, UTF8),
            ApiResult.class);
        throw BizException.of(apiResult.getCode(), apiResult.getMsg());
      }
    }
    return response;
  }


}
