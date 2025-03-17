package cloud.xcan.sdf.api.pojo;

import java.time.LocalDateTime;

public interface IdAndCreatedDateBase<T extends IdAndCreatedDateBase<T>> {

  Long getId();

  T setId(Long id);

  LocalDateTime getCreatedDate();

  T setCreatedDate(LocalDateTime createdDate);

}
