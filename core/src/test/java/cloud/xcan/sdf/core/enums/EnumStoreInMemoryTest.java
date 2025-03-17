package cloud.xcan.sdf.core.enums;

import static cloud.xcan.sdf.spec.experimental.BizConstant.GM_SERVICE;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cloud.xcan.sdf.api.enums.GrantType;
import cloud.xcan.sdf.core.spring.boot.ApplicationInfo;
import cloud.xcan.sdf.spec.locale.EnumMessage;
import org.junit.Test;

public class EnumStoreInMemoryTest {

  @Test
  public void testInit() {
    ApplicationInfo apInfo = new ApplicationInfo();
    apInfo.setArtifactId(GM_SERVICE);
    EnumStoreInMemory enumStoreInMemory = new EnumStoreInMemory(apInfo);
    assertFalse(isEmpty(enumStoreInMemory.getEndpointRegisterNames()));
    assertFalse(isEmpty(enumStoreInMemory.getNames()));
    assertTrue(isEmpty(enumStoreInMemory.get(EnumMessage.class.getSimpleName())));
    assertTrue(isEmpty(enumStoreInMemory.get(GrantType.class.getSimpleName())));
  }

  @Test
  public void testClear() {
    ApplicationInfo apInfo = new ApplicationInfo();
    apInfo.setArtifactId(GM_SERVICE);
    EnumStoreInMemory enumStoreInMemory = new EnumStoreInMemory(apInfo);
    assertFalse(isEmpty(enumStoreInMemory.getEndpointRegisterNames()));
    assertFalse(isEmpty(enumStoreInMemory.getNames()));
    enumStoreInMemory.clearStore();
    assertTrue(isEmpty(enumStoreInMemory.getEndpointRegisterNames()));
    assertTrue(isEmpty(enumStoreInMemory.getNames()));
  }

  @Test
  public void testClassName() {
    assertEquals("cloud.xcan.sdf.core.enums.EnumStoreInMemoryTest",
        EnumStoreInMemoryTest.class.getName());
    assertEquals("cloud.xcan.sdf.core.enums.EnumStoreInMemoryTest",
        EnumStoreInMemoryTest.class.getCanonicalName());
    assertEquals("EnumStoreInMemoryTest", EnumStoreInMemoryTest.class.getSimpleName());
  }

}
