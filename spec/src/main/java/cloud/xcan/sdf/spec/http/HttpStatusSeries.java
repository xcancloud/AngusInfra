package cloud.xcan.sdf.spec.http;

/**
 * Enumeration of HTTP status series.
 * <p>
 * The class of HTTP status. Simplification of {@code io.netty.handler.codec.http.HttpStatusClass}.
 */
public enum HttpStatusSeries {

  INFORMATIONAL(100, 200),
  SUCCESS(200, 300),
  REDIRECTION(300, 400),
  CLIENT_ERROR(400, 500),
  SERVER_ERROR(500, 600),
  UNKNOWN(0, 0) {
    @Override
    public boolean contains(int code) {
      return code < 100 || code >= 600;
    }
  };

  /**
   * Return the {@code HttpStatusSeries} enum constant for the supplied status code.
   */
  public static HttpStatusSeries valueOf(int statusCode) {
    HttpStatusSeries series = resolve(statusCode);
    if (series == null) {
      throw new IllegalArgumentException("No matching constant for [" + statusCode + "]");
    }
    return series;
  }

  private final int min;
  private final int max;

  HttpStatusSeries(int min, int max) {
    this.min = min;
    this.max = max;
  }

  public static String getStatusType(int code) {
    if (code >= 100 && code < 200) {
      return "1xx";
    } else if (code >= 200 && code < 300) {
      return "2xx";
    } else if (code >= 300 && code < 400) {
      return "3xx";
    } else if (code >= 400 && code < 500) {
      return "4xx";
    } else if (code >= 500 && code < 600) {
      return "5xx";
    }
    return String.valueOf(code);
  }

  /**
   * Returns {@code true} if and only if the specified HTTP status code falls into this class.
   */
  public boolean contains(int code) {
    return code >= min && code < max;
  }

  public boolean isSuccess() {
    return this.equals(SUCCESS);
  }

  /**
   * Return the {@code HttpStatusSeries} enum constant for the supplied {@code HttpStatus}.
   *
   * @param status a standard HTTP status enum constant
   * @return the {@code HttpStatusSeries} enum constant for the supplied {@code HttpStatus}
   * @deprecated as of 5.3, in favor of invoking {@link HttpStatus#series()} directly
   */
  @Deprecated
  public static HttpStatusSeries valueOf(HttpStatus status) {
    return status.series;
  }

  /**
   * Resolve the given status code to an {@code HttpStatusSeries}, if possible.
   */
  public static HttpStatusSeries resolve(int statusCode) {
    for (HttpStatusSeries series : values()) {
      if (series.contains(statusCode)) {
        return series;
      }
    }
    return null;
  }

}
