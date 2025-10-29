package cloud.xcan.angus.remote;

import static cloud.xcan.angus.remote.ApiConstant.ECode.BUSINESS_ERROR_CODE;
import static cloud.xcan.angus.remote.ApiConstant.EXT_EKEY_NAME;
import static cloud.xcan.angus.remote.ApiConstant.OK_CODE;
import static cloud.xcan.angus.remote.message.BizException.M.BIZ_ERROR;
import static cloud.xcan.angus.remote.message.SuccessResultMessage.M.OK_MSG;

import cloud.xcan.angus.remote.message.AbstractResultMessageException;
import cloud.xcan.angus.remote.message.BizException;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * <p>
 * Standard API response wrapper that provides a consistent structure for all API responses.
 * This class encapsulates response status, message, data payload, timestamp, and extensible metadata.
 * </p>
 *
 * <p>
 * Key features:
 * - Standardized response format across all APIs
 * - Type-safe data payload with generic support
 * - Extensible metadata through the ext field
 * - Built-in error handling and validation
 * - Fluent API for easy construction
 * - JSON serialization optimizations
 * </p>
 *
 * <p>
 * Usage examples:
 * <pre>
 * // Success response with data
 * ApiResult&lt;User&gt; result = ApiResult.success(user);
 *
 * // Error response
 * ApiResult&lt;?&gt; error = ApiResult.error("User not found");
 *
 * // Custom response with metadata
 * ApiResult&lt;List&lt;User&gt;&gt; response = ApiResult.success("Query completed", users)
 *     .setExt(Map.of("totalCount", 100, "pageSize", 20));
 * </pre>
 * </p>
 *
 * <p>
 * Thread Safety: This class is not thread-safe. Create separate instances for concurrent use.
 * </p>
 */
@Schema(description =
    """
        Represents a standard API response structure, providing status, message, data, timestamp, and extensible fields.

        Example usage:
        ```ts
        const result: ApiResult = {
          code: 'S',
          msg: 'Operation successful',
          data: { id: 1, name: 'test' },
          datetime: new Date(),
          ext: { traceId: 'abc123' }
        };
        ```"""
    , accessMode = AccessMode.READ_ONLY)
