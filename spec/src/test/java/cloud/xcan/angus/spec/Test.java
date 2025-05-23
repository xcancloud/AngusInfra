package cloud.xcan.angus.spec;

import cloud.xcan.angus.api.pojo.UserName;
import cloud.xcan.angus.spec.utils.GzipUtils;
import cloud.xcan.angus.spec.utils.crypto.Base64Utils;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Test {

  public static void main(String[] args) throws IOException {
    System.out.println(Base64Utils.encode(GzipUtils.compress("123".getBytes())));

    System.out.println(Base64Utils.encode(GzipUtils.compress("123".getBytes("UTF-8"))));

    System.out.println(new String(Base64Utils.decode("H4sIAAAAAAAAADM0MgYA0mNIiAMAAAA=")));

    System.out.println(new String(Base64Utils.decode(
        "UEsDBAoAAAAAAJMd6VbSY0iIAwAAAAMAAAAKAAAAZGF0YTAudGV4dDEyM1BLAQIUAAoAAAAAAJMd6VbSY0iIAwAAAAMAAAAKAAAAAAAAAAAAAAAAAAAAAABkYXRhMC50ZXh0UEsFBgAAAAABAAEAOAAAACsAAAAAAA==")));

    Map<UserName, Long> map = new HashMap<>();
    map.put(new UserName().setId(1L).setFullName("a"), 1L);
    map.put(new UserName().setId(1L).setFullName("a"), 2L);
    System.out.println(map.size());
    System.out.println(map.get(new UserName().setId(1L).setFullName("a")));

    String input = "This is my username and password.";
    String regex = "(?=.*\\busername\\b)(?=.*\\bpassword\\b).*";

    boolean containsKeywords = input.matches(regex);
    System.out.println("Contains keywords: " + containsKeywords);

  }
}
