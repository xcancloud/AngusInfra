package cloud.xcan.angus.swagger;

import cloud.xcan.angus.remote.search.SearchOperation;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.core.MethodParameter;
import org.springframework.web.method.HandlerMethod;

public class FiltersOperationCustomizer implements OperationCustomizer {

  @Override
  public Operation customize(Operation operation, HandlerMethod handlerMethod) {
      if (operation.getParameters() == null) return operation;

    List<Parameter> parameters = new ArrayList<>();
    for (Parameter param : operation.getParameters()) {
      if ("filters".equals(param.getName()) && isListOfSearchCriteria(handlerMethod)) {
        parameters.addAll(generateFilterParameters());
      }else {
        parameters.add(param);
      }
    }
    operation.setParameters(parameters);
    return operation;
  }

  private boolean isListOfSearchCriteria(HandlerMethod handlerMethod) {
    for (MethodParameter methodParam : handlerMethod.getMethodParameters()) {
      try {
        Field field = methodParam.getParameterType().getDeclaredField("filters");
        return List.class.isAssignableFrom(field.getType());
      } catch (NoSuchFieldException e) {
      }
    }
    return false;
  }

  private List<Parameter> generateFilterParameters() {
    List<Parameter> params = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      params.add(new Parameter()
          .in("query")
          .name("filters[" + i + "].key")
          .description("Filter field name")
          .schema(new StringSchema()));

      params.add(new Parameter()
          .in("query")
          .name("filters[" + i + "].op")
          .description("Filter condition (EQUAL, NOT_EQUAL, GREATER_THAN, etc.)")
          .schema(new StringSchema()._enum(Arrays.stream(SearchOperation.values()).map(
              SearchOperation::getValue).toList())));

      params.add(new Parameter()
          .in("query")
          .name("filters[" + i + "].value")
          .description("Filter value")
          .schema(new Schema<>().type("object")));
    }
    return params;
  }
}
