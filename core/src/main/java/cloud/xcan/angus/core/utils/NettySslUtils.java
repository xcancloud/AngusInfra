package cloud.xcan.angus.core.utils;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static org.springframework.util.ResourceUtils.getURL;

import cloud.xcan.angus.spec.utils.StringUtils;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import java.net.URL;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;

/**
 * refer to {@link org.springframework.boot.web.embedded.netty.SslServerCustomizer}
 */
public final class NettySslUtils {

  public static SslContext createSslContext(
      String keyPassword, String keyStoreResource, String keyStoreType, String keyStorePassword,
      String trustStoreResource, String trustStoreType, String trustStorePassword)
      throws SSLException {
    SslContextBuilder sslBuilder = SslContextBuilder
        .forServer(getKeyManagerFactory(keyStoreType, keyStoreResource,
            keyPassword, keyStorePassword))
        .trustManager(getTrustManagerFactory(trustStoreType,
            trustStoreResource, trustStorePassword));
    return sslBuilder.build();
  }

  private static KeyManagerFactory getKeyManagerFactory(
      String type, String resource, String keyPassword, String keyStorePassword) {
    try {
      KeyStore keyStore = loadKeyStore(type, resource, keyStorePassword);
      KeyManagerFactory keyManagerFactory = KeyManagerFactory
          .getInstance(KeyManagerFactory.getDefaultAlgorithm());
      char[] keyPasswordBytes = (StringUtils.isNotEmpty(keyPassword)
          ? keyPassword.toCharArray() : null);
      if (keyPasswordBytes == null && StringUtils.isNotEmpty(keyStorePassword)) {
        keyPasswordBytes = keyStorePassword.toCharArray();
      }
      keyManagerFactory.init(keyStore, keyPasswordBytes);
      return keyManagerFactory;
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }

  private static TrustManagerFactory getTrustManagerFactory(
      String trustStoreType, String trustStoreResource, String trustStorePassword) {
    try {
      KeyStore store = loadKeyStore(trustStoreType, trustStoreResource, trustStorePassword);
      TrustManagerFactory trustManagerFactory = TrustManagerFactory
          .getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(store);
      return trustManagerFactory;
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }

  private static KeyStore loadKeyStore(String type, String resource, String password)
      throws Exception {
    type = isEmpty(type) ? "JKS" : type;
    if (isEmpty(resource)) {
      return null;
    }
    KeyStore store = KeyStore.getInstance(type);
    URL url = getURL(resource);
    store.load(url.openStream(), isEmpty(password) ? null : password.toCharArray());
    return store;
  }
}
