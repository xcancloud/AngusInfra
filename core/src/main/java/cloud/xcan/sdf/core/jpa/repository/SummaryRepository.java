package cloud.xcan.sdf.core.jpa.repository;

import cloud.xcan.sdf.core.jpa.repository.summary.SummaryQueryBuilder;
import java.util.List;

public interface SummaryRepository {

  List<Object[]> getSummer(SummaryQueryBuilder builder);
}
