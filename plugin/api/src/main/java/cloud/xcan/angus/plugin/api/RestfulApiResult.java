package cloud.xcan.angus.plugin.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Schema(
        description = """
                Standard API response wrapper used across REST endpoints.

                It carries a short business status code, a human-friendly message, optional
                payload data, a server timestamp, and an extensible metadata map (ext).
                The structure is intentionally generic so it can represent both success
                and error responses in a consistent format.
                """,
        accessMode = Schema.AccessMode.READ_ONLY
)
@Setter
@Getter
@Accessors(chain = true)
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestfulApiResult<T> implements Serializable {

    public static final String OK_CODE = "S";
    public static final String OK_MSG = "Success";

    public static final String PROTOCOL_ERROR_CODE = "'E0'";
    public static final String PROTOCOL_ERROR_MESSAGE = "Protocol error (bad request)";

    public static final String BUSINESS_ERROR_CODE = "E1";
    public static final String BUSINESS_ERROR_MESSAGE = "Business Error";

    public static final String SYSTEM_ERROR_CODE = "E2";
    public static final String SYSTEM_ERROR_MESSAGE = "System Error";

    // Conventional key for error code in ext map
    public static final String EXT_EKEY_NAME = "eKey";

    @Schema(description = "Business result code. Typical values:" +
            " 'S' = success, " +
            " 'E1' = business error," +
            " 'E2' = system error.")
    private String code;

    @Schema(description = "Human-readable message describing the result or error.")
    private String msg;

    @Schema(description = "Payload of the response; may be null for actions that return no data.")
    private T data;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Server timestamp when the response was created (formatted string).")
    private LocalDateTime datetime;

    @Schema(description = "Extensible key/value map for additional metadata (tracing, error keys, etc.).")
    private Map<String, Object> ext;

    /**
     * Create a default successful result with no data and the current timestamp.
     */
    public RestfulApiResult() {
        this(OK_CODE, OK_MSG, null, null);
    }

    /**
     * Create a successful result with the given data payload.
     *
     * @param data response payload
     */
    public RestfulApiResult(T data) {
        this(OK_CODE, OK_MSG, data, null);
    }

    /**
     * Create a result with explicit business code and message.
     *
     * @param code short business code
     * @param msg  human-readable message
     */
    public RestfulApiResult(String code, String msg) {
        this(code, msg, null, null);
    }

    /**
     * Create a result with code, message and payload.
     *
     * @param code business code
     * @param msg  message
     * @param data payload
     */
    public RestfulApiResult(String code, String msg, T data) {
        this(code, msg, data, null);
    }

    /**
     * Full constructor. Copies the ext map to avoid external mutation.
     *
     * @param code business code (non-null)
     * @param msg  message (may be null)
     * @param data payload (may be null)
     * @param ext  additional metadata (copied if non-null)
     */
    public RestfulApiResult(String code, String msg, T data, Map<String, Object> ext) {
        this.code = Objects.requireNonNull(code, "Response code cannot be null");
        this.msg = msg;
        this.data = data;
        this.datetime = LocalDateTime.now();
        this.ext = ext != null ? new HashMap<>(ext) : new HashMap<>();
    }

    // ----------------- convenience static factories -----------------

    public static RestfulApiResult<?> success() {
        return new RestfulApiResult<>();
    }

    public static <T> RestfulApiResult<T> success(T data) {
        return new RestfulApiResult<>(data);
    }

    public static RestfulApiResult<?> success(String msg) {
        return new RestfulApiResult<>(OK_CODE, msg, null);
    }

    public static <T> RestfulApiResult<T> success(String msg, T data) {
        return new RestfulApiResult<>(OK_CODE, msg, data);
    }

    public static <T> RestfulApiResult<T> error() {
        return new RestfulApiResult<>(BUSINESS_ERROR_CODE, BUSINESS_ERROR_MESSAGE);
    }

    public static <T> RestfulApiResult<T> error(String msg) {
        return new RestfulApiResult<>(BUSINESS_ERROR_CODE, msg);
    }

    public static <T> RestfulApiResult<T> error(String code, String msg) {
        return new RestfulApiResult<>(code, msg);
    }

    public static <T> RestfulApiResult<T> error(String code, String msg, T data) {
        return new RestfulApiResult<>(code, msg, data);
    }

    public static <T> RestfulApiResult<T> error(String code, String msg, T data, Map<String, Object> ext) {
        return new RestfulApiResult<>(code, msg, data, ext);
    }

    // ----------------- utility helpers -----------------

    /**
     * Returns true when this result represents a successful business response.
     */
    @JsonIgnore
    public boolean isSuccess() {
        return OK_CODE.equals(this.code);
    }

    /**
     * Convenience accessor for a standardized error key stored under ext[EXT_EKEY_NAME].
     *
     * @return the error key as string, or null if not present
     */
    @JsonIgnore
    public String getEKey() {
        if (this.ext == null) {
            return null;
        }
        Object eKey = this.ext.get(EXT_EKEY_NAME);
        return eKey != null ? String.valueOf(eKey) : null;
    }

    /**
     * Add a single metadata entry to the ext map. This method ensures the ext map is initialized.
     *
     * @param key   metadata key
     * @param value metadata value
     * @return this result for chaining
     */
    public RestfulApiResult<T> addExt(String key, Object value) {
        if (key != null) {
            if (this.ext == null) {
                this.ext = new HashMap<>();
            }
            this.ext.put(key, value);
        }
        return this;
    }

    /**
     * Merge entries from a map into ext. Null/empty maps are ignored.
     *
     * @param extMap map of metadata entries
     * @return this result for chaining
     */
    public RestfulApiResult<T> addExtAll(Map<String, Object> extMap) {
        if (extMap != null && !extMap.isEmpty()) {
            if (this.ext == null) {
                this.ext = new HashMap<>();
            }
            this.ext.putAll(extMap);
        }
        return this;
    }

    /**
     * Remove a key from ext if present.
     *
     * @param key metadata key to remove
     * @return this result for chaining
     */
    public RestfulApiResult<T> removeExt(String key) {
        if (this.ext != null && key != null) {
            this.ext.remove(key);
        }
        return this;
    }

    /**
     * Check whether ext contains a key.
     */
    public boolean hasExt(String key) {
        return this.ext != null && this.ext.containsKey(key);
    }

    /**
     * Get a typed ext value if present and matching the requested type.
     *
     * @param key  metadata key
     * @param type expected class of the value
     * @param <V>  expected type
     * @return the value cast to V, or null if absent/mismatched
     */
    @SuppressWarnings("unchecked")
    public <V> V getExt(String key, Class<V> type) {
        if (this.ext == null || key == null) {
            return null;
        }
        Object value = this.ext.get(key);
        if (type.isInstance(value)) {
            return (V) value;
        }
        return null;
    }

    /**
     * Create a copy of this result with a different data payload but preserving other fields.
     */
    public <U> RestfulApiResult<U> withData(U newData) {
        return new RestfulApiResult<>(this.code, this.msg, newData, this.ext);
    }

    /**
     * Create a copy with a different message.
     */
    public RestfulApiResult<T> withMessage(String newMsg) {
        return new RestfulApiResult<>(this.code, newMsg, this.data, this.ext);
    }

    /**
     * Basic validation to ensure required fields exist.
     *
     * @throws IllegalStateException when required fields are missing
     */
    public void validate() {
        if (this.code == null || this.code.trim().isEmpty()) {
            throw new IllegalStateException("Response code cannot be null or empty");
        }
        if (this.datetime == null) {
            throw new IllegalStateException("Response datetime cannot be null");
        }
    }

    /**
     * Set ext map (defensive copy). Returns this for chaining.
     */
    public RestfulApiResult<T> setExt(Map<String, Object> ext) {
        this.ext = ext != null ? new HashMap<>(ext) : new HashMap<>();
        return this;
    }

    /**
     * Get ext map, ensuring a non-null map is returned.
     */
    public Map<String, Object> getExt() {
        if (this.ext == null) {
            this.ext = new HashMap<>();
        }
        return this.ext;
    }
}
