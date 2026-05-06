package cloud.xcan.angus.plugin.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在 {@link Sampler} 实现类上声明插件元数据的注解。
 *
 * <p>引擎工具（注册表、CLI {@code angus plugin list}）在
 * 运行时读取此注解以显示关于已加载插件的人类可读信息。
 *
 * <pre>{@code
 * @PluginMetadata(
 *     name    = "my-http",
 *     version = "1.0.0",
 *     description = "Custom HTTP sampler with mTLS support",
 *     author  = "Acme Corp",
 *     tags    = {"http", "tls"}
 * )
 * public class MyHttpSampler extends AbstractSampler { ... }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PluginMetadata {

    /** 唯一的插件标识符。必须与 {@link Sampler#getName()} 匹配。 */
    String name();

    /** SemVer 兼容版本字符串（例如 {@code "1.2.3"}）。 */
    String version();

    /** 插件的简短人类可读描述。 */
    String description() default "";

    /** 插件作者或组织。 */
    String author() default "";

    /** 用于过滤/发现的可选分类标签。 */
    String[] tags() default {};
}
