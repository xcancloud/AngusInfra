package cloud.xcan.angus.spec.http;


import static cloud.xcan.angus.spec.http.HttpRequestHeader.Content_Encoding;
import static cloud.xcan.angus.spec.utils.ObjectUtils.appendParameter;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isBlank;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import cloud.xcan.angus.api.pojo.Pair;
import cloud.xcan.angus.api.pojo.auth.SimpleHttpAuth;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.zip.GZIPOutputStream;
import lombok.SneakyThrows;

/**
 * A general-purpose interface for controlling how implementations perform HTTP calls for various
 * purposes. This interface can be used to inject more advanced customization like SSL verification,
 * key loading, etc. without requiring further additions to registry configurations.
 */
public interface HttpSender {

  Response send(Request request) throws Throwable;

  default Request.Builder post(String uri) {
    return newRequest(uri).withMethod(HttpMethod.POST);
  }

  default Request.Builder head(String uri) {
    return newRequest(uri).withMethod(HttpMethod.HEAD);
  }

  default Request.Builder put(String uri) {
    return newRequest(uri).withMethod(HttpMethod.PUT);
  }

  default Request.Builder get(String uri) {
    return newRequest(uri).withMethod(HttpMethod.GET);
  }

  default Request.Builder delete(String uri) {
    return newRequest(uri).withMethod(HttpMethod.DELETE);
  }

  default Request.Builder patch(String uri) {
    return newRequest(uri).withMethod(HttpMethod.PATCH);
  }

  default Request.Builder options(String uri) {
    return newRequest(uri).withMethod(HttpMethod.OPTIONS);
  }

  default Request.Builder newRequest(String uri) {
    return new Request.Builder(uri, this);
  }

  class Request {

    private final URL url;
    private final byte[] entity;
    private final HttpMethod httpMethod;
    private final Map<String, String> requestHeaders;
    private final List<SimpleHttpAuth> auths;

    public Request(URL url, byte[] entity, HttpMethod httpMethod,
        Map<String, String> requestHeaders, List<SimpleHttpAuth> auths) {
      this.url = url;
      this.entity = entity;
      this.httpMethod = httpMethod;
      this.requestHeaders = requestHeaders;
      this.auths = auths;
    }

    public URL getUrl() {
      return url;
    }

    public byte[] getEntity() {
      return entity;
    }

    public HttpMethod getMethod() {
      return httpMethod;
    }

    public Map<String, String> getRequestHeaders() {
      return requestHeaders;
    }

    public List<SimpleHttpAuth> getAuths() {
      return auths;
    }

    public static Builder build(String uri, HttpSender sender) {
      return new Builder(uri, sender);
    }

