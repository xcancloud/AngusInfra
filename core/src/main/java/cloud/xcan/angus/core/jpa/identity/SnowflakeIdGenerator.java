package cloud.xcan.angus.core.jpa.identity;

import cloud.xcan.angus.idgen.uid.impl.CachedUidGenerator;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

public class SnowflakeIdGenerator implements IdentifierGenerator {

  private static CachedUidGenerator uidGenerator;

  public static void setUidGenerator(CachedUidGenerator uidGenerator) {
    SnowflakeIdGenerator.uidGenerator = uidGenerator;
  }

  @Override
  public Object generate(SharedSessionContractImplementor implementor, Object o) {
    return uidGenerator.getUID();
  }
}
