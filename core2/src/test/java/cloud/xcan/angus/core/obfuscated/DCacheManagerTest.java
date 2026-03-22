package cloud.xcan.angus.core.obfuscated;

import cloud.xcan.angus.api.obf.Obj0;
import cloud.xcan.angus.core.spring.SpringContextHolder;

public class DCacheManagerTest {

  /**
   * @see SpringContextHolder#getDCacheManager(String, String)
   */
  public static void main(String[] args) {
    System.out.println(Obj0.var0("cert/XCanTest.publicCert.keystore")); // pubKeyStore
    System.out.println(Obj0.var0("xcan@store@pub_cNui8V")); // pubKeyStorePass
    System.out.println(Obj0.var0("XCanTest.publicCert")); // pubCertAlis
    System.out.println(Obj0.var0(".435E9A3AB63ED118")); // keyPassSalt
  }

}
