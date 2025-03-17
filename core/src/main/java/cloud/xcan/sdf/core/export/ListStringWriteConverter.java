package cloud.xcan.sdf.core.export;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.ReadConverterContext;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.data.WriteCellData;
import java.util.List;

public class ListStringWriteConverter implements Converter<List<String>> {

  @Override
  public Class<?> supportJavaTypeKey() {
    return List.class;
  }

  @Override
  public CellDataTypeEnum supportExcelTypeKey() {
    return CellDataTypeEnum.STRING;
  }

  @Override
  public List<String> convertToJavaData(ReadConverterContext<?> context) {
    // NOOP
    return null;
  }

  @Override
  public WriteCellData<String> convertToExcelData(WriteConverterContext<List<String>> context) {
    return isNotEmpty(context.getValue()) ? new WriteCellData<>(
        String.join("##", context.getValue())) : new WriteCellData<>("");
  }

}
