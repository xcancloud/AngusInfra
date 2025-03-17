package cloud.xcan.sdf.core.biz;

import java.util.function.Supplier;

public class JoinSupplier {

  @NameJoin
  public <T> T execute(Supplier<T> supplier) {
    return supplier.get();
  }

}