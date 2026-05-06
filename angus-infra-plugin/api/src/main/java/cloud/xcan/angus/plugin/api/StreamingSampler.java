package cloud.xcan.angus.plugin.api;

/**
 * 可选扩展接口。{@link Sampler} 实现若希望支持 <b>长连接 / 流式调试</b>（HTTP SSE、
 * WebSocket、gRPC streaming、MQTT 订阅等），可同时实现本接口；DevBridge 的
 * {@code POST /bridge/stream/{name}} 端点会优先路由到 {@link #stream(SampleContext, StreamEmitter)}。
 *
 * <p>不实现本接口的 Sampler 仍可被 {@code /bridge/stream/{name}} 以 <em>降级模式</em> 调用：
 * DevBridge 会执行一次 {@link Sampler#sample(SampleContext)}，把结果包一条 {@code end} 帧
 * 下发后关闭连接。
 *
 * <h2>线程模型</h2>
 * <ul>
 *   <li>{@link #stream} 在 DevBridge 的虚拟线程上调用，可同步阻塞直到连接结束或 {@link StreamEmitter#isOpen()}
 *       返回 {@code false}；</li>
 *   <li>{@link #onClientFrame} 由 DevBridge 在接收到客户端上行 {@code send} 信令时回调，可能与
 *       {@link #stream} 不在同一线程，实现必须线程安全。</li>
 * </ul>
 *
 * @since 2.0.0
 */
public interface StreamingSampler extends Sampler {

    /**
     * 执行长连接采样。实现内：
     * <ol>
     *   <li>根据 {@link SampleContext#getStepConfig()} 建立与目标系统的真实连接；</li>
     *   <li>每次收到服务端推送或发送出上行帧时，通过 {@code emitter.emit(...)} 回传给客户端；</li>
     *   <li>正常结束调用 {@link StreamEmitter#close(SampleResult)}；
     *       出错调用 {@link StreamEmitter#error(String, String)}；</li>
     *   <li>长轮询中应周期性检查 {@link StreamEmitter#isOpen()}，客户端关闭后尽快退出。</li>
     * </ol>
     *
     * @throws Exception 任何未捕获异常由 DevBridge 转为 {@code error} 帧后关闭连接
     */
    void stream(SampleContext context, StreamEmitter emitter) throws Exception;

    /**
     * 处理客户端上行帧（双向协议专用，如 WebSocket / gRPC bidi / MQTT publish）。
     * 默认实现忽略所有上行帧。
     *
     * @param context 与 {@link #stream} 同一会话的上下文
     * @param payload 客户端 JSON 信令 {@code send.payload} 反序列化后的对象
     */
    default void onClientFrame(SampleContext context, Object payload) {
        // no-op: 单向协议默认忽略
    }

    /**
     * 是否支持流式调用。可用于同一 Sampler 动态关闭流式能力（例如未加载相应依赖时）。
     * 返回 {@code false} 时 DevBridge 将走降级分支。
     */
    default boolean supportsStreaming() {
        return true;
    }
}
