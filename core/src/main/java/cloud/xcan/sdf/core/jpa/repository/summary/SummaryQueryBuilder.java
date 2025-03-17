package cloud.xcan.sdf.core.jpa.repository.summary;

import static cloud.xcan.sdf.core.jpa.repository.SimpleSummaryRepository.REGISTER;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.join;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.biz.ProtocolAssert;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@Accessors(chain = true)
public class SummaryQueryBuilder {

  @NotEmpty
  @Length(max = DEFAULT_NAME_LENGTH)
  private String name;

  private GroupBy groupBy;

  @Size(max = 10)
  private List<String> groupByColumns;

  private DateRangeType dateRangeType;

  @Size(max = 10)
  private List<Aggregate> aggregates;

  @Size(max = 20)
  @Schema(description = "Filter conditions, Max 20")
  private Set<SearchCriteria> filters = new HashSet<>();

  private transient boolean closeMultiTenantCtrl = false;

  private SummaryQueryBuilder(Builder builder) {
    setName(builder.name);
    setGroupBy(builder.groupBy);
    setGroupByColumns(builder.groupByColumns);
    setDateRangeType(isNull(builder.dateRangeType) ? DateRangeType.DAY : builder.dateRangeType);
    setAggregates(isEmpty(builder.aggregates) ? List.of(new Aggregate()) : builder.aggregates);
    setFilters(builder.filters);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static Builder newBuilder(SummaryQueryBuilder copy) {
    Builder builder = new Builder();
    builder.name = copy.getName();
    builder.groupBy = copy.getGroupBy();
    builder.groupByColumns = copy.getGroupByColumns();
    builder.dateRangeType = copy.getDateRangeType();
    builder.aggregates = copy.getAggregates();
    builder.filters = copy.getFilters();
    builder.closeMultiTenantCtrl = copy.isCloseMultiTenantCtrl();
    return builder;
  }

  public SummaryMode getSummaryMode() {
    if (Objects.isNull(groupBy)) {
      return SummaryMode.NO_GROUP;
    }
    return groupBy.isDateRange() ? SummaryMode.GROUP_BY_DATE : SummaryMode.GROUP_BY_STATUS;
  }

  public static final class Builder {

    private String name;
    private GroupBy groupBy;
    private List<String> groupByColumns;
    private DateRangeType dateRangeType;
    private List<Aggregate> aggregates;
    private @Size(max = 20) Set<SearchCriteria> filters;
    private transient boolean closeMultiTenantCtrl = false;

    private Builder() {
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder groupBy(GroupBy groupBy) {
      this.groupBy = groupBy;
      return this;
    }

    public Builder groupByColumns(List<String> groupByColumns) {
      this.groupByColumns = groupByColumns;
      return this;
    }

    public Builder dateRangeType(DateRangeType dateRangeType) {
      this.dateRangeType = dateRangeType;
      return this;
    }

    public Builder aggregates(List<Aggregate> aggregates) {
      this.aggregates = aggregates;
      return this;
    }

    public Builder filters(@Size(max = 20) Set<SearchCriteria> filters) {
      this.filters = filters;
      return this;
    }

    public Builder closeMultiTenantCtrl(boolean closeMultiTenantCtrl) {
      this.closeMultiTenantCtrl = closeMultiTenantCtrl;
      return this;
    }

    public SummaryMode getSummaryMode() {
      if (Objects.isNull(groupBy)) {
        return SummaryMode.NO_GROUP;
      }
      return groupBy.isDateRange() ? SummaryMode.GROUP_BY_DATE : SummaryMode.GROUP_BY_STATUS;
    }

    public void validate() {
      SummaryMode mode = getSummaryMode();
      ProtocolAssert.assertNotEmpty(name, "Summary resource is required");
      ProtocolAssert.assertTrue(mode.equals(SummaryMode.NO_GROUP)
          || isNotEmpty(groupByColumns), "Summary groupBy column is required");

      SummaryQueryRegister register = REGISTER.get(name);
      ProtocolAssert.assertNotNull(register, String.format("Unregistered resource: %s", name));

      if (mode.equals(SummaryMode.GROUP_BY_STATUS) || mode.equals(SummaryMode.GROUP_BY_DATE)) {
        for (int i = 0; i < groupByColumns.size(); i++) {
          String groupByColumn = groupByColumns.get(i);
          ProtocolAssert.assertTrue(ArrayUtils.contains(register.groupByColumns(), groupByColumn),
              String.format("Unregistered groupBy column: %s", groupByColumn));
          ProtocolAssert.assertTrue(!mode.equals(SummaryMode.GROUP_BY_STATUS)
                  || (!groupByColumn.contains("date") && !groupByColumn.contains("id")),
              String.format("Non status and date range statistics are not allowed, "
                  + "error column: %s", groupByColumn));
          ProtocolAssert.assertTrue(!mode.equals(SummaryMode.GROUP_BY_STATUS)
                  || !groupByColumn.contains("date"),
              "Date field is not supported for grouping by status, it should be an enum field");

          // Check multiple fields group must be consistent with the order of the union index
          if (mode.equals(SummaryMode.GROUP_BY_STATUS)) {
            if (register.groupByColumns()[0].contains("date")) {
              ProtocolAssert.assertTrue(groupByColumn.equals(register.groupByColumns()[i + 1]),
                  String.format(
                      "Multiple fields group must be consistent with the order of the union index, current order [%s], required order [%s]",
                      join(groupByColumns,","), join(register.groupByColumns(),",")));
            } else {
              ProtocolAssert.assertTrue(groupByColumn.equals(register.groupByColumns()[i]),
                  String.format(
                      "Multiple fields group must be consistent with the order of the union index, current order [%s], required order [%s]",
                      join(groupByColumns,","), join(register.groupByColumns(),",")));
            }
          }
        }
      }

      // Init default in constructor method: SummaryQueryBuilder(Builder builder)
      if (ObjectUtils.isNotEmpty(aggregates)) {
        for (Aggregate aggregate : aggregates) {
          ProtocolAssert
              .assertTrue(ArrayUtils.contains(register.aggregateColumns(), aggregate.getColumn()),
                  String.format("Unregistered aggregate column: %s", aggregate.getColumn()));
        }
      }
    }

    public SummaryQueryBuilder build() {
      validate();
      return new SummaryQueryBuilder(this);
    }
  }
}
