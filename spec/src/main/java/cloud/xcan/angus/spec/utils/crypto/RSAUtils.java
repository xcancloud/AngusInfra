package cloud.xcan.angus.spec.utils.crypto;

import static java.nio.charset.StandardCharsets.UTF_8;

import cloud.xcan.angus.api.pojo.Pair;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RSA public/private key/signature toolkit.
 *
 * <pre>
 *   - String format secret key are BASE64 encoded unless otherwise specified.
 *   - Due to the extremely slow speed of asymmetric encryption, general files do not use it to encrypt.
 *     Asymmetric encryption algorithm can be used to encrypt the key, so that the security of the key also ensures the security of the data.
 * </pre>
 */
public final class RSAUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(RSAUtils.class);

  /**
   * Key(secret) bits
   */
  public static final int RAS_KEY_SIZE = 1024;

  /**
   * Encryption Algorithm RSA
   */
  public static final String KEY_ALGORITHM = "RSA";

  /**
   * Padding
   */
  public static final String KEY_ALGORITHM_PADDING = "RSA/ECB/PKCS1Padding";

  /**
   * Signature algorithm
   */
  public static final String SIGNATURE_ALGORITHM = "MD5withRSA";

  /**
   * RSA maximum decrypted ciphertext size
   */
  private static final int MAX_DECRYPT_BLOCK = 128;

  /**
   * RSA maximum encrypted plaintext size
   */
  private static final int MAX_ENCRYPT_BLOCK = 128 - 11;

  /**
   * Generate public and private keys
   *
   * @param rsaKeySize key size
   * @return public and private keys
   */
  public static Pair<RSAPublicKey, RSAPrivateKey> genKeyPair(int rsaKeySize)
      throws NoSuchAlgorithmException {
    KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
    keyPairGen.initialize(rsaKeySize);
    KeyPair keyPair = keyPairGen.generateKeyPair();
    RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
    RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
    return Pair.of(publicKey, privateKey);
  }

  /**
   * Encoded keys for easy storage
   *
   * @param key key
   * @return base64 string
   */
  public static String encodeBase64(Key key) {
    return Base64Utils.encode(key.getEncoded());
  }

  /**
   * Decode private key from string
   *
   * @param key key
   * @return base64 string
   * @throws Exception Exception
   */
  public static PrivateKey decodePrivateKey(String key) throws Exception {
    byte[] keyBytes = Base64Utils.decode(key);
    PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
    return keyFactory.generatePrivate(pkcs8KeySpec);
  }

  /**
   * Decode public key from string
   *
   * @param publicKey public key
   * @return public key
   * @throws Exception Exception
   */
  public static PublicKey decodePublicKey(String publicKey) throws Exception {
    byte[] keyBytes = Base64Utils.decode(publicKey);
    X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
    return keyFactory.generatePublic(x509KeySpec);
  }

  /**
   * Use the private key to generate a digital signature on the message
   *
   * @param data       encrypted data
   * @param privateKey private key (BASE64 encoding)
   * @return private key
   * @throws Exception Exception
   */
  public static String sign(byte[] data, String privateKey) throws Exception {
    Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
    signature.initSign(decodePrivateKey(privateKey));
    signature.update(data);
    return Base64Utils.encode(signature.sign());
  }

  /**
   * Verify digital signature
   *
   * @param data      encrypted data
   * @param publicKey public key (BASE64 encoding)
   * @param sign      digital signature
   * @return pass the verification
   * @throws Exception Exception
   */
  public static boolean verify(byte[] data, String publicKey, String sign) throws Exception {
    Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
    signature.initVerify(decodePublicKey(publicKey));
    signature.update(data);
    return signature.verify(Base64Utils.decode(sign));
  }

  /**
   * Use modulus and exponent to generate RSA public key
   * <p>
   * Note: This code uses the default padding method, which is RSA/None/PKCS1Padding. The default
   * padding method of different JDKs may be different. For example, Android defaults to
   * RSA/None/NoPadding.
   *
   * @param modulus
   * @param exponent exponent
   * @return public key
   */
  public static RSAPublicKey getPublicKey(String modulus, String exponent)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    BigInteger b1 = new BigInteger(modulus);
    BigInteger b2 = new BigInteger(exponent);
    KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
    RSAPublicKeySpec keySpec = new RSAPublicKeySpec(b1, b2);
    return (RSAPublicKey) keyFactory.generatePublic(keySpec);
  }

  /**
   * Generate RSA private key using modulus and exponent
   * <p>
   * Note: This code uses the default padding method, which is RSA/None/PKCS1Padding. The default
   * padding method of different JDKs may be different. For example, Android defaults to
   * RSA/None/NoPadding
   *
   * @param exponent exponent
   * @return private key
   */
  public static RSAPrivateKey getPrivateKey(String modulus, String exponent)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    BigInteger b1 = new BigInteger(modulus);
    BigInteger b2 = new BigInteger(exponent);
    KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
    RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(b1, b2);
    return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
  }

  /**
   * Public key encryption
   *
   * @param data      data to be encrypted
   * @param publicKey public key
   * @return encrypted value
   */
  public static byte[] encryptByPublicKey(byte[] data, RSAPublicKey publicKey) {
    try {
      Cipher cipher = Cipher.getInstance(KEY_ALGORITHM_PADDING);
      cipher.init(Cipher.ENCRYPT_MODE, publicKey);
      int keyLen = publicKey.getModulus().bitLength() / 8;
      return doFinal(cipher, data, keyLen - 11);
    } catch (Exception e) {
      LOGGER.error("encryptByPublicKey ex", e);
      throw new CryptoException("RSA encrypt ex", e);
    }
  }

  /**
   * Private key decryption
   *
   * @param data       data to be encrypted
   * @param privateKey private key
   * @return decrypted value
   */
  public static byte[] decryptByPrivateKey(byte[] data, RSAPrivateKey privateKey)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
    Cipher cipher = Cipher.getInstance(KEY_ALGORITHM_PADDING);
    cipher.init(Cipher.DECRYPT_MODE, privateKey);
    int keyLen = privateKey.getModulus().bitLength() / 8;
    //If the ciphertext length is greater than the modulo length, it needs to be decrypted in blocks
    return doFinal(cipher, data, keyLen);
  }

  /**
   * The maximum length of RSA encrypted plaintext is 117 bytes, and the maximum length of
   * ciphertext for decryption is 128 bytes, so it needs to be divided into blocks in the process of
   * encryption and decryption.
   *
   * @param cipher key
   * @param data   data to be processed
   * @return the processed value
   */
  private static byte[] doFinal(Cipher cipher, byte[] data, int key_len)
      throws BadPaddingException, IllegalBlockSizeException {
    int inputLen = data.length, offset = 0;
    byte[] tmp;
    ByteArrayOutputStream out = new ByteArrayOutputStream(getTmpArrayLength(inputLen));
    while (inputLen > 0) {
      tmp = cipher.doFinal(data, offset, Math.min(key_len, inputLen));
      out.write(tmp, 0, tmp.length);
      offset += key_len;
      inputLen -= key_len;
    }
    return out.toByteArray();
  }

  private static int getTmpArrayLength(int l) {
    int s = MAX_DECRYPT_BLOCK;
    while (s < l) {
      s <<= 1;
    }
    return s;
  }

  /**
   * Private key decryption
   *
   * @param data       encrypted data
   * @param privateKey private key (BASE64 encoding)
   * @return decrypted value
   */
  public static byte[] decryptByPrivateKey(byte[] data, String privateKey) throws Exception {
    PrivateKey key = decodePrivateKey(privateKey);
    Cipher cipher = Cipher.getInstance(KEY_ALGORITHM_PADDING);
    cipher.init(Cipher.DECRYPT_MODE, key);
    return doFinal(cipher, data, MAX_DECRYPT_BLOCK);
  }

  /**
   * Public key decryption
   *
   * @param data      encrypted data
   * @param publicKey public key (BASE64 encoding)
   * @return decrypted value
   */
  public static byte[] decryptByPublicKey(byte[] data, String publicKey) throws Exception {
    PublicKey key = decodePublicKey(publicKey);
    Cipher cipher = Cipher.getInstance(KEY_ALGORITHM_PADDING);
    cipher.init(Cipher.DECRYPT_MODE, key);
    return doFinal(cipher, data, MAX_DECRYPT_BLOCK);
  }

  /**
   * Public key encryption
   *
   * @param data      source data
   * @param publicKey public key (BASE64 encoding)
   * @return encrypted value
   */
  public static byte[] encryptByPublicKey(byte[] data, String publicKey) throws Exception {
    PublicKey key = decodePublicKey(publicKey);
    Cipher cipher = Cipher.getInstance(KEY_ALGORITHM_PADDING);
    cipher.init(Cipher.ENCRYPT_MODE, key);
    return doFinal(cipher, data, MAX_ENCRYPT_BLOCK);
  }

  /**
   * Private key encryption
   *
   * @param data       source data
   * @param privateKey private key (BASE64 encoding)
   * @return encrypted value
   */
  public static byte[] encryptByPrivateKey(byte[] data, String privateKey) throws Exception {
    PrivateKey key = decodePrivateKey(privateKey);
    Cipher cipher = Cipher.getInstance(KEY_ALGORITHM_PADDING);
    cipher.init(Cipher.ENCRYPT_MODE, key);
    return doFinal(cipher, data, MAX_ENCRYPT_BLOCK);
  }

  public static void main(String[] args) throws Exception {
    int keySize = RAS_KEY_SIZE;
    if (args.length > 0) {
      keySize = Integer.parseInt(args[0]);
    }
    if (keySize < RAS_KEY_SIZE) {
      keySize = RAS_KEY_SIZE;
    }
    Pair<RSAPublicKey, RSAPrivateKey> pair = RSAUtils.genKeyPair(keySize);
    RSAPublicKey publicKey = pair.name;
    RSAPrivateKey privateKey = pair.value;

    byte[] text = "This is a test text.".getBytes(UTF_8);
    System.out.println("Plain           text:\t" + new String(text, UTF_8));

    System.out.println("Private          key:\t" + RSAUtils.encodeBase64(privateKey));
    System.out.println("Public           key:\t" + RSAUtils.encodeBase64(publicKey));

    byte[] ciphertext = RSAUtils.encryptByPublicKey(text, publicKey);
    System.out.println("Encrypted ciphertext:\t" + new String(ciphertext, UTF_8));

    text = RSAUtils.decryptByPrivateKey(ciphertext, privateKey);
    System.out.println("Decrypted plaintext:\t" + new String(text, UTF_8));

    String modulus = publicKey.getModulus().toString();
    String public_exponent = publicKey.getPublicExponent().toString();
    String private_exponent = privateKey.getPrivateExponent().toString();
    text = "123456789".getBytes(UTF_8);
    System.out.println("\n\nPlain           text:\t" + new String(text, UTF_8));
    //Generate public and private keys using modulus and exponent
    RSAPrivateKey priKey = RSAUtils.getPrivateKey(modulus, private_exponent);
    RSAPublicKey pubKey = RSAUtils.getPublicKey(modulus, public_exponent);
    System.out.println("Private          key:\t" + priKey);
    System.out.println("Public           key:\t" + pubKey);

    ciphertext = RSAUtils.encryptByPublicKey(text, pubKey);
    System.out.println("Encrypted ciphertext:\t" + new String(ciphertext, UTF_8));

    text = RSAUtils.decryptByPrivateKey(ciphertext, priKey);
    System.out.println("Decrypted  plaintext:\t" + new String(text, UTF_8));
  }
}
