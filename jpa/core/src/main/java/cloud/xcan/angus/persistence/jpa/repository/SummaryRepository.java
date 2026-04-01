package cloud.xcan.angus.persistence.jpa.repository;

import cloud.xcan.angus.persistence.jpa.repository.summary.SummaryQueryBuilder;
import java.util.List;

public interface SummaryRepository {

  List<Object[]> getSummer(SummaryQueryBuilder builder);
}
