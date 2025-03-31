package cloud.xcan.angus.core.utils;

import static cloud.xcan.angus.core.utils.CoreUtils.randomUUIDWithoutDelimiter;
import static cloud.xcan.angus.remote.ApiConstant.ECode.PROTOCOL_ERROR_CODE;
import static cloud.xcan.angus.remote.ApiConstant.EXT_EKEY_NAME;
import static cloud.xcan.angus.spec.SpecConstant.DEFAULT_ENCODING;
import static cloud.xcan.angus.spec.experimental.BizConstant.AuthKey.BEARER_TOKEN_TYPE;
import static cloud.xcan.angus.spec.experimental.BizConstant.Header.ACCESS_TOKEN;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.spring.SpringContextHolder;
import cloud.xcan.angus.core.spring.filter.MutableHttpServletRequest;
import cloud.xcan.angus.remote.ApiResult;
import cloud.xcan.angus.remote.message.CommSysException;
import cloud.xcan.angus.spec.experimental.BizConstant.AuthKey;
import cloud.xcan.angus.spec.experimental.BizConstant.Header;
import cloud.xcan.angus.spec.experimental.StandardCharsets;
import cloud.xcan.angus.spec.http.ContentType;
import cloud.xcan.angus.spec.http.HttpResponseHeader;
import cloud.xcan.angus.spec.locale.MessageHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;

@Slf4j
public class ServletUtils {

  private ServletUtils() { /* no instance */ }

  public static String getRequestId(HttpServletRequest request) {
    String requestId = request.getHeader(Header.REQUEST_ID);
    return StringUtils.isBlank(requestId) ? randomUUIDWithoutDelimiter() : requestId;
  }

  public static String getAndSetRequestId(MutableHttpServletRequest request) {
    String requestId = request.getHeader(Header.REQUEST_ID);
    if (StringUtils.isBlank(requestId)) {
      requestId = StringUtils.isBlank(requestId) ? randomUUIDWithoutDelimiter() : requestId;
      request.putHeader(Header.REQUEST_ID, requestId);
    }
    return requestId;
  }

  public static String getUserAgent(HttpServletRequest request) {
    String userAgent = request.getHeader(Header.USER_AGENT);
    return StringUtils.isBlank(userAgent) ? "" : userAgent;
  }

  public static String getAuthServiceCode(HttpServletRequest request) {
    String serviceCode = request.getParameter(AuthKey.AUTH_SERVICE_CODE);
    return StringUtils.isBlank(serviceCode) ? AuthKey.DEFAULT_AUTH_SERVICE_CODE
        : serviceCode.toUpperCase();
  }

  public static String getAuthorization(HttpServletRequest request) {
    String authorization = request.getHeader(Header.AUTHORIZATION);
    if (isEmpty(authorization)) {
      authorization = BEARER_TOKEN_TYPE + " " + request.getParameter(ACCESS_TOKEN);
    }
    return authorization;
  }

  @SneakyThrows
  public static String getDownloadFileName(HttpHeaders headers) {
    List<String> dispositions = headers.get(HttpResponseHeader.Content_Disposition.value);
    if (isEmpty(dispositions)) {
      return null;
    }
    String disposition = dispositions.get(0);
    String filename = disposition.substring(disposition.indexOf("filename=") + 10,
        disposition.length() - 1);
    filename = URLDecoder.decode(filename, DEFAULT_ENCODING);
    return filename;
  }

  public static void writeResourceToFile(Resource resource, File file) throws IOException {
    if (!file.exists()) {
      file.getParentFile().mkdirs();
      file.createNewFile();
    }
    InputStream inputStream = resource.getInputStream();
    FileOutputStream outputStream = new FileOutputStream(file);
    byte[] buffer = new byte[1024];
    int length;
    while ((length = inputStream.read(buffer)) > 0) {
      outputStream.write(buffer, 0, length);
    }
    inputStream.close();
    outputStream.close();
  }

  public static void writeApiResult(HttpServletResponse response, int status, String message,
      String eKey, Object[] messageArgs) throws ServletException {
    response.setHeader(Header.E_KEY, eKey);
    ApiResult<?> result = new ApiResult<>().setCode(PROTOCOL_ERROR_CODE)
        .setMsg(MessageHolder.message(message, messageArgs))
        .setExt(Map.of(EXT_EKEY_NAME, eKey));
    writeJsonUtf8Result(response, status, result);
  }

  public static void writeJsonUtf8Result(HttpServletResponse response, int status, Object result)
      throws ServletException {
    response.setCharacterEncoding(DEFAULT_ENCODING);
    response.setContentType(ContentType.TYPE_JSON_UTF8);
    response.setStatus(status);
    try {
      if (nonNull(result)) {
        response.getWriter().write(SpringContextHolder.getBean(ObjectMapper.class)
            .writeValueAsString(result));
        response.getWriter().flush();
      }
    } catch (Exception e) {
      throw new ServletException(e.getMessage());
    }
  }

  public static void buildSupportRangeDownload(
      int cacheAge, MediaType mediaType, String filename, long filesize, Date lastModified,
      InputStream inputStream, HttpServletRequest request, HttpServletResponse response) {
    String range = request.getHeader("range");
    if (ObjectUtils.isEmpty(range)) {
      doNotRangeDownload(cacheAge, mediaType, filename, filesize, lastModified, inputStream,
          response);
    } else {
      doRangeDownload(cacheAge, mediaType, filename, filesize, lastModified, inputStream, response,
          range);
    }
  }

