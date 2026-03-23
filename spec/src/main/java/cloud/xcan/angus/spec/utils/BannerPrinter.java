package cloud.xcan.angus.spec.utils;

import cloud.xcan.angus.spec.experimental.Assert;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * BannerPrinter implementation that prints from a source text {@link URL}.
 *
 * @author XiaoLong Liu
 */
@Slf4j
public class BannerPrinter {

  public static final String DECORATION_CHAR = "#############################################";

  /**
   * @deprecated Use {@link #DECORATION_CHAR} (typo in name).
   */
  @Deprecated
  public static final String DECORATION_CHARD = DECORATION_CHAR;

  private final URL resource;

  public BannerPrinter() {
    this(null);
  }

  public BannerPrinter(URL resource) {
    if (Objects.nonNull(resource)) {
      this.resource = resource;
    } else {
      this.resource = getClass().getClassLoader().getResource("banner.txt");
    }
    Assert.assertNotNull(this.resource, "Resource must not be null");
  }

  public void printBanner() {
    try (var in = this.resource.openStream()) {
      String banner = StreamUtils.copyToString(in, StandardCharsets.UTF_8);
      log.info(DECORATION_CHAR + "\n" + banner);
      System.out.println(DECORATION_CHAR + "\n" + banner);
    } catch (Exception ex) {
      log.warn(String.format("Application banner not printable: %s (%s: '%s')", this.resource,
          ex.getClass(), ex.getMessage()), ex);
    }
  }

  public void printConsoleBanner() {
    try (var in = this.resource.openStream()) {
      String banner = StreamUtils.copyToString(in, StandardCharsets.UTF_8);
      System.out.println(banner);
    } catch (Exception ignored) {
      // optional console banner
    }
  }

  public URL getBanner() {
    return this.resource;
  }
}