@Setter
@Getter
@Accessors(chain = true)
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResult<T> implements Serializable {

  private static final long serialVersionUID = 1L;

  @Schema(description = """
      Business status code of the API response.
      - 'S': Success
      - 'E0': Protocol error (bad request)
      - 'E1': Business error
      - 'E2': System error (server error)
      - 'E3': Quota error (unauthorized or insufficient quota)
      - 'XXX': Custom business codes (require further handling)"""
  )
  private String code;

  @Schema(description = "Message providing additional context, such as success or error details.")
  private String messages;

  @Schema(description = "Actual response data or error details.")
  private T data;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Schema(description = "Server processing timestamp (date-time string).")
  private Long timestamp;

  @Schema(description = "Extensible map for extra response information.")
  private Map<String, Object> extensions;

  /**
   * <p>
   * Default constructor creating a successful response with no data.
   * Initializes with success code, default message, and current timestamp.
   * </p>
   */
  public ApiResult() {
    this(OK_CODE, OK_MSG, null, null);
  }

  /**
   * <p>
   * Constructor for creating a successful response with data.
   * </p>
   *
   * @param data the response data payload
   */
  public ApiResult(T data) {
    this(OK_CODE, OK_MSG, data, null);
  }

  /**
   * <p>
   * Constructor for creating a response with specific code and message.
   * </p>
   *
   * @param code the response status code
   * @param messages the response message
   */
  public ApiResult(String code, String messages) {
    this(code, messages, null, null);
  }

  /**
   * <p>
   * Constructor for creating a response with code, message, and data.
   * </p>
   *
   * @param code the response status code
   * @param messages the response message
   * @param data the response data payload
   */
  public ApiResult(String code, String messages, T data) {
    this(code, messages, data, null);
  }

  /**
   * <p>
   * Full constructor for creating a response with all parameters.
   * </p>
   *
   * @param code the response status code
   * @param messages the response message
   * @param data the response data payload
   * @param extensions additional metadata map
   */
  public ApiResult(String code, String messages, T data, Map<String, Object> extensions) {
    this.code = Objects.requireNonNull(code, "Response code cannot be null");
    this.messages = messages; // Allow null messages
    this.data = data; // Allow null data
    this.timestamp = System.currentTimeMillis();
    this.extensions = extensions != null ? new HashMap<>(extensions) : new HashMap<>();
  }

  // Static factory methods for success responses

  /**
   * <p>
   * Creates a successful response with no data.
   * </p>
   *
   * @return a new successful ApiResult instance
   */
  public static ApiResult<?> success() {
    return new ApiResult<>();
  }

  /**
   * <p>
   * Creates a successful response with the specified data.
   * </p>
   *
   * @param data the response data payload
   * @param <T> the type of the data
   * @return a new successful ApiResult instance with data
   */
  public static <T> ApiResult<T> success(T data) {
    return new ApiResult<>(data);
  }

  /**
   * <p>
   * Creates a successful response with a custom message.
   * </p>
   *
   * @param msg the success message
   * @return a new successful ApiResult instance with custom message
   */
  public static ApiResult<?> success(String msg) {
    return new ApiResult<>(OK_CODE, msg, null);
  }

  /**
   * <p>
   * Creates a successful response with a custom message and data.
   * </p>
   *
   * @param msg the success message
   * @param data the response data payload
   * @param <T> the type of the data
   * @return a new successful ApiResult instance with message and data
   */
  public static <T> ApiResult<T> success(String msg, T data) {
    return new ApiResult<>(OK_CODE, msg, data);
  }

  // Static factory methods for error responses

  /**
   * <p>
   * Creates a generic business error response.
   * </p>
   *
   * @param <T> the type of the data
   * @return a new error ApiResult instance
   */
  public static <T> ApiResult<T> error() {
    return new ApiResult<>(BUSINESS_ERROR_CODE, BIZ_ERROR);
  }

  /**
   * <p>
   * Creates an error response with a custom message.
   * </p>
   *
   * @param msg the error message
   * @param <T> the type of the data
   * @return a new error ApiResult instance with custom message
   */
  public static <T> ApiResult<T> error(String msg) {
    return new ApiResult<>(BUSINESS_ERROR_CODE, msg);
  }

  /**
   * <p>
   * Creates an error response with custom code and message.
   * </p>
   *
   * @param code the error code
   * @param msg the error message
   * @param <T> the type of the data
   * @return a new error ApiResult instance with custom code and message
   */
  public static <T> ApiResult<T> error(String code, String msg) {
    return new ApiResult<>(code, msg);
  }

  /**
   * <p>
   * Creates an error response with code, message, and data.
   * </p>
   *
   * @param code the error code
   * @param msg the error message
   * @param data the error data payload
   * @param <T> the type of the data
   * @return a new error ApiResult instance with code, message, and data
   */
  public static <T> ApiResult<T> error(String code, String msg, T data) {
    return new ApiResult<>(code, msg, data);
  }

  /**
   * <p>
   * Creates an error response with all parameters.
   * </p>
   *
   * @param code the error code
   * @param msg the error message
   * @param data the error data payload
   * @param ext additional metadata map
   * @param <T> the type of the data
   * @return a new error ApiResult instance with all parameters
   */
  public static <T> ApiResult<T> error(String code, String msg, T data,
      Map<String, Object> ext) {
    return new ApiResult<>(code, msg, data, ext);
  }

  // Utility methods

  /**
   * <p>
   * Checks if this response represents a successful operation.
   * </p>
   *
   * @return true if the response code indicates success, false otherwise
   */
  @JsonIgnore
  public boolean isSuccess() {
    return OK_CODE.equals(this.code);
  }

  /**
   * <p>
   * Throws a BizException if this response is not successful.
   * This method allows for fluent error handling in method chains.
   * </p>
   *
   * @return this ApiResult instance if successful
   * @throws BizException if the response indicates an error
   */
  public ApiResult<T> orElseThrow() {
    if (isSuccess()) {
      return this;
    }
    throw BizException.of(this.code, this.getMessages());
  }

  /**
   * <p>
   * Throws the specified exception if this response is not successful.
   * </p>
   *
   * @param exception the exception to throw if not successful
   * @return this ApiResult instance if successful
   * @throws AbstractResultMessageException the provided exception if not successful
   */
  public ApiResult<T> orElseThrow(AbstractResultMessageException exception) {
    if (isSuccess()) {
      return this;
    }
    throw exception;
  }

  /**
   * <p>
   * Returns the data content if successful, otherwise throws a BizException.
   * This method is useful for extracting data from successful responses.
   * </p>
   *
   * @return the response data if successful
   * @throws BizException if the response indicates an error
   */
  public T orElseContentThrow() {
    if (isSuccess()) {
      return this.data;
    }
    throw BizException.of(this.code, this.getMessages());
  }

  /**
   * <p>
   * Returns the data content if successful, otherwise throws the specified exception.
   * </p>
   *
   * @param exception the exception to throw if not successful
   * @return the response data if successful
   * @throws AbstractResultMessageException the provided exception if not successful
   */
  public T orElseContentThrow(AbstractResultMessageException exception) {
    if (isSuccess()) {
      return this.data;
    }
    throw exception;
  }

  /**
   * <p>
   * Retrieves the error key from the extension metadata.
   * This is a convenience method for accessing standardized error keys.
   * </p>
   *
   * @return the error key string, or null if not present
   */
  @JsonIgnore
  public String getEKey() {
    if (this.extensions == null) {
      return null;
    }
    Object eKey = this.extensions.get(EXT_EKEY_NAME);
    return eKey != null ? String.valueOf(eKey) : null;
  }

  /**
   * <p>
   * Adds a key-value pair to the extension metadata.
   * This method provides a fluent API for adding metadata.
   * </p>
   *
   * @param key the metadata key
   * @param value the metadata value
   * @return this ApiResult instance for method chaining
   */
  public ApiResult<T> addExt(String key, Object value) {
    if (key != null) {
      if (this.extensions == null) {
        this.extensions = new HashMap<>();
      }
      this.extensions.put(key, value);
    }
    return this;
  }

  /**
   * <p>
   * Adds all entries from the provided map to the extension metadata.
   * </p>
   *
   * @param extMap the map of metadata to add
   * @return this ApiResult instance for method chaining
   */
  public ApiResult<T> addExtAll(Map<String, Object> extMap) {
    if (extMap != null && !extMap.isEmpty()) {
      if (this.extensions == null) {
        this.extensions = new HashMap<>();
      }
      this.extensions.putAll(extMap);
    }
    return this;
  }

  /**
   * <p>
   * Removes a key from the extension metadata.
   * </p>
   *
   * @param key the key to remove
   * @return this ApiResult instance for method chaining
   */
  public ApiResult<T> removeExt(String key) {
    if (this.extensions != null && key != null) {
      this.extensions.remove(key);
    }
    return this;
  }

  /**
   * <p>
   * Checks if the extension metadata contains the specified key.
   * </p>
   *
   * @param key the key to check
   * @return true if the key exists in the extension metadata, false otherwise
   */
  public boolean hasExt(String key) {
    return this.extensions != null && this.extensions.containsKey(key);
  }

  /**
   * <p>
   * Gets a value from the extension metadata with the specified type.
   * </p>
   *
   * @param key the metadata key
   * @param type the expected type of the value
   * @param <V> the type parameter
   * @return the value cast to the specified type, or null if not found or type mismatch
   */
  @SuppressWarnings("unchecked")
  public <V> V getExtensions(String key, Class<V> type) {
    if (this.extensions == null || key == null) {
      return null;
    }
    Object value = this.extensions.get(key);
    if (value != null && type.isInstance(value)) {
      return (V) value;
    }
    return null;
  }

  /**
   * <p>
   * Creates a copy of this ApiResult with a different data type.
   * This is useful for transforming the data payload while preserving other fields.
   * </p>
   *
   * @param newData the new data payload
   * @param <U> the type of the new data
   * @return a new ApiResult instance with the new data
   */
  public <U> ApiResult<U> withData(U newData) {
    return new ApiResult<>(this.code, this.messages, newData, this.extensions);
  }

  /**
   * <p>
   * Creates a copy of this ApiResult with a different message.
   * </p>
   *
   * @param newMsg the new message
   * @return a new ApiResult instance with the new message
   */
  public ApiResult<T> withMessage(String newMsg) {
    return new ApiResult<>(this.code, newMsg, this.data, this.extensions);
  }

  /**
   * <p>
   * Validates that this ApiResult has all required fields properly set.
   * </p>
   *
   * @throws IllegalStateException if any required field is invalid
   */
  public void validate() {
    if (this.code == null || this.code.trim().isEmpty()) {
      throw new IllegalStateException("Response code cannot be null or empty");
    }
    if (this.timestamp == null) {
      throw new IllegalStateException("Response datetime cannot be null");
    }
  }

  /**
   * <p>
   * Custom setter for the ext field that ensures the map is never null.
   * </p>
   *
   * @param extensions the extension metadata map
   * @return this ApiResult instance for method chaining
   */
  public ApiResult<T> setExtensions(Map<String, Object> extensions) {
    this.extensions = extensions != null ? new HashMap<>(extensions) : new HashMap<>();
    return this;
  }

  /**
   * <p>
   * Gets the extension metadata map, ensuring it's never null.
   * </p>
   *
   * @return the extension metadata map, never null
   */
  public Map<String, Object> getExtensions() {
    if (this.extensions == null) {
      this.extensions = new HashMap<>();
    }
    return this.extensions;
  }
}