  @NotNull
  public static ResponseEntity<Resource> buildDownloadResourceResponseEntity(
      int cacheAge, MediaType mediaType, File file) {
    BodyBuilder bodyBuilder = ResponseEntity.ok();
    // Enabled browser caching
    if (cacheAge > 0) {
      CacheControl cacheControl = CacheControl.maxAge(cacheAge, TimeUnit.SECONDS)
          .noTransform().mustRevalidate();
      bodyBuilder.cacheControl(cacheControl);
    }
    if (file.length() > 0) {
      bodyBuilder.contentLength(file.length());
    }
    InputStreamResource resource;
    try {
      resource = new InputStreamResource(new FileInputStream(file));
    } catch (FileNotFoundException e) {
      throw CommSysException.of(e.getMessage());
    }
    return bodyBuilder.contentType(mediaType)
        //.header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
        .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""
            + URLEncoder.encode(file.getName(), StandardCharsets.UTF_8) + "\"")
        .body(resource);
  }

  @NotNull
  public static ResponseEntity<Resource> buildDownloadResourceResponseEntity(
      int cacheAge, MediaType mediaType, String filename, long filesize,
      InputStreamResource resource) {
    BodyBuilder bodyBuilder = ResponseEntity.ok();
    // Enabled browser caching
    if (cacheAge > 0) {
      CacheControl cacheControl = CacheControl.maxAge(cacheAge, TimeUnit.SECONDS)
          .noTransform().mustRevalidate();
      bodyBuilder.cacheControl(cacheControl);
    }
    if (filesize > 0) {
      bodyBuilder.contentLength(filesize);
    }
    return bodyBuilder.contentType(mediaType)
        //.header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
        .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""
            + URLEncoder.encode(filename, StandardCharsets.UTF_8) + "\"")
        .body(resource);
  }

  private static void doRangeDownload(int cacheAge, MediaType mediaType, String filename,
      long filesize, Date lastModified, InputStream inputStream, HttpServletResponse response,
      String range) {
    String[] split = range.split("bytes=|-");
    long begin = 0;
    if (split.length >= 2) {
      begin = Long.parseLong(split[1]);
    }
    long end = filesize - 1;
    if (split.length >= 3) {
      end = Long.parseLong(split[2]);
    }
    long len = (end - begin) + 1;
    // Check if the request scope is legal
    if (end > filesize) {
      response.setStatus(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE.value());
      response.addHeader(HttpHeaders.CONTENT_RANGE, "bytes */" + filesize);
      return;
    }

    ServletOutputStream os = null;
    try {
      inputStream.skip(begin);
      response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
      response.setContentType(mediaType.toString());
      response.addHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
          HttpHeaders.CONTENT_DISPOSITION);
      response.addHeader(HttpHeaders.CONTENT_RANGE,
          "bytes " + begin + "-" + end + "/" + filesize);
      response.addHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(len));
      response.addHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
      response.addHeader(HttpHeaders.CACHE_CONTROL, "private");
      response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""
          + URLEncoder.encode(filename, StandardCharsets.UTF_8) + "\"");
      if (cacheAge > 0) {
        CacheControl cacheControl = CacheControl.maxAge(cacheAge, TimeUnit.SECONDS)
            .noTransform().mustRevalidate();
        response.addHeader(HttpHeaders.CACHE_CONTROL, cacheControl.getHeaderValue());
      }
      if (nonNull(lastModified)) {
        response.addHeader(HttpHeaders.LAST_MODIFIED,
            new SimpleDateFormat("EEE, d MMM yyyy hh:mm:ss Z", Locale.ENGLISH)
                .format(lastModified) + " GMT");
      }

      os = response.getOutputStream();
      byte[] buf = new byte[1024];
      while (len > 0) {
        inputStream.read(buf);
        long l = len > 1024 ? 1024 : len;
        os.write(buf, 0, (int) l);
        os.flush();
        len -= l;
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          log.error(e.getMessage(), e);
        }
      }
      if (os != null) {
        try {
          os.close();
        } catch (IOException e) {
          log.error(e.getMessage(), e);
        }
      }
    }
  }

  private static void doNotRangeDownload(int cacheAge, MediaType mediaType, String filename,
      long filesize,
      Date lastModified, InputStream inputStream, HttpServletResponse response) {
    ServletOutputStream os = null;
    try {
      response.setStatus(HttpServletResponse.SC_OK);
      response.setContentType(mediaType.toString());
      response.addHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
          HttpHeaders.CONTENT_DISPOSITION);
      response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""
          + URLEncoder.encode(filename, StandardCharsets.UTF_8) + "\"");
      if (cacheAge > 0) {
        CacheControl cacheControl = CacheControl.maxAge(cacheAge, TimeUnit.SECONDS)
            .noTransform().mustRevalidate();
        response.addHeader(HttpHeaders.CACHE_CONTROL, cacheControl.getHeaderValue());
      }
      if (nonNull(lastModified)) {
        response.addHeader(HttpHeaders.LAST_MODIFIED,
            new SimpleDateFormat("EEE, d MMM yyyy hh:mm:ss Z", Locale.ENGLISH)
                .format(lastModified) + " GMT");
      }
      long len = filesize;
      os = response.getOutputStream();
      byte[] buf = new byte[1024];
      while (len > 0) {
        inputStream.read(buf);
        long l = len > 1024 ? 1024 : len;
        os.write(buf, 0, (int) l);
        os.flush();
        len -= l;
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          log.error(e.getMessage(), e);
        }
      }
      if (os != null) {
        try {
          os.close();
        } catch (IOException e) {
          log.error(e.getMessage(), e);
        }
      }
    }
  }

}

