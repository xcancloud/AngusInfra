package cloud.xcan.angus.web;

import static cloud.xcan.angus.spec.SpecConstant.UTF8;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.core.utils.GsonUtils;
import cloud.xcan.angus.remote.ApiResult;
import cloud.xcan.angus.spec.experimental.BizConstant.Header;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.support.FeignHttpClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Slf4j
@Configuration
//@ConditionalOnProperty({"feign.okhttp.enabled"})
public class OkHttpFeignConfiguration {

  private okhttp3.OkHttpClient okHttpClient;

  @Bean
  @ConditionalOnMissingBean
  public okhttp3.OkHttpClient.Builder okHttpClientBuilder() {
    return new okhttp3.OkHttpClient.Builder();
  }

  @Bean
  @ConditionalOnMissingBean({ConnectionPool.class})
  public ConnectionPool httpClientConnectionPool(FeignHttpClientProperties httpClientProperties) {
    int maxTotalConnections = httpClientProperties.getMaxConnections();
    long timeToLive = httpClientProperties.getTimeToLive();
    TimeUnit ttlUnit = httpClientProperties.getTimeToLiveUnit();
    return new ConnectionPool(maxTotalConnections, timeToLive, ttlUnit);
  }

  @Bean
  public okhttp3.OkHttpClient okHttpClient(okhttp3.OkHttpClient.Builder builder,
      ConnectionPool connectionPool, FeignHttpClientProperties httpClientProperties) {
    boolean followRedirects = httpClientProperties.isFollowRedirects();
    int connectTimeout = httpClientProperties.getConnectionTimeout();
    boolean disableSslValidation = httpClientProperties.isDisableSslValidation();
    Duration readTimeout = httpClientProperties.getOkHttp().getReadTimeout();
    List<Protocol> protocols = httpClientProperties.getOkHttp().getProtocols().stream()
        .map(Protocol::valueOf).collect(Collectors.toList());
    if (disableSslValidation) {
      this.disableSsl(builder);
    }
    this.okHttpClient = builder.connectTimeout(connectTimeout,
            TimeUnit.MILLISECONDS).followRedirects(followRedirects).readTimeout(readTimeout)
        .connectionPool(connectionPool)
        .addInterceptor(new BizExceptionInterceptor())
        .protocols(protocols).build();
    return this.okHttpClient;
  }

  private void disableSsl(okhttp3.OkHttpClient.Builder builder) {
    try {
      X509TrustManager disabledTrustManager = new DisableValidationTrustManager();
      TrustManager[] trustManagers = new TrustManager[]{disabledTrustManager};
      SSLContext sslContext = SSLContext.getInstance("SSL");
      sslContext.init((KeyManager[]) null, trustManagers, new SecureRandom());
      SSLSocketFactory disabledSSLSocketFactory = sslContext.getSocketFactory();
      builder.sslSocketFactory(disabledSSLSocketFactory, disabledTrustManager);
      builder.hostnameVerifier(new TrustAllHostnames());
    } catch (KeyManagementException | NoSuchAlgorithmException var6) {
      GeneralSecurityException e = var6;
      log.warn("Error setting SSLSocketFactory in OKHttpClient", e);
    }
  }

  @PreDestroy
  public void destroy() {
    if (this.okHttpClient != null) {
      this.okHttpClient.dispatcher().executorService().shutdown();
      this.okHttpClient.connectionPool().evictAll();
    }

  }


  static class BizExceptionInterceptor implements Interceptor {

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

  static class DisableValidationTrustManager implements X509TrustManager {

    DisableValidationTrustManager() {
    }

    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
    }

    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
    }

    public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[0];
    }
  }

  static class TrustAllHostnames implements HostnameVerifier {

    TrustAllHostnames() {
    }

    public boolean verify(String s, SSLSession sslSession) {
      return true;
    }
  }
}
