package cloud.xcan.angus.core.app.verx;

import static cloud.xcan.angus.core.spring.SpringContextHolder.isCloudService;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import cloud.xcan.angus.core.utils.SpringAppDirUtils;
import cloud.xcan.angus.spec.annotations.DoInFuture;
import java.io.File;
import java.io.InputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.typelevel.v.Str0;

/**
 * Read the license file according to the configuration path first, and read it from the class path
 * if not configured.
 * <p>
 * 1. Privatization edition configuration storage location reading;
 * <p>
 * 2. The cloud service edition development environment is read from the project class path;
 * <p>
 * 3. The prod environment of the cloud service edition is read according to the path environment
 * variable set by Jenkins.
 *
 * <pre>
 * // Private Edition Configuration Example
 * final String SUBJECT = "AngusTester License Authorization";
 * final String licenseKeypass = "4JL8-G8HZ-NK2M-QKNA-XQX0.435E9A3AB63ED118";
 * final String licenseFile = "4JL8-G8HZ-NK2M-QKNA-XQX0.priv.lic";
 *
 * String keyStore = "/workdata/keystore/XCanTest.privateKey.keystore";
 * String licensePath = "/workdata/keystore/backup/" + licenseFile;
 * String alias = "XCanTest.privateKey";
 * String storepass = "xcan@key@priv_Abv10vY";
 * String keypass = "xcan@key@priv_Abv10vY";
 * </pre>
 */
@ConfigurationProperties(prefix = "xcan.verx")
final public class VerxProperties {

  /**
   * pubKeyStore
   */
  private String key;
  /**
   * pubKeyStorePass
   */
  private String build;
  /**
   * licensePath
   */
  private String path;
  /**
   * pubCertAlis
   */
  private String name;

  public VerxProperties() {
  }

  /**
   * => getPubKeyStore() => Lic path.
   * <p>
   * The public certificate store does not distinguish between versions and is consistent.
   */
  public String getKey() {
    // Read in the manual setting
    if (isNotBlank(this.key)) {
      return this.key;
    }
    // Use system config (Used by privatization edition)
    String key = System.getProperty(
        new Str0(new long[]{0x888D01092E563260L, 0x554F6A24FDDE268FL, 0xB2AF743B126C6C61L})
            .toString() /* => "xcan.verx.key" */);
    if (isNotBlank(key)) {
      // this.key = key; // For safe.
      return key;
    }

    // Use built-in keystore when on the cloud service
    key = getCloudServiceKey();
    if (isNotBlank(key)) {
      // this.key = key; // For safe.
      return key;
    }
    return null;
  }

  public void setKey(String key) {
    this.key = key;
  }

  /**
   * => getPubKeyStorePass()
   * <p>
   * The public certificate store password does not distinguish between versions and is consistent.
   */
  public String getBuild() {
    return isEmpty(this.build) ? new Str0(
        new long[]{0x276ECA6301FA289L, 0xCF84F4E9289C5F5EL, 0xE760E986D5A90EDEL,
            0x8E0B6844D2345BB4L})
        .toString() /* => "xcan@store@pub_cNui8V" */ : this.build;
  }

  public void setBuild(String build) {
    this.build = build;
  }

