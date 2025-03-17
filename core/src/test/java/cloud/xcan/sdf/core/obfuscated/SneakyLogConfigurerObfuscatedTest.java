package cloud.xcan.sdf.core.obfuscated;

import cloud.xcan.sdf.api.obf.Obj0;

public class SneakyLogConfigurerObfuscatedTest {

  public static void main(String[] args) {
    // AppWorkspaceInit
    System.out.println(Obj0.var0("Failed to create home directory"));
    System.out.println(Obj0.var0("Failed to create application workspace directory and exit, cause: {}"));
    System.out.println(Obj0.var0("TERM_THROW_INFO"));
    System.out.println(Obj0.var0("TERM_THROW_WARN"));
    System.out.println(Obj0.var0("TERM_THROW_ERROR"));

    System.out.println("----");

    // SneakyLogConfigurer
    System.out.println(Obj0.var0("SELECT count(*) FROM user0"));
    System.out.println(Obj0.var0("SELECT count(*) FROM exec WHERE status = 'RUNNING'"));
    System.out.println(Obj0.var0("SELECT count(*) FROM exec_debug WHERE status = 'RUNNING'"));
    System.out.println(Obj0.var0("TERM_THROW_INFO"));
    System.out.println(Obj0.var0("TERM_THROW_WARN"));
    System.out.println(Obj0.var0("TERM_THROW_ERROR"));
    System.out.println(Obj0.var0("Internal application error: LE-0901"));
    System.out.println(Obj0.var0("Internal application error: LE-0902"));
    System.out.println(Obj0.var0("Internal application error: LE-0903"));

    System.out.println("----");

    // EnumStoreInMemory
    System.out.println(Obj0.var0("dCacheManager"));
    System.out.println(Obj0.var0("Internal application error: LE-0909"));

    System.out.println("----");

    // SchedulerAutoConfigurer
    System.out.println(Obj0.var0("SDF-Scheduler"));

    System.out.println("----");

    // PluginAutoConfigurer
    System.out.println(Obj0.var0("descriptor"));
    System.out.println(Obj0.var0("path"));
    System.out.println(Obj0.var0("state"));
    System.out.println(Obj0.var0("runtimeMode"));

    System.out.println("----");

    // ApplicationReadyListener
    System.out.println(Obj0.var0("Application i18n resources configuration success"));
    System.out.println(Obj0.var0("Application started successfully [PID="));
    System.out.println(Obj0.var0(" and Http(s) port "));
    System.out.println(Obj0.var0(" is ready"));

  }
}
