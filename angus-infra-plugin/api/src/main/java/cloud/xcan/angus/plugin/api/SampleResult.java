package cloud.xcan.angus.plugin.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 由 {@link Sampler} 在执行一个步骤后生成的不可变（通过构建器）结果。
 *
 * <p>使用 {@link SampleResult#builder()} 构建结果（或对于常见情况
 * 使用 {@link AbstractSampler#createResult()} / {@link AbstractSampler#createFailedResult(String, Throwable)}）：
 *
 * <pre>{@code
 * SampleResult result = SampleResult.builder()
 *     .success(true)
 *     .statusCode(200)
 *     .requestMethod("GET")
 *     .responseBody(body)
 *     .responseTimeMs(elapsed)
 *     .requestUrl(url)
 *     .build();
 * }</pre>
 */
public final class SampleResult {

    /** 此样本的逻辑名称（例如步骤名称或请求名称），用于按接口分组聚合。 */
    private final String sampleName;

    /** 该步骤是否无错误执行且所有断言都已通过。 */
    private final boolean success;

    /** HTTP（或协议等效的）状态代码；如果不适用则为 {@code 0}。 */
    private final int statusCode;

    /**
     * 请求方法（如 HTTP {@code GET}/{@code POST}）；非 HTTP 或未知时为 {@code null}。
     */
    private final String requestMethod;

    /**
     * 请求 URL（含查询串等）；非 HTTP 类插件或未知时为 {@code null}。
     */
    private final String requestUrl;

    /**
     * 请求头（名称大小写策略由采样器决定）；不可用时为空映射。
     */
    private final Map<String, String> requestHeaders;

    /**
     * 请求体文本（与线上发送内容一致；大文件/二进制等场景可能为 {@code null}）。
     */
    private final String requestBody;

    /** 原始响应体作为字符串（对于二进制响应可能为 {@code null}）。 */
    private final String responseBody;

    /** 通过标头名称（小写）键入的响应标头。 */
    private final Map<String, String> responseHeaders;

    /** 总往返时间（以毫秒为单位）。 */
    private final long responseTimeMs;

    /** 请求体中发送的字节数。 */
    private final long requestSizeBytes;

    /** 响应体中接收的字节数。 */
    private final long responseSizeBytes;

    /** 当 {@link #success} 为 {@code false} 时的人类可读错误消息。 */
    private final String errorMessage;

    /**
     * 机器可读错误代码（例如 {@code "TIMEOUT"}、{@code "CONNECTION_REFUSED"}）。
     * 没有错误时为 {@code null}。
     */
    private final String errorCode;

    /** 由采样器或 {@link PostProcessor} 评估的断言结果。 */
    private final List<AssertionResult> assertions;

    /**
     * 从响应中提取的变量（例如通过 JSONPath 或正则表达式提取器）。
     * 这些变量由引擎合并到 {@link SampleContext#getVariables()} 映射中。
     */
    private final Map<String, String> extractedVariables;

    /** 网络/操作调用的系统时间开始，单位为自纪元以来的毫秒数。 */
    private final long startTime;

    /** 网络/操作调用的系统时间结束，单位为自纪元以来的毫秒数。 */
    private final long endTime;

    /** 此结果记录时的时间戳，单位为自纪元以来的毫秒数。 */
    private final long timestamp;

    /**
     * 不符合标准字段的插件特定结果元数据
     * （例如浏览器 HAR、JDBC 行数、MQTT 消息 ID）。
     */
    private final Map<String, Object> metadata;

    private SampleResult(Builder builder) {
        this.sampleName         = builder.sampleName;
        this.success            = builder.success;
        this.statusCode         = builder.statusCode;
        this.requestMethod      = builder.requestMethod;
        this.requestUrl         = builder.requestUrl;
        this.requestHeaders     = builder.requestHeaders != null
                ? new HashMap<>(builder.requestHeaders) : new HashMap<>();
        this.requestBody        = builder.requestBody;
        this.responseBody       = builder.responseBody;
        this.responseHeaders    = builder.responseHeaders != null
                ? new HashMap<>(builder.responseHeaders) : new HashMap<>();
        this.responseTimeMs     = builder.responseTimeMs;
        this.requestSizeBytes   = builder.requestSizeBytes;
        this.responseSizeBytes  = builder.responseSizeBytes;
        this.errorMessage       = builder.errorMessage;
        this.errorCode          = builder.errorCode;
        this.assertions         = builder.assertions != null
                ? new ArrayList<>(builder.assertions) : new ArrayList<>();
        this.extractedVariables = builder.extractedVariables != null
                ? new HashMap<>(builder.extractedVariables) : new HashMap<>();
        this.startTime          = builder.startTime;
        this.endTime            = builder.endTime;
        this.timestamp          = builder.timestamp > 0 ? builder.timestamp : builder.startTime;
        this.metadata           = builder.metadata != null
                ? new HashMap<>(builder.metadata) : new HashMap<>();
    }

    // ==================== 访问器 ====================

    public String getSampleName()                          { return sampleName; }
    public boolean isSuccess()                             { return success; }
    public int getStatusCode()                             { return statusCode; }
    public String getRequestMethod()                       { return requestMethod; }
    public String getRequestUrl()                          { return requestUrl; }
    public Map<String, String> getRequestHeaders()         { return requestHeaders; }
    public String getRequestBody()                         { return requestBody; }
    public String getResponseBody()                        { return responseBody; }
    public Map<String, String> getResponseHeaders()        { return responseHeaders; }
    public long getResponseTimeMs()                        { return responseTimeMs; }
    public long getRequestSizeBytes()                      { return requestSizeBytes; }
    public long getResponseSizeBytes()                     { return responseSizeBytes; }
    public String getErrorMessage()                        { return errorMessage; }
    public String getErrorCode()                           { return errorCode; }
    public List<AssertionResult> getAssertions()           { return assertions; }
    public Map<String, String> getExtractedVariables()     { return extractedVariables; }
    public long getStartTime()                             { return startTime; }
    public long getEndTime()                               { return endTime; }
    public long getTimestamp()                             { return timestamp; }
    public Map<String, Object> getMetadata()               { return metadata; }

    /** 便利方法：当此结果中的所有断言都通过时返回 {@code true}。 */
    public boolean allAssertionsPassed() {
        return assertions.stream().allMatch(AssertionResult::passed);
    }

    /**
     * 返回一个携带指定 {@code sampleName} 的浅拷贝。
     *
     * <p>用于在编排器层面为采样器返回的结果赋予步骤名称，
     * 使得下游指标采集能够按接口名称分组聚合。
     *
     * @param name 样本名称（通常是步骤名称）
     * @return 新的 {@link SampleResult}，仅 {@code sampleName} 不同
     */
    public SampleResult withSampleName(String name) {
        return builder()
                .sampleName(name)
                .success(this.success)
                .statusCode(this.statusCode)
                .requestMethod(this.requestMethod)
                .requestUrl(this.requestUrl)
                .requestHeaders(this.requestHeaders)
                .requestBody(this.requestBody)
                .responseBody(this.responseBody)
                .responseHeaders(this.responseHeaders)
                .responseTimeMs(this.responseTimeMs)
                .requestSizeBytes(this.requestSizeBytes)
                .responseSizeBytes(this.responseSizeBytes)
                .errorMessage(this.errorMessage)
                .errorCode(this.errorCode)
                .assertions(this.assertions)
                .extractedVariables(this.extractedVariables)
                .startTime(this.startTime)
                .endTime(this.endTime)
                .timestamp(this.timestamp)
                .metadata(this.metadata)
                .build();
    }

    // ==================== 构建器 ====================

    /** 返回一个新的 {@link Builder} 用于构造 {@link SampleResult}。 */
    public static Builder builder() {
        return new Builder();
    }

    /** {@link SampleResult} 的流畅构建器。 */
    public static final class Builder {

        private String sampleName;
        private boolean success;
        private int statusCode;
        private String requestMethod;
        private String requestUrl;
        private Map<String, String> requestHeaders;
        private String requestBody;
        private String responseBody;
        private Map<String, String> responseHeaders;
        private long responseTimeMs;
        private long requestSizeBytes;
        private long responseSizeBytes;
        private String errorMessage;
        private String errorCode;
        private List<AssertionResult> assertions;
        private Map<String, String> extractedVariables;
        private long startTime;
        private long endTime;
        private long timestamp;
        private Map<String, Object> metadata;

        private Builder() {}

        public Builder sampleName(String sampleName)                                   { this.sampleName = sampleName; return this; }
        public Builder success(boolean success)                                        { this.success = success; return this; }
        public Builder statusCode(int statusCode)                                      { this.statusCode = statusCode; return this; }
        public Builder requestMethod(String requestMethod)                             { this.requestMethod = requestMethod; return this; }
        public Builder requestUrl(String requestUrl)                                   { this.requestUrl = requestUrl; return this; }
        public Builder requestHeaders(Map<String, String> requestHeaders)              { this.requestHeaders = requestHeaders; return this; }
        public Builder requestBody(String requestBody)                                 { this.requestBody = requestBody; return this; }
        public Builder responseBody(String responseBody)                               { this.responseBody = responseBody; return this; }
        public Builder responseHeaders(Map<String, String> responseHeaders)            { this.responseHeaders = responseHeaders; return this; }
        public Builder responseTimeMs(long responseTimeMs)                             { this.responseTimeMs = responseTimeMs; return this; }
        public Builder requestSizeBytes(long requestSizeBytes)                         { this.requestSizeBytes = requestSizeBytes; return this; }
        public Builder responseSizeBytes(long responseSizeBytes)                       { this.responseSizeBytes = responseSizeBytes; return this; }
        public Builder errorMessage(String errorMessage)                               { this.errorMessage = errorMessage; return this; }
        public Builder errorCode(String errorCode)                                     { this.errorCode = errorCode; return this; }
        public Builder assertions(List<AssertionResult> assertions)                    { this.assertions = assertions; return this; }
        public Builder extractedVariables(Map<String, String> extractedVariables)      { this.extractedVariables = extractedVariables; return this; }
        public Builder startTime(long startTime)                                       { this.startTime = startTime; return this; }
        public Builder endTime(long endTime)                                           { this.endTime = endTime; return this; }
        public Builder timestamp(long timestamp)                                       { this.timestamp = timestamp; return this; }
        public Builder metadata(Map<String, Object> metadata)                          { this.metadata = metadata; return this; }

        /**
         * 将单个断言结果附加到断言列表。
         *
         * @param assertion 要添加的断言
         * @return 此构建器
         */
        public Builder addAssertion(AssertionResult assertion) {
            if (this.assertions == null) {
                this.assertions = new ArrayList<>();
            }
            this.assertions.add(assertion);
            return this;
        }

        /** 构建 {@link SampleResult}。 */
        public SampleResult build() {
            return new SampleResult(this);
        }
    }
}
