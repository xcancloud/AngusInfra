package cloud.xcan.angus.spec.utils.ssl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Objects;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * {@link SSLSocketFactory} backed by a local truststore file (for example a custom CA bundle).
 */
public class LocalTrustStoreSSLSocketFactory extends SSLSocketFactory {

  private final SSLSocketFactory factory;

  /**
   * Loads a JKS truststore from the given file (no keystore password).
   */
  public LocalTrustStoreSSLSocketFactory(File truststore) {
    this(Objects.requireNonNull(truststore, "truststore").toPath(), "JKS");
  }

  /**
   * Loads a truststore from the given path (no keystore password).
   *
   * @param truststore   path to the keystore file
   * @param keyStoreType keystore type, for example {@code "JKS"} or {@code "PKCS12"}
   */
  public LocalTrustStoreSSLSocketFactory(Path truststore, String keyStoreType) {
    Objects.requireNonNull(truststore, "truststore");
    Objects.requireNonNull(keyStoreType, "keyStoreType");
    SSLContext sslcontext;
    try {
      KeyStore ks = KeyStore.getInstance(keyStoreType);
      try (InputStream stream = new BufferedInputStream(Files.newInputStream(truststore))) {
        ks.load(stream, null);
      }

      TrustManagerFactory tmf = TrustManagerFactory.getInstance(
          TrustManagerFactory.getDefaultAlgorithm());
      tmf.init(ks);
      TrustManager[] trustmanagers = tmf.getTrustManagers();
      sslcontext = SSLContext.getInstance("TLS"); // $NON-NLS-1$
      sslcontext.init(null, trustmanagers, new SecureRandom());
    } catch (Exception e) {
      throw new IllegalStateException("Could not create the SSL context", e);
    }
    factory = sslcontext.getSocketFactory();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Socket createSocket(Socket socket, String s, int i, boolean flag) throws IOException {
    return factory.createSocket(socket, s, i, flag);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Socket createSocket(InetAddress inaddr, int i, InetAddress inaddr1, int j)
      throws IOException {
    return factory.createSocket(inaddr, i, inaddr1, j);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Socket createSocket(InetAddress inaddr, int i) throws IOException {
    return factory.createSocket(inaddr, i);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Socket createSocket(String s, int i, InetAddress inaddr, int j) throws IOException {
    return factory.createSocket(s, i, inaddr, j);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Socket createSocket(String s, int i) throws IOException {
    return factory.createSocket(s, i);
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
