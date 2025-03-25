package cloud.xcan.angus.spec.utils.crypto;

import static cloud.xcan.angus.spec.SpecConstant.DEFAULT_ENCODING;

import cloud.xcan.angus.api.pojo.Pair;
import cloud.xcan.angus.spec.experimental.Assert;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AESUtils {

  private static final Logger log = LoggerFactory.getLogger(AESUtils.class);

  public static final String ALGORITHM = "AES";
  public static final String ALGORITHM_TRANSFORMATION = "AES/CBC/PKCS5Padding";
  public static final String KEY_ALGORITHM = "SHA1PRNG";
  public static final int KEY_SIZE = 256;

  public static String generateKey(int keySize) {
    return generateKey(null, keySize, DEFAULT_ENCODING);
  }

  public static String generateKey(byte[] seed) {
    return generateKey(seed, KEY_SIZE, DEFAULT_ENCODING);
  }

  public static String generateKey(byte[] seed, int keySize) {
    return generateKey(seed, keySize, DEFAULT_ENCODING);
  }

  public static String generateKey(int keySize, String encodingType) {
    return generateKey(null, keySize, encodingType);
  }

  private static String generateKey(byte[] seed, int keySize, String encodingType) {
    try {
      SecretKey key = generateKey0(seed, keySize);
      return new String(Base64.getEncoder().encode(key.getEncoded()), encodingType);
    } catch (UnsupportedEncodingException e) {
      log.error("Unsupported encoding {} exception: {}", encodingType, e.getMessage());
      throw new RuntimeException("No such algorithm " + encodingType);
    }
  }

  public static SecretKey generateKey0(byte[] seed, int keySize) {
    KeyGenerator keyGen;
    try {
      keyGen = KeyGenerator.getInstance(ALGORITHM);
      if (Objects.nonNull(seed)) {
        SecureRandom secureRandom = SecureRandom.getInstance(KEY_ALGORITHM);
        secureRandom.setSeed(seed);
        keyGen.init(keySize, secureRandom);
      } else {
        keyGen.init(keySize);
      }
      return keyGen.generateKey();
    } catch (NoSuchAlgorithmException e) {
      log.error("No such algorithm {} exception: {}", ALGORITHM, e.getMessage());
      throw new RuntimeException("No such algorithm " + ALGORITHM);
    }
  }

  public static byte[] encrypt(byte[] encryptKey, byte[] initVector, byte[] content) {
    IvParameterSpec zeroIv = new IvParameterSpec(initVector);
    SecretKeySpec key = new SecretKeySpec(encryptKey, ALGORITHM);
    return encrypt(key, zeroIv, content);
  }

  public static byte[] decrypt(byte[] decryptKey, byte[] initVector, byte[] content) {
    IvParameterSpec zeroIv = new IvParameterSpec(initVector);
    SecretKeySpec key = new SecretKeySpec(decryptKey, ALGORITHM);
    return decrypt(key, zeroIv, content);
  }

  public static byte[] decrypt(SecretKeySpec keySpec, IvParameterSpec initVector, byte[] content) {
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM_TRANSFORMATION);
      cipher.init(Cipher.DECRYPT_MODE, keySpec, initVector);
      return cipher.doFinal(content);
    } catch (Exception e) {
      log.error("AES decrypt exception, iv={}, key={}",
          Arrays.toString(initVector.getIV()),
          Arrays.toString(keySpec.getEncoded()), e);
      throw new CryptoException("AES decrypt exception", e);
    }
  }

  public static byte[] encrypt(SecretKeySpec keySpec, IvParameterSpec initVector, byte[] content) {
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM_TRANSFORMATION);
      cipher.init(Cipher.ENCRYPT_MODE, keySpec, initVector);
      return cipher.doFinal(content);
    } catch (Exception e) {
      log.error("AES encrypt exception, iv={}, key={}",
          Arrays.toString(initVector.getIV()),
          Arrays.toString(keySpec.getEncoded()), e);
      throw new CryptoException("AES encrypt exception", e);
    }
  }

  public static String encrypt(String encryptKey, String initVector, String transformation,
      String content) {
    try {
      IvParameterSpec iv = new IvParameterSpec(initVector.getBytes(DEFAULT_ENCODING));
      SecretKeySpec skeySpec = new SecretKeySpec(Base64Utils.decode(encryptKey), ALGORITHM);

      Cipher cipher = Cipher.getInstance(transformation);
      cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

      byte[] encrypted = cipher.doFinal(content.getBytes());
      return Base64.getEncoder().encodeToString(encrypted);
    } catch (Exception e) {
      log.error(
          "AES encrypt exception, encryptKey={}, initVector={}, transformation={}, content={}",
          encryptKey, initVector, transformation, content, e);
      throw new CryptoException("AES decrypt exception", e);
    }
  }

  public static String decrypt(String decryptKey, String initVector, String transformation,
      String content) {
    try {
      IvParameterSpec iv = new IvParameterSpec(initVector.getBytes(DEFAULT_ENCODING));
      SecretKeySpec skeySpec = new SecretKeySpec(Base64Utils.decode(decryptKey), ALGORITHM);

      Cipher cipher = Cipher.getInstance(transformation);
      cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

      byte[] original = cipher.doFinal(Base64.getDecoder().decode(content));

      return new String(original);
    } catch (Exception e) {
      log.error(
          "AES decrypt exception, decryptKey={}, initVector={}, transformation={}, content={}",
          decryptKey, initVector, transformation, content, e);
      throw new CryptoException("AES decrypt exception", e);
    }
  }

  public static String encrypt(Pair<String, String> pair) {
    Assert.assertNotNull(pair.first(), "Encrypt key is required");
    Assert.assertNotNull(pair.second(), "Encrypt content is required");
    String encryptKey = generateKey(pair.first().getBytes());
    String initVector = encryptKey.substring(0, 16);
    return encrypt(encryptKey, initVector, ALGORITHM_TRANSFORMATION, pair.second());
  }

  public static String decrypt(Pair<String, String> pair) {
    Assert.assertNotNull(pair.first(), "Decrypt key is required");
    Assert.assertNotNull(pair.second(), "Decrypt content is required");
    String decryptKey = generateKey(pair.first().getBytes());
    String initVector = decryptKey.substring(0, 16);
    return decrypt(decryptKey, initVector, ALGORITHM_TRANSFORMATION, pair.second());
  }

  /**
   * 在AES算法中，密钥长度有限制，必须符合以下规则：
   * <p>
   * 对于AES-128，密钥长度必须是128位（16字节）, 如：0123456789abcdef
   * <p>
   * 对于AES-192，密钥长度必须是192位（24字节）, 如：0123456789abcdef12345678
   * <p>
   * 对于AES-256，密钥长度必须是256位（32字节）
   */
  public static void main(String[] args) {

    String key = AESUtils.generateKey(KEY_SIZE, DEFAULT_ENCODING);
    String initVector = key.substring(0, 16);
    String content = "XCan";
    System.out.println("Content   : " + content);

    String encrypted = AESUtils.encrypt(key, initVector, ALGORITHM_TRANSFORMATION, content);
    System.out.println("Encrypted : " + encrypted);

    String decrypted = AESUtils.decrypt(key, initVector, ALGORITHM_TRANSFORMATION, encrypted);
    System.out.println("Decrypted : " + decrypted);

    // First run >_
    // Content   : XCan
    // Encrypted : artlftRsDrwnDIzF8y36hA==
    // Decrypted : XCan

    // Second run >_
    // Content   : XCan
    // Encrypted : c7OhJzs9RSZQDdRTfSOYPw==
    // Decrypted : XCan

    Pair<String, String> passdAndContent = Pair.of("0123456789abcdef", content);
    String encrypted2 = AESUtils.encrypt(passdAndContent);
    System.out.println("Encrypted : " + encrypted2);

    Pair<String, String> passdAndEncrypted = Pair.of("0123456789abcdef", encrypted2);
    String decrypted2 = AESUtils.decrypt(passdAndEncrypted);
    System.out.println("Decrypted : " + decrypted2);
  }
}

