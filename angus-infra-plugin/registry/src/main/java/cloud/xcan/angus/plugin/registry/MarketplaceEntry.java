package cloud.xcan.angus.plugin.registry;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;

/**
 * 市场中远程可用插件的描述符。
 *
 * <p>这是发现格式——告诉客户端在安装前有哪些插件可供下载。
 *
 * @param id                 唯一插件标识符
 * @param name               显示名称
 * @param version            最新可用版本
 * @param description        单行摘要
 * @param author             插件作者/供应商
 * @param downloadUrl        插件 JAR 制品的下载 URL
 * @param supportedPlatforms 此插件支持的平台
 * @param tags               搜索标签
 * @param checksumSha256     可下载 JAR 的 SHA-256 校验和（用于完整性验证）
 * @since 2.0.0
 */
@JsonInclude(Include.NON_NULL)
public record MarketplaceEntry(
        String id,
        String name,
        String version,
        String description,
        String author,
        String downloadUrl,
        List<String> supportedPlatforms,
        List<String> tags,
        String checksumSha256
) {
    public MarketplaceEntry {
        supportedPlatforms = supportedPlatforms != null ? List.copyOf(supportedPlatforms) : List.of();
        tags = tags != null ? List.copyOf(tags) : List.of();
    }
}
