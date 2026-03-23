package cloud.xcan.angus.spec.utils.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

/**
 * {@link SSLSocketFactory} that accepts all certificates and hostnames.
 * <p><b>Not suitable for production</b> — use only for local testing or controlled environments.
 * <p>Calling {@link #disableSSLVerification()} installs JVM-wide defaults for
 * {@link HttpsURLConnection} once; constructing this class alone does not change those defaults.
 */
public class TrustAllSSLSocketFactory extends SSLSocketFactory {

  private static final X509Certificate[] EMPTY_ISSUERS = new X509Certificate[0];

  private static final TrustManager TRUST_ALL = new X509ExtendedTrustManager() {
    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return EMPTY_ISSUERS;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {
      // intentionally accept all — testing / perf tooling only
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) {
      // intentionally accept all — testing / perf tooling only
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket)
        throws CertificateException {
      // NOOP
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
        throws CertificateException {
      // NOOP
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket)
        throws CertificateException {
      // NOOP
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
        throws CertificateException {
      // NOOP
    }
  };

  private static volatile SSLContext sharedContext;

  private final SSLSocketFactory factory;

  public TrustAllSSLSocketFactory() {
    factory = sharedSslContext().getSocketFactory();
  }

  private static SSLContext sharedSslContext() {
    SSLContext ctx = sharedContext;
    if (ctx != null) {
      return ctx;
    }
    synchronized (TrustAllSSLSocketFactory.class) {
      if (sharedContext == null) {
        sharedContext = createSslContext();
      }
      return sharedContext;
    }
  }

  private static SSLContext createSslContext() {
    try {
      SSLContext sslcontext = SSLContext.getInstance("TLS"); // $NON-NLS-1$
      sslcontext.init(null, new TrustManager[]{TRUST_ALL}, new SecureRandom());
      return sslcontext;
    } catch (Exception e) {
      throw new IllegalStateException("Could not create the SSL context", e);
    }
  }

  private static volatile boolean jvmDefaultsInstalled;

  private static void installJvmHttpsDefaultsOnce() {
    if (jvmDefaultsInstalled) {
      return;
    }
    synchronized (TrustAllSSLSocketFactory.class) {
      if (jvmDefaultsInstalled) {
        return;
      }
      SSLContext ctx = sharedSslContext();
      HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
      HostnameVerifier allHostsValid = (hostname, session) -> true;
      HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
      jvmDefaultsInstalled = true;
    }
  }

  /**
   * Returns a new factory instance (no JVM-wide {@link HttpsURLConnection} side effects).
   */
  public static SocketFactory getDefault() {
    return new TrustAllSSLSocketFactory();
  }

  /**
   * Installs trust-all defaults for {@link HttpsURLConnection} at most once for this JVM.
   */
  public static void disableSSLVerification() {
    installJvmHttpsDefaultsOnce();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
      throws IOException {
    return factory.createSocket(socket, host, port, autoClose);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Socket createSocket(InetAddress address, int port,
      InetAddress localAddress, int localPort) throws IOException {
    return factory.createSocket(address, port, localAddress, localPort);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Socket createSocket(InetAddress address, int port) throws IOException {
    return factory.createSocket(address, port);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
      throws IOException {
    return factory.createSocket(host, port, localHost, localPort);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Socket createSocket(String host, int port) throws IOException {
    return factory.createSocket(host, port);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Socket createSocket() throws IOException {
    return factory.createSocket();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String[] getDefaultCipherSuites() {
    return factory.getDefaultCipherSuites();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String[] getSupportedCipherSuites() {
    return factory.getSupportedCipherSuites();
  }
}
