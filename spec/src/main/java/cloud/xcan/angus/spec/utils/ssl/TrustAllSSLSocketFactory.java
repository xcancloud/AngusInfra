package cloud.xcan.angus.spec.utils.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
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
 * This class can be used as a SocketFactory with SSL-connections.<p> Its purpose is to ensure that
 * all certificates - no matter from which CA - are accepted to enable the SSL-connection.<p>
 * <b>This is of course not secure</b>
 */
public class TrustAllSSLSocketFactory extends SSLSocketFactory {

  private final SSLSocketFactory factory;

  // Empty arrays are immutable
  private static final X509Certificate[] EMPTY_X509Certificate = new X509Certificate[0];

  /**
   * Standard constructor
   */
  public TrustAllSSLSocketFactory() {
    SSLContext sslcontext = null;
    try {
      sslcontext = SSLContext.getInstance("TLS"); // $NON-NLS-1$
      sslcontext.init(null, new TrustManager[]{
              new X509ExtendedTrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                  return EMPTY_X509Certificate;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain,
                    String authType) { // NOSONAR JMeter is a pentest and perf testing tool
                  // NOOP
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain,
                    String authType) { // NOSONAR JMeter is a pentest and perf testing tool
                  // NOOP
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket)
                    throws CertificateException {
                  // NOOP
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType,
                    SSLEngine engine)
                    throws CertificateException {
                  // NOOP
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket)
                    throws CertificateException {
                  // NOOP
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType,
                    SSLEngine engine)
                    throws CertificateException {
                  // NOOP
                }
              }
          },
          new java.security.SecureRandom());

      HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
      HostnameVerifier allHostsValid = (hostname, session) -> true;
      HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    } catch (Exception e) {
      throw new IllegalStateException("Could not create the SSL context", e);
    }
    factory = sslcontext.getSocketFactory();
  }

  /**
   * Factory method
   *
   * @return New TrustAllSSLSocketFactory
   */
  public static synchronized SocketFactory getDefault() {
    return new TrustAllSSLSocketFactory();
  }

  public static synchronized void disableSSLVerification(){
    new TrustAllSSLSocketFactory();
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
    return factory.createSocket(address, port, localAddress,
        localPort); // NOSONAR JMeter is a pentest and perf testing tool
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Socket createSocket(InetAddress address, int port) throws
      IOException {
    return factory.createSocket(address, port); // NOSONAR JMeter is a pentest and perf testing tool
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
      throws IOException {
    return factory.createSocket(host, port, localHost,
        localPort); // NOSONAR JMeter is a pentest and perf testing tool
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Socket createSocket(String host, int port) throws IOException {
    return factory.createSocket(host, port); // NOSONAR JMeter is a pentest and perf testing tool
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Socket createSocket() throws IOException {
    return factory.createSocket(); // NOSONAR JMeter is a pentest and perf testing tool
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String[] getDefaultCipherSuites() {
    return factory.getSupportedCipherSuites();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String[] getSupportedCipherSuites() {
    return factory.getSupportedCipherSuites();
  }
}
