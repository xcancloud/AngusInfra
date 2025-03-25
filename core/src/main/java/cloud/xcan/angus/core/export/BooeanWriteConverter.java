package cloud.xcan.angus.core.export;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.ReadConverterContext;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.data.WriteCellData;

public class BooeanWriteConverter implements Converter<Boolean> {

  @Override
  public Class<?> supportJavaTypeKey() {
    return Boolean.class;
  }

  @Override
  public CellDataTypeEnum supportExcelTypeKey() {
    return CellDataTypeEnum.STRING;
  }

  @Override
  public Boolean convertToJavaData(ReadConverterContext<?> context) {
    // NOOP
    return null;
  }

  @Override
  public WriteCellData<String> convertToExcelData(WriteConverterContext<Boolean> context) {
    return isNotEmpty(context.getValue()) ? new WriteCellData<>(
        isNull(context.getValue()) ? "" : context.getValue() ? "是" : "否")
        : new WriteCellData<>("");
  }

}
