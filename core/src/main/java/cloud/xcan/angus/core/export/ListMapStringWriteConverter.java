package cloud.xcan.angus.core.export;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.ReadConverterContext;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.data.WriteCellData;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ListMapStringWriteConverter implements Converter<List<Map<String, Integer>>> {

  @Override
  public Class<?> supportJavaTypeKey() {
    return List.class;
  }

  @Override
  public CellDataTypeEnum supportExcelTypeKey() {
    return CellDataTypeEnum.STRING;
  }

  @Override
  public List<Map<String, Integer>> convertToJavaData(ReadConverterContext<?> context) {
    // NOOP
    return null;
  }

  @Override
  public WriteCellData<String> convertToExcelData(
      WriteConverterContext<List<Map<String, Integer>>> context) {
    return isNotEmpty(context.getValue()) ? new WriteCellData<>(
        context.getValue().stream().flatMap(map -> map.entrySet().stream())
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining(";"))) : new WriteCellData<>("");
  }

}
