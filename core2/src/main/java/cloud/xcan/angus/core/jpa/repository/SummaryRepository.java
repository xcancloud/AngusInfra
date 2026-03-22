package cloud.xcan.angus.core.jpa.repository;

import cloud.xcan.angus.core.jpa.repository.summary.SummaryQueryBuilder;
import java.util.List;

public interface SummaryRepository {

  List<Object[]> getSummer(SummaryQueryBuilder builder);
}
