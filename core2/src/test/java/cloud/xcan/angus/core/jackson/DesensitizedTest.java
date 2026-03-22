package cloud.xcan.angus.core.jackson;

import cloud.xcan.angus.spec.jackson.desensitized.Desensitized;
import cloud.xcan.angus.spec.jackson.desensitized.DesensitizedSerializer;
import cloud.xcan.angus.spec.jackson.desensitized.SensitiveType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.junit.Assert;
import org.junit.Test;

public class DesensitizedTest {

  @Test
  public void testDesensitized() throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    Assert.assertEquals("Error",
        "{\"fullname\":\"张*\",\"passd\":\"********\",\"bankCard\":\"520522********4487\",\"mobile\":\"152****7234\",\"fixedPhone\":\"***7190\",\"email\":\"t***@xcan.cloud\",\"carNumber\":\"京A****C\",\"idCardNumber\":\"520***********4487\",\"address\":\"北京市海淀区珠江摩尔大厦********\"}",
        objectMapper.writeValueAsString(new User()));
  }

}

@Data
class User {

  @JsonSerialize(using = DesensitizedSerializer.class)
  @Desensitized(type = SensitiveType.CHINESE_NAME)
  private String fullname = "张三";

  @JsonSerialize(using = DesensitizedSerializer.class)
  @Desensitized(type = SensitiveType.PASSWORD)
  private String passd = "1123@321";

  @JsonSerialize(using = DesensitizedSerializer.class)
  @Desensitized(type = SensitiveType.BANK_CARD)
  private String bankCard = "520522198090994487";

  @JsonSerialize(using = DesensitizedSerializer.class)
  @Desensitized(type = SensitiveType.MOBILE)
  private String mobile = "15219097234";

  @JsonSerialize(using = DesensitizedSerializer.class)
  @Desensitized(type = SensitiveType.FIXED_PHONE)
  private String fixedPhone = "7867190";

  @JsonSerialize(using = DesensitizedSerializer.class)
  @Desensitized(type = SensitiveType.EMAIL)
  private String email = "test@xcan.cloud";

  @JsonSerialize(using = DesensitizedSerializer.class)
  @Desensitized(type = SensitiveType.CAR_NUMBER)
  private String carNumber = "京A8HNCC";

  @JsonSerialize(using = DesensitizedSerializer.class)
  @Desensitized(type = SensitiveType.ID_CARD)
  private String idCardNumber = "520522198090994487";

  @JsonSerialize(using = DesensitizedSerializer.class)
  @Desensitized(type = SensitiveType.ADDRESS)
  private String address = "北京市海淀区珠江摩尔大厦3-2-1809";

}
