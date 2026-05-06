package cloud.xcan.angus.plugin.api;

/**
 * DevBridge 长连接调试通道中，Sampler 用来把服务端推送 / 上行帧 / 调试日志 / 最终结果
 * 回传给客户端的回调。实现方由 DevBridge 注入，Sampler 不应自行持久化引用。
 *
 * <h2>生命周期</h2>
 * <ol>
 *   <li>DevBridge 在 WebSocket 连接建立并校验 {@code start} 信令后构造 {@link StreamEmitter} 并
 *       调用 {@link StreamingSampler#stream(SampleContext, StreamEmitter)}；</li>
 *   <li>Sampler 内建立与目标协议（SSE/WS/gRPC/MQTT 等）的真实连接，把每条事件通过
 *       {@link #emit(Direction, Object)} 发回；必要时用 {@link #log(String, String)}
 *       写调试日志；</li>
 *   <li>最终（正常结束或异常）Sampler 调用 {@link #close(SampleResult)} 或 {@link #error(String, String)}
 *       通知客户端。此后 emitter 不再可用。</li>
 * </ol>
 *
 * <p>实现必须保证线程安全：Sampler 可能在多个回调线程（协议库的 IO 线程）同时调用 emit。
 *
 * @since 2.0.0
 */
public interface StreamEmitter {

    /** 帧方向：{@link #IN} 服务端 → 客户端；{@link #OUT} 客户端 → 服务端回放。 */
    enum Direction { IN, OUT }

    /** 客户端连接是否仍然打开。Sampler 在长循环中应用于短路退出。 */
    boolean isOpen();

    /**
     * 推送一条事件帧。frame 将被 JSON 序列化后下发到客户端。
     *
     * @param direction 帧方向
     * @param frame     任意可被 Jackson 序列化的对象（建议使用 Map 或 Record）
     */
    void emit(Direction direction, Object frame);

    /** 推送一条调试日志；level 建议使用 {@code DEBUG | INFO | WARN | ERROR}。 */
    void log(String level, String message);

    /**
     * 正常结束：下发一条 {@code end} 帧并关闭连接。
     *
     * @param finalResult 最终采样结果；可为 {@code null}
     */
    void close(SampleResult finalResult);

    /**
     * 异常结束：下发一条 {@code error} 帧并关闭连接。
     *
     * @param code    机器可读错误码
     * @param message 人类可读错误描述
     */
    void error(String code, String message);
}
