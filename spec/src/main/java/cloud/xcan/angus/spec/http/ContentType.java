package cloud.xcan.angus.spec.http;

/**
 * Http Content Type
 */
public interface ContentType {

  String CONTENT_TYPE_KEY = HttpRequestHeader.Content_Type.value;
  String TYPE_DEFAULT = "application/json; charset=UTF-8";
  String TYPE_HTML = "text/html; charset=UTF-8";
  String TYPE_JSON = "application/json";
  String TYPE_JSON_UTF8 = "application/json; charset=UTF-8";
  String TYPE_JAVASCRIPT = "application/javascript";
  String APPLICATION_XML = "application/xml";
  String TYPE_PLAIN = "text/plain";
  String TYPE_PLAIN_UTF8 = "text/plain; charset=UTF-8";
  String TYPE_XML = "text/xml";
  String TYPE_OCTET_STREAM = "application/octet-stream";
  String TYPE_FORM_DATA = "multipart/form-data";
  String TYPE_FORM_URL_ENCODED = "application/x-www-form-urlencoded";
  String SUB_TYPE_JSON = "json";
  String SUB_TYPE_FORM_URL_ENCODED = "x-www-form-urlencoded";
  String SUB_TYPE_XML = "xml";
  String SUB_TYPE_TEXT = "text";

}
