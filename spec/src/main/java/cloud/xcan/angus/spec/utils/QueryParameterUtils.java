package cloud.xcan.angus.spec.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class QueryParameterUtils {

  private QueryParameterUtils() {
    /* no instance */
  }

  public static String buildQueryString(Map<String, Deque<String>> params) {
    StringBuilder sb = new StringBuilder();
    boolean first = true;

    for (Entry<String, Deque<String>> entry : params.entrySet()) {
      if (entry.getValue().isEmpty()) {
        if (first) {
          first = false;
        } else {
          sb.append('&');
        }
        sb.append(entry.getKey());
        sb.append('=');
      } else {
        for (String val : entry.getValue()) {
          if (first) {
            first = false;
          } else {
            sb.append('&');
          }
          sb.append(entry.getKey());
          sb.append('=');
          sb.append(val);
        }
      }
    }
    return sb.toString();
  }

  public static Map<String, Deque<String>> parseQueryString(String newQueryString,
      String encoding) {
    Map<String, Deque<String>> newQueryParameters = new LinkedHashMap<>();
    int startPos = 0;
    int equalPos = -1;
    boolean needsDecode = false;

    for (int i = 0; i < newQueryString.length(); ++i) {
      char c = newQueryString.charAt(i);
      if (c == '=' && equalPos == -1) {
        equalPos = i;
      } else if (c == '&') {
        handleQueryParameter(newQueryString, newQueryParameters, startPos, equalPos, i,
            encoding, needsDecode);
        needsDecode = false;
        startPos = i + 1;
        equalPos = -1;
      } else if ((c == '%' || c == '+') && encoding != null) {
        needsDecode = true;
      }
    }
    if (startPos != newQueryString.length()) {
      handleQueryParameter(newQueryString, newQueryParameters, startPos, equalPos,
          newQueryString.length(), encoding, needsDecode);
    }
    return newQueryParameters;
  }

  private static void handleQueryParameter(String newQueryString,
      Map<String, Deque<String>> newQueryParameters, int startPos, int equalPos, int i,
      String encoding, boolean needsDecode) {
    String value = "", key;
    if (equalPos == -1) {
      key = decodeParam(newQueryString, startPos, i, encoding, needsDecode);
    } else {
      key = decodeParam(newQueryString, startPos, equalPos, encoding, needsDecode);
      value = decodeParam(newQueryString, equalPos + 1, i, encoding, needsDecode);
    }

    Deque<String> queue = newQueryParameters.get(key);
    if (queue == null) {
      newQueryParameters.put(key, queue = new ArrayDeque<>(1));
    }
    if (value != null) {
      queue.add(value);
    }
  }

  private static String decodeParam(String newQueryString, int startPos, int equalPos,
      String encoding, boolean needsDecode) {
    String key;
    if (needsDecode) {
      try {
        key = URLDecoder.decode(newQueryString.substring(startPos, equalPos), encoding);
      } catch (UnsupportedEncodingException var7) {
        key = newQueryString.substring(startPos, equalPos);
      }
    } else {
      key = newQueryString.substring(startPos, equalPos);
    }
    return key;
  }

  public static Map<String, Deque<String>> mergeQueryParametersWithNewQueryString(
      Map<String, Deque<String>> queryParameters, String newQueryString, String encoding) {
    Map<String, Deque<String>> newQueryParameters = parseQueryString(newQueryString, encoding);
    for (Entry<String, Deque<String>> entry : queryParameters.entrySet()) {
      if (!newQueryParameters.containsKey(entry.getKey())) {
        newQueryParameters.put(entry.getKey(), new ArrayDeque<>(entry.getValue()));
      } else {
        newQueryParameters.get(entry.getKey()).addAll(entry.getValue());
      }
    }
    return newQueryParameters;
  }
}
