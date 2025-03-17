package cloud.xcan.sdf.spec.utils;

import cloud.xcan.sdf.spec.experimental.Assert;
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

  public static final String DECORATION_CHARD = "#############################################";

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
    try {
      String banner = StreamUtils.copyToString(this.resource.openStream(), StandardCharsets.UTF_8);
      log.info(DECORATION_CHARD + "\n" + banner);
    } catch (Exception ex) {
      log.warn(String.format("Application banner not printable: %s (%s: '%s')", this.resource,
          ex.getClass(), ex.getMessage()), ex);
    }
  }

  public void printConsoleBanner() {
    try {
      String banner = StreamUtils.copyToString(this.resource.openStream(), StandardCharsets.UTF_8);
      System.out.println(banner);
    } catch (Exception ex) {
      // NOOP
    }
  }

  public URL getBanner(){
    return this.resource;
  }
}