  /**
   * => getPubCertAlis()
   */
  public String getName() {
    // Read in the manual setting
    if (isNotBlank(this.name)) {
      return this.name;
    }

    // Use system config (Used by privatization edition)
    String name = System.getProperty(
        new Str0(new long[]{0x5D393F52BA94DEEAL, 0x4908E4329D8F92AL, 0xC6F42DB658421A96L})
            .toString() /* => "xcan.verx.name" */);
    if (isNotBlank(name)) {
      // this.name = name; // For safe.
      return name;
    }

    // Use built-in cert when on the cloud service
    // this.name = getCloudServiceName(); // For safe.
    return getCloudServiceName();
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * => getLicensePath()
   * <p>
   * Different certificates are used for cloud service environment and non cloud service
   * environment, and the purchased installation certificate is used during privatization.
   */
  public String getPath() {
    // Read in the manual setting
    if (isNotEmpty(this.path)) {
      return this.path;
    }

    // Use system config (Used by privatization edition)
    String path = System.getProperty(
        new Str0(new long[]{0xE43D8059F3E530D6L, 0xC4C054AE10BC12C2L, 0xBF424E83BD48C347L})
            .toString() /* => "xcan.verx.path" */);
    if (isNotBlank(path)) {
      // this.key = key; // For safe.
      return path;
    }

    path = System.getProperty(new Str0(new long[]{0x52FD1AF4732F21B5L, 0x394F21575D501104L,
        0xA9430B736CC7284BL, 0x9765D14C9FF99083L}).toString() /* => "MAIN_LICENSE_PATH" */);
    // The privatized license file comes from the customer's purchase and installation
    if (isNotEmpty(path) || !isCloudService()) {
      return path;
    }

    // Use built-in certificates and passwords when on the cloud service
    return getCloudServicePath(path);
  }

  public void setPath(String path) {
    this.path = path;
  }

  @DoInFuture("Use jenkins to set the key in the prod environment")
  @Nullable
  private String getCloudServiceKey() {
    String tempPath = null;
    File tmpFile = new File(new SpringAppDirUtils().getTmpDir() + randomAlphanumeric(32) + ".dat");
    InputStream stream = null;
    try {
     /* if (SpringContextHolder.isProd()) {
        stream = VerxProperties.class.getResourceAsStream("/" + new Str0(
            new long[]{0x6CFC50D9F5B5BCEL, 0x8ED763F0CAA00A70L, 0x8E853B09DE81E35L,
                0x279EB25AE03C303AL, 0x17C74FEA13A1AFD5L, 0xF9409114E50A21BL})
            .toString()) *//* => "cert/XCanCloud.publicCert.keystore" *//*;
      } else {
        stream = VerxProperties.class.getResourceAsStream("/" + new Str0(
            new long[]{0x42B9520297DB8146L, 0x660000C5D1B8D76DL, 0x4BCA6450B7B6CC71L,
                0xEE1C5A51C62718B9L, 0xC61DBBC74634D727L, 0xDC07A702C0366209L})
            .toString()) *//* => "cert/XCanTest.publicCert.keystore" *//*;
      }*/
      // Warning: Development and prod environments use the same configuration
      stream = VerxProperties.class.getResourceAsStream("/" + new Str0(
          new long[]{0x42B9520297DB8146L, 0x660000C5D1B8D76DL, 0x4BCA6450B7B6CC71L,
              0xEE1C5A51C62718B9L, 0xC61DBBC74634D727L, 0xDC07A702C0366209L})
          .toString()) /* => "cert/XCanTest.publicCert.keystore" */;
      FileUtils.copyInputStreamToFile(stream, tmpFile);
      tempPath = tmpFile.getAbsolutePath();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (null != stream) {
        IOUtils.closeQuietly(stream);
      }
    }
    return tempPath;
  }

  @DoInFuture("Use jenkins to set the name in the prod environment")
  @NotNull
  private String getCloudServiceName() {
   /* if (SpringContextHolder.isProd()) {
      return new Str0(new long[]{0x5D49827238F2EE35L, 0xEB009B2480ED608AL, 0x705B3D3EC4161998L,
          0x93EBABAE75134064L}).toString() *//* => "XCanCloud.publicCert" *//*;
    }
    return new Str0(
        new long[]{0xBD6A2DCCCC42F388L, 0xB62445B360A00717L, 0x26DD774A8A4F1FEL,
            0x3234E4FA1BBEC35L})
        .toString() *//* => "XCanTest.publicCert" *//*;*/
    // Warning: Development and prod environments use the same configuration
    return new Str0(new long[]{0xBD6A2DCCCC42F388L, 0xB62445B360A00717L, 0x26DD774A8A4F1FEL,
        0x3234E4FA1BBEC35L}).toString() /* => "XCanTest.publicCert" */;
  }

  @DoInFuture("Use jenkins to set the path in the prod environment, and bind IP address in online environment")
  private String getCloudServicePath(String path) {
    File tmpFile = new File(new SpringAppDirUtils().getTmpDir() + randomAlphanumeric(32) + ".dat");
    InputStream stream = null;
    try {
    /* if (SpringContextHolder.isProd()) {
        stream = VerxProperties.class.getResourceAsStream("/" + new Str0(
            new long[]{0xDB26FAC22A90A704L, 0xB65E4E58ECF57471L, 0xE89A012332339A39L,
                0x519800781A06E189L}).toString() *//* => "cert/XCanCloud.lic" *//*);
      } else {
        stream = VerxProperties.class.getResourceAsStream("/" + new Str0(
            new long[]{0x7199F6B52D71A0CEL, 0x3ECD139F9A2FD8F1L, 0x1D3579DD84AD8915L})
            .toString() *//* => "cert/license.lic" *//*);
      }*/
      // Warning: Development and prod environments use the same configuration
      stream = VerxProperties.class.getResourceAsStream("/" + new Str0(
          new long[]{0x7199F6B52D71A0CEL, 0x3ECD139F9A2FD8F1L, 0x1D3579DD84AD8915L})
          .toString() /* => "cert/license.lic" */);
      FileUtils.copyInputStreamToFile(stream, tmpFile);
      path = tmpFile.getAbsolutePath();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (null != stream) {
        IOUtils.closeQuietly(stream);
      }
    }
    return path;
  }
}
