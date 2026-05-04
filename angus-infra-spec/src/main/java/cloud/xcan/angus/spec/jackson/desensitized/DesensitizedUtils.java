package cloud.xcan.angus.spec.jackson.desensitized;

import org.apache.commons.lang3.StringUtils;


public class DesensitizedUtils {

  /**
   * 【用户姓名或租户名称】中间使用 * 代理
   *
   * <pre>
   * 李  ->  李
   * 李明  ->  李*
   * 李世民  ->  李*民
   * 欧阳平平  ->  欧**平
   * 欧阳平平平  ->  欧阳*平平
   * 晓蚕科技（北京）有限公司  -> 晓蚕科技****有限公司
   * </pre>
   */
  public static String chineseName(String name) {
    if (name == null || name.length() == 1) {
      return name;
    }
    name = name.trim();
    if (name.length() == 2) {
      return name.charAt(0) + "*";
    }
    // 中间两位 **
    if (name.length() >= 3 && name.length() <= 4) {
      return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
    }
    // 前后两位中间 **
    if (name.length() >= 5 && name.length() <= 7) {
      return name.substring(0, 2) + "*".repeat(name.length() - 4) + name.substring(
          name.length() - 2);
    }
    // 只保留前4后4
    return name.substring(0, 4) + "*".repeat(name.length() - 8) + name.substring(name.length() - 4);
  }

  /**
   * 【身份证号】保留前三后四，其他隐藏。共计18位或者15位，比如：520**********1234
   */
  public static String idCardNum(String id) {
    if (StringUtils.isNotEmpty(id)) {
      return id.replaceAll("(?<=\\w{3})\\w(?=\\w{4})", "*");
    }
    return id;
  }

  /**
   * 【固定电话】后四位，其他隐藏，比如1234
   */
  public static String fixedPhone(String value) {
    if (StringUtils.isBlank(value)) {
      return "";
    }
    return StringUtils.leftPad(StringUtils.right(value, 4), StringUtils.length(value), "*");
  }

  /**
   * 【手机号码】前三位，后四位，其他隐藏，比如135****1010
   */
  public static String mobilePhone(String value) {
    if (StringUtils.isNotEmpty(value)) {
      return value.replaceAll("(?<=\\w{3})\\w(?=\\w{4})", "*");
    }
    return value;
  }

  /**
   * 【地址】只显示到地区，不显示详细地址，比如：北京市海淀区****
   *
   * @param sensitiveSize 敏感信息长度
   */
  public static String address(String address, int sensitiveSize) {
    if (StringUtils.isBlank(address)) {
      return "";
    }
    int length = StringUtils.length(address);
    return StringUtils.rightPad(StringUtils.left(address, length - sensitiveSize), length, "*");
  }

  /**
   * 【电子邮箱】邮箱前缀仅显示第一个字母，前缀其他隐藏，用星号代替，@及后面的地址显示，比如：d**@126.com>
   */
  public static String email(String email) {
    if (StringUtils.isBlank(email)) {
      return "";
    }
    int index = StringUtils.indexOf(email, "@");
    if (index <= 1) {
      return email;
    } else {
      return StringUtils.rightPad(StringUtils.left(email, 1), index, "*")
          .concat(StringUtils.mid(email, index, StringUtils.length(email)));
    }
  }

  /**
   * 【银行卡号】前六位，后四位，其他用星号隐藏每位1个星号，比如：6222600**********1234>
   */
  public static String bankCard(String cardNum) {
    if (StringUtils.isBlank(cardNum)) {
      return "";
    }
    return StringUtils.left(cardNum, 6).concat(StringUtils.removeStart(
        StringUtils.leftPad(StringUtils.right(cardNum, 4), StringUtils.length(cardNum), "*"),
        "******"));
  }

  /**
   * 【密码】密码的全部字符都用*代替，比如：******
   */
  public static String password(String password) {
    if (StringUtils.isBlank(password)) {
      return "";
    }
    String pwd = StringUtils.left(password, 0);
    return StringUtils.rightPad(pwd, StringUtils.length(password), "*");
  }

  /**
   * 【车牌号】前两位后一位，比如：苏M****5
   */
  public static String carNumber(String carNumber) {
    if (StringUtils.isBlank(carNumber)) {
      return "";
    }
    return StringUtils.left(carNumber, 2).
        concat(StringUtils.removeStart(StringUtils
            .leftPad(StringUtils.right(carNumber, 1), StringUtils.length(carNumber), "*"), "**"));

  }

  public static void main(String[] args) {
    System.out.println(mobilePhone("18910691729"));
    System.out.println(chineseName("李"));
    System.out.println(chineseName("李明"));
    System.out.println(chineseName("李世民"));
    System.out.println(chineseName("欧阳平平"));
    System.out.println(chineseName("欧阳平平平"));
    System.out.println(chineseName("欧阳平平平平"));
    System.out.println(chineseName("欧阳平平平平平"));
    System.out.println(chineseName("晓蚕科技（北京）有限公司"));
    System.out.println(chineseName("晓蚕云科技（北京）有限公司"));
  }
}
