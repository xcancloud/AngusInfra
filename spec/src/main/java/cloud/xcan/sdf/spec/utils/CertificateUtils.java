package cloud.xcan.sdf.spec.utils;

import javax.security.cert.CertificateEncodingException;
import javax.security.cert.X509Certificate;

public class CertificateUtils {

  public static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
  public static final String END_CERT = "-----END CERTIFICATE-----";

  private CertificateUtils() {
    /* no instance */
  }

  public static String toPem(X509Certificate certificate) throws CertificateEncodingException {
    StringBuilder builder = new StringBuilder();
    builder.append("-----BEGIN CERTIFICATE-----");
    builder.append('\n');
    //builder.append(encodeString(certificate.getEncoded(), 0, true, false)); // TODO
    builder.append('\n');
    builder.append("-----END CERTIFICATE-----");
    return builder.toString();
  }

}