    @Override
    public String toString() {
      StringBuilder printed = new StringBuilder(httpMethod.toString()).append(' ')
          .append(url.toString()).append("\n");
      if (isNotEmpty(requestHeaders)) {
        printed.append("-------------Request Headers---------------\n");
        for (Entry<String, String> entry : requestHeaders.entrySet()) {
          printed.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
      }
      String requestBody = entity == null || entity.length == 0
          ? "<no request body>" : new String(entity);
      printed.append("-------------Request Body---------------------\n").append(requestBody);
      return printed.toString();
    }

    public static class Builder {

      private static final String APPLICATION_JSON = ContentType.TYPE_JSON;
      private static final String TEXT_PLAIN = ContentType.TYPE_PLAIN;

      private URL url;
      private final HttpSender sender;

      private byte[] entity = new byte[0];
      private HttpMethod httpMethod;
      private final Map<String, String> requestHeaders = new LinkedHashMap<>();
      private final List<SimpleHttpAuth> auths = new ArrayList<>();

      Builder(String uri, HttpSender sender) {
        try {
          uri = ObjectUtils.cleanUrl(uri);
          this.url = URI.create(uri).toURL();
        } catch (MalformedURLException ex) {
          throw new UncheckedIOException(ex);
        }
        this.sender = sender;
      }

      /**
       * Add a header to the request.
       *
       * @param name  The name of the header.
       * @param value The value of the header.
       * @return This request builder.
       */
      public final Builder withHeader(String name, String value) {
        requestHeaders.put(name, value);
        return this;
      }

      /**
       * Add a header to the request.
       *
       * @param headers The pair of header name and value.
       * @return This request builder.
       */
      public final Builder withHeader(List<Pair<String, String>> headers) {
        for (Pair<String, String> header : headers) {
          requestHeaders.put(header.getKey(), header.getValue());
        }
        return this;
      }

      @SneakyThrows
      public final Builder withAuths(List<SimpleHttpAuth> auths0) {
        if (isEmpty(auths0)) {
          return this;
        }
        for (SimpleHttpAuth auth : auths0) {
          if (auth.getIn().isHeader()) {
            requestHeaders.put(auth.getKeyName(), auth.getValue());
          } else {
            url = appendParameter(url.toURI(), auth.getKeyName(), auth.getValue())
                .toURL();
          }
        }
        return this;
      }

      @SneakyThrows
      public final Builder withAuth(SimpleHttpAuth auth) {
        if (isEmpty(auth)) {
          return this;
        }
        if (auth.getIn().isHeader()) {
          requestHeaders.put(auth.getKeyName(), auth.getValue());
        } else {
          url = appendParameter(url.toURI(), auth.getKeyName(), auth.getValue()).toURL();
        }
        return this;
      }

      /**
       * If user and password are non-empty, set basic authentication on the request.
       *
       * @param user     A user name, if available.
       * @param password A password, if available.
       * @return This request builder.
       */
      public final Builder withBasicAuthentication(String user, String password) {
        if (isNotBlank(user)) {
          String encoded = Base64.getEncoder().encodeToString((user.trim() + ":"
              + (password == null ? "" : password.trim())).getBytes(StandardCharsets.UTF_8));
          withAuthentication("Basic", encoded);
        }
        return this;
      }

      /**
       * Configures the {@code Authorization} HTTP header with the given type and credentials. The
       * format will be:
       * <pre>{@code Authorization: <type> <credentials>}</pre>
       * No encoding will be performed on the {@code credentials}, so if the authentication scheme
       * expects {@code credentials} to be encoded, encode them before passing them to this
       * httpMethod.
       *
       * @param type        authentication type
       * @param credentials authentication credentials
       * @return This request builder.
       * @since 1.8.0
       */
      public final Builder withAuthentication(String type, String credentials) {
        if (isNotBlank(credentials)) {
          withHeader("Authorization", type + " " + credentials);
        }
        return this;
      }

      /**
       * Set the request body as JSON content type.
       *
       * @param content The request body.
       * @return This request builder.
       */
      public final Builder withJsonContent(String content) {
        return withContent(APPLICATION_JSON, content);
      }

      /**
       * Set the request body as plain text content type.
       *
       * @param content The request body.
       * @return This request builder.
       */
      public final Builder withPlainText(String content) {
        return withContent(TEXT_PLAIN, content);
      }

      /**
       * Set the request body.
       *
       * @param type    The value of the "Content-Type" header to add.
       * @param content The request body.
       * @return This request builder.
       */
      public final Builder withContent(String type, String content) {
        return withContent(type, content.getBytes(StandardCharsets.UTF_8));
      }

      /**
       * Set the request body.
       *
       * @param type    The value of the "Content-Type" header to add.
       * @param content The request body.
       * @return This request builder.
       */
      public final Builder withContent(String type, byte[] content) {
        if (Objects.nonNull(type)) {
          withHeader("Content-Type", type);
        }
        entity = content;
        return this;
      }

      /**
       * Add header to accept {@code application/json} data.
       *
       * @return This request builder.
       */
      public Builder acceptJson() {
        return accept(APPLICATION_JSON);
      }

      /**
       * Add accept header.
       *
       * @param type The value of the "Accept" header to add.
       * @return This request builder.
       */
      public Builder accept(String type) {
        return withHeader("Accept", type);
      }

      /**
       * Set the request httpMethod.
       *
       * @param httpMethod An HTTP httpMethod.
       * @return This request builder.
       */
      public final Builder withMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
        return this;
      }

      /**
       * Add a "Content-Encoding" header of "gzip" and compress the request body.
       *
       * @return This request builder.
       * @throws IOException If compression fails.
       */
      public final Builder compress() throws IOException {
        withHeader(Content_Encoding.value, HttpHeaderValues.GZIP);
        this.entity = gzip(entity);
        return this;
      }

      /**
       * Add a "Content-Encoding" header of "gzip" and compress the request body when the supplied
       * condition is true.
       *
       * @param when Condition that governs when to compress the request body.
       * @return This request builder.
       * @throws IOException If compression fails.
       */
      public final Builder compressWhen(Supplier<Boolean> when) throws IOException {
        if (when.get()) {
          return compress();
        }
        return this;
      }

      private static byte[] gzip(byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
        try (GZIPOutputStream out = new GZIPOutputStream(bos)) {
          out.write(data);
        }
        return bos.toByteArray();
      }

      public final Builder print() {
        System.out.println(new Request(url, entity, httpMethod, requestHeaders, auths));
        return this;
      }

      public Response send() throws Throwable {
        return sender.send(new Request(url, entity, httpMethod, requestHeaders, auths));
      }
    }
  }

  class Response {

    public static final String NO_RESPONSE_BODY = "<no response body>";
    private final int code;
    private final List<Pair<String, String>> headers;
    private final String body;
    private final InputStream bodyIS;

    public Response(int code, List<Pair<String, String>> headers,
        String body, InputStream bodyIS) {
      this.code = code;
      this.headers = headers;
      this.body = isBlank(body) ? NO_RESPONSE_BODY : body;
      this.bodyIS = bodyIS;
    }

    public int code() {
      return code;
    }

    public List<Pair<String, String>> headers() {
      return headers;
    }

    public String body() {
      return body;
    }

    public InputStream bodyIS() {
      return bodyIS;
    }

    public Response onSuccess(Consumer<Response> onSuccess) {
      switch (HttpStatusSeries.valueOf(code)) {
        case INFORMATIONAL:
        case SUCCESS:
          onSuccess.accept(this);
      }
      return this;
    }

    public Response onError(Consumer<Response> onError) {
      switch (HttpStatusSeries.valueOf(code)) {
        case CLIENT_ERROR:
        case SERVER_ERROR:
          onError.accept(this);
      }
      return this;
    }

    public boolean isSuccessful() {
      return switch (HttpStatusSeries.valueOf(code)) {
        case INFORMATIONAL, SUCCESS -> true;
        default -> false;
      };
    }
  }

}
