package cloud.xcan.angus.core.export;

import static java.util.Objects.nonNull;

import cloud.xcan.angus.spec.experimental.Message;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.ReadConverterContext;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.data.WriteCellData;

public class EnumMessageWriteConverter implements Converter<Message> {

  @Override
  public Class<?> supportJavaTypeKey() {
    return Message.class;
  }

  @Override
  public CellDataTypeEnum supportExcelTypeKey() {
    return CellDataTypeEnum.STRING;
  }

  @Override
  public Message convertToJavaData(ReadConverterContext<?> context) {
    // NOOP
    return null;
  }

  @Override
  public WriteCellData<?> convertToExcelData(WriteConverterContext<Message> context) {
    return nonNull(context.getValue()) ? new WriteCellData<>(context.getValue().getMessage())
        : null;
  }
}
