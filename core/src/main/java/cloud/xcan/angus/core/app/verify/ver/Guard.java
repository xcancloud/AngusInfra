package cloud.xcan.angus.core.app.verify.ver;

import cloud.xcan.angus.core.app.verify.oar.Oar1;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.typelevel.xml.Var0;
import org.typelevel.xml.Var3;

public class Guard {

  public String var101() {
    return var0(Var.var1);
  }

  public String var102() {
    return var0(Var.var2);
  }

  public String var103() {
    return var0(Var.var3);
  }

  public String var104() {
    return var0(Var.var4);
  }

  public String var105() {
    return var0(Var.var5);
  }

  public String var106() {
    return var0(Var.var6);
  }

  public String var107() {
    return var0(Var.var7);
  }

  public String var108() {
    return var0(Var.var8);
  }

  public String var109() {
    return var0(Var.var9);
  }

  public String var110() {
    return var0(Var.var10);
  }

  public String var111() {
    return var0(Var.var11);
  }

  public String var112() {
    return var0(Var.var12);
  }

  public String var113() {
    return var0(Var.var13);
  }

  public String var114() {
    return var0(Var.var14);
  }

  public String var115() {
    return var0(Var.var15);
  }

  public String var116() {
    return var0(Var.var16);
  }

  public String var117() {
    return var0(Var.var17);
  }

  public String var118() {
    return var0(Var.var18);
  }

  public String var119() {
    return var0(Var.var19);
  }

  public String var120() {
    return var0(Var.var20);
  }

  public String var121() {
    return var0(Var.var21);
  }

  public String var122() {
    return var0(Var.var22);
  }

  public String var123() {
    return var0(Var.var23);
  }

  public String var124() {
    return var0(Var.var24);
  }

  public String var125() {
    return var0(Var.var25);
  }

  public String var126() {
    return var0(Var.var26);
  }

  public String var127() {
    return var0(Var.var27);
  }

  public String var128() {
    return var0(Var.var28);
  }

  public String var129() {
    return var0(Var.var29);
  }

  public String var130() {
    return var0(Var.var30);
  }

  public String var131() {
    return var0(Var.var31);
  }

  public String var132() {
    return var0(Var.var32);
  }

  private static final String Var9 = new Oar1(new long[]{
      0x9462FFDE0183752L, 0xE2A34A222A24DB14L
  }).toString(); /* => "param" */

  private static final String var10 = new Oar1(new long[]{
      0x27B2E8783E47F1ABL, 0x45CF8AD4390DC9D8L, 0xAB320350966BC9BFL
  }).toString(); /* => "PBEWithMD5AndDES" */

  private static final String var
      = new String(new char[]{'\u0055', '\u0054', '\u0046', '\u0038'}); // => "UTF8"

  private final String var11;
  private Cipher var12;
  private SecretKey var13;
  private AlgorithmParameterSpec var14;
  private Var0 var15;
  private final Document var3;

  public Guard(String licPwd, String licPath) throws Exception {
    if (licPwd == null) {
      throw new NullPointerException(Var9);
    }
    this.var11 = licPwd;
    this.var12 = null;
    this.var13 = null;
    this.var14 = null;
    this.var15 = var(licPath);
    this.var3 = new SAXReader()
        .read(new ByteArrayInputStream(this.var15.getEncoded().getBytes(var)));
  }

  public String var0(String var0) {
    Element var4 = var3.getRootElement();
    Iterator<Element> iterator = var4.elementIterator();
    while (iterator.hasNext()) {
      Element var5 = iterator.next();
      Iterator<Element> var7 = var5.elementIterator();
      while (var7.hasNext()) {
        Element var6 = var7.next();
        String var1 = var6.attribute(0).getValue();
        if (var1 != null) {
          if (var1.equalsIgnoreCase(var0)) {
            String var2 = var6.elements().get(0).getText();
            if (null != var6.elements().get(0).elements()
                && !var6.elements().get(0).elements().isEmpty()) {
              return var6.elements().get(0).elements().get(0).getText();
            }
            return var2;
          }
        }
      }
    }
    return null;
  }

  private Var0 var(String var0) throws Exception {
    if (this.var15 != null) {
      return this.var15;
    }
    byte[] key = var0(new File(var0));
    final InputStream in = new GZIPInputStream(
        new ByteArrayInputStream(var2().doFinal(key)));
    final Var0 var01;
    try {
      var01 = (Var0) Var3.load(in);
      this.var15 = var01;
    } finally {
      try {
        in.close();
      } catch (IOException weDontCare) {
        System.out.println(weDontCare.getMessage());
      }
    }
    return var01;
  }

  private Cipher var2() {
    Cipher var0 = var7();
    try {
      var0.init(Cipher.DECRYPT_MODE, var13, var14);
    } catch (Exception cannotHappen) {
      throw new AssertionError(cannotHappen);
    }
    return var0;
  }

  protected Cipher var7() {
    if (var12 != null) {
      return var12;
    }
    var14 = new PBEParameterSpec(
        new byte[]{
            (byte) 0xce, (byte) 0xfb, (byte) 0xde, (byte) 0xac,
            (byte) 0x05, (byte) 0x02, (byte) 0x19, (byte) 0x71
        },
        2005);
    try {
      KeySpec var0 = new PBEKeySpec(var11.toCharArray());
      SecretKeyFactory keyFac = SecretKeyFactory.getInstance(var10);
      var13 = keyFac.generateSecret(var0);
      var12 = Cipher.getInstance(var10);
    } catch (Exception cannotHappen) {
      throw new AssertionError(cannotHappen);
    }
    return var12;
  }

  private static byte[] var0(final File var0) throws IOException {
    // Allow max 1MB size files and let the verifier detect a partial read
    final int size = Math.min((int) var0.length(), 1024 * 1024);
    final byte[] b = new byte[size];
    try (InputStream in = new FileInputStream(var0)) {
      // Let the verifier detect a partial read as an error
      in.read(b);
    }
    return b;
  }
}
