package cloud.xcan.angus.spec.http;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.pojo.Pair;
import cloud.xcan.angus.spec.utils.IOUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link HttpURLConnection}-based {@link HttpSender}.
 */
@Slf4j
public class HttpUrlConnectionSender implements HttpSender {

  private static final String TRUST_ALL = String.format("%s.trustAll", HttpSender.class.getName());
  private static final ConnectionConfigurator CONNECTION_CONFIGURATOR = createConnectionConfigurator();
  private static final String ACCEPT_HEADER_VALUE = "application/json, application/yaml, */*";
  private static final String USER_AGENT_HEADER_VALUE = "SDF-HttpSender";

  private static final int DEFAULT_CONNECT_TIMEOUT_MS = 3000;
  private static final int DEFAULT_READ_TIMEOUT_MS = 30000;

  private final int connectTimeoutMs;
  private final int readTimeoutMs;
  private final Proxy proxy;

  /**
   * Creates a sender with the specified timeouts but uses the default proxy settings.
   *
   * @param connectTimeout connect timeout when establishing a connection
   * @param readTimeout    read timeout when receiving a response
   */
  public HttpUrlConnectionSender(Duration connectTimeout, Duration readTimeout) {
    this(connectTimeout, readTimeout, null);
  }

  /**
   * Creates a sender with the specified timeouts and proxy settings.
   *
   * @param connectTimeout connect timeout when establishing a connection
   * @param readTimeout    read timeout when receiving a response
   * @param proxy          proxy to use when establishing a connection
   */
  public HttpUrlConnectionSender(Duration connectTimeout, Duration readTimeout, Proxy proxy) {
    this.connectTimeoutMs = (int) connectTimeout.toMillis();
    this.readTimeoutMs = (int) readTimeout.toMillis();
    this.proxy = proxy;
  }

  /**
   * Use the default timeouts and proxy settings for the sender.
   */
  public HttpUrlConnectionSender() {
    this.connectTimeoutMs = DEFAULT_CONNECT_TIMEOUT_MS;
    this.readTimeoutMs = DEFAULT_READ_TIMEOUT_MS;
    this.proxy = null;
  }

  @Override
  public Response send(Request request) throws IOException {
    HttpURLConnection conn = null;
    try {
      if (proxy != null) {
        conn = (HttpURLConnection) request.getUrl().openConnection(proxy);
      } else {
        conn = (HttpURLConnection) request.getUrl().openConnection();
      }
      CONNECTION_CONFIGURATOR.process(conn);
      conn.setConnectTimeout(connectTimeoutMs);
      conn.setReadTimeout(readTimeoutMs);
      HttpMethod httpMethod = isNull(request.getMethod()) ? HttpMethod.GET : request.getMethod();
      conn.setRequestMethod(httpMethod.name());

      conn.setRequestProperty(HttpRequestHeader.Accept.value, ACCEPT_HEADER_VALUE);
      conn.setRequestProperty(HttpRequestHeader.User_Agent.value, USER_AGENT_HEADER_VALUE);

      for (Map.Entry<String, String> header : request.getRequestHeaders().entrySet()) {
        conn.setRequestProperty(header.getKey(), header.getValue());
      }

      if (httpMethod != HttpMethod.GET && nonNull(request.getEntity())) {
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
          os.write(request.getEntity());
          os.flush();
        } catch (Exception e) {
          log.error("Handle OutputStream exception: ", e);
        }
      }

      int status = conn.getResponseCode();

      String body = null;
      InputStream bodyIS = null;
      try {
        if (conn.getErrorStream() != null) {
          // conn.getErrorStream() is closed in usage.
          byte[] data = conn.getErrorStream().readAllBytes();
          bodyIS = new ByteArrayInputStream(data);
          body = IOUtils.toString(new ByteArrayInputStream(data));
        } else if (conn.getInputStream() != null) {
          // conn.getInputStream() is closed in usage.
          byte[] data = conn.getInputStream().readAllBytes();
          bodyIS = new ByteArrayInputStream(data);
          body = IOUtils.toString(new ByteArrayInputStream(data));
        }
      } catch (Exception e) {
        log.error("Handle InputStream exception: ", e);
      }

      List<Pair<String, String>> headers = new ArrayList<>();
      try {
        Map<String, List<String>> headersMap = conn.getHeaderFields();
        for (String headerName : headersMap.keySet()) {
          for (String value : headersMap.get(headerName)) {
            headers.add(new Pair<>(headerName, value));
          }
        }
      } catch (Exception ignored) {
      }

      return new Response(status, headers, body, bodyIS);
    } finally {
      try {
        if (conn != null) {
          conn.disconnect();
        }
      } catch (Exception ignore) {
      }
    }
  }

  private static ConnectionConfigurator createConnectionConfigurator() {
    if (Boolean.parseBoolean(System.getProperty(TRUST_ALL, "true"))) {
      try {
        // Create a trust manager that does not validate certificate chains
        final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
          @Override
          public X509Certificate[] getAcceptedIssuers() {
            return null;
          }

          @Override
          public void checkClientTrusted(X509Certificate[] certs, String authType) {
          }

          @Override
          public void checkServerTrusted(X509Certificate[] certs, String authType) {
          }
        }};

        // Install the all-trusting trust manager
        final SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        final SSLSocketFactory sf = sc.getSocketFactory();
        // Create all-trusting host name verifier
        final HostnameVerifier trustAllNames = new HostnameVerifier() {
          @Override
          public boolean verify(String hostname, SSLSession session) {
            return true;
          }
        };

        return new ConnectionConfigurator() {
          @Override
          public void process(URLConnection connection) {
            if (connection instanceof HttpsURLConnection) {
              final HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
              httpsConnection.setSSLSocketFactory(sf);
              httpsConnection.setHostnameVerifier(trustAllNames);
            }
          }
        };
      } catch (NoSuchAlgorithmException | KeyManagementException e) {
        log.error("Not Supported", e);
      }
    }
    return new ConnectionConfigurator() {
      @Override
      public void process(URLConnection connection) {
        // Do nothing
      }
    };
  }
}

interface ConnectionConfigurator {

  void process(URLConnection connection);
}
