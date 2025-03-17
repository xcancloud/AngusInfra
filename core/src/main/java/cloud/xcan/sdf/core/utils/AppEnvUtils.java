package cloud.xcan.sdf.core.utils;

import static cloud.xcan.sdf.core.app.verx.VerxRegister.cacheManager;

public class AppEnvUtils {

  public static boolean APP_INIT_READY = false;

  public static void initSneakyLogDir() throws Exception {
    System.setProperty(
        new cloud.xcan.sdf.api.obf.Str0(new long[]{0x24F939DF58E7C793L, 0x8864F93068A31006L, 0x3C6E3C516A053288L})
            .toString() /* => "TERM_THROW_INFO" */,
        String.valueOf(cacheManager().getCon().getCam()));
    System.setProperty(
        new cloud.xcan.sdf.api.obf.Str0(new long[]{0xED1C49103C70360DL, 0xED45EC2573340FF7L, 0x51D6F23651EA754DL})
            .toString() /* => "TERM_THROW_WARN" */,
        String.valueOf(cacheManager().getCon().getTta()));
    System.setProperty(
        new cloud.xcan.sdf.api.obf.Str0(new long[]{0x683C1E9BAF325DFEL, 0x7FE9A981AF2BEBA8L, 0xAC1B6B7146356A21L})
            .toString() /* => "TERM_THROW_ERROR" */,
        String.valueOf(cacheManager().getCon().getTta()));
  }
}
