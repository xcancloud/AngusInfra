package cloud.xcan.angus.swagger;

import cloud.xcan.angus.remote.search.SearchOperation;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.web.method.HandlerMethod;

public class FiltersOperationCustomizer implements OperationCustomizer {

  public static int FILTER_PARAMETERS = 2;

  @Override
  public Operation customize(Operation operation, HandlerMethod handlerMethod) {
    if (operation.getParameters() == null) {
      return operation;
    }

    List<Parameter> parameters = new ArrayList<>();
    for (Parameter param : operation.getParameters()) {
      if ("filters".equals(param.getName())) {
        parameters.addAll(generateFilterParameters());
      } else {
        parameters.add(param);
      }
    }
    operation.setParameters(parameters);
    return operation;
  }

  private List<Parameter> generateFilterParameters() {
    List<Parameter> params = new ArrayList<>();
    for (int i = 0; i < FILTER_PARAMETERS; i++) {
      params.add(new Parameter()
          .in("query")
          .name("filters[" + i + "].key")
          .description("Customize the filter parameter name. Note: The parameter name must be a whitelist parameter")
          .schema(new StringSchema()));

      params.add(new Parameter()
          .in("query")
          .name("filters[" + i + "].op")
          .description("Customize the filter condition (EQUAL, NOT_EQUAL, GREATER_THAN, etc.)")
          .schema(new StringSchema()._enum(Arrays.stream(SearchOperation.values()).map(
              SearchOperation::getValue).toList())));

      params.add(new Parameter()
          .in("query")
          .name("filters[" + i + "].value")
          .description("Customize the filter value")
          .schema(new Schema<>().type("object")));
    }
    return params;
  }
}
