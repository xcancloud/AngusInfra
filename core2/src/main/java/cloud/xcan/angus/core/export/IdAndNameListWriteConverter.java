package cloud.xcan.angus.core.export;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.remote.vo.IdAndNameVo;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.ReadConverterContext;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.data.WriteCellData;
import java.util.List;
import java.util.stream.Collectors;

public class IdAndNameListWriteConverter implements Converter<List<IdAndNameVo>> {

  @Override
  public Class<?> supportJavaTypeKey() {
    return IdAndNameVo.class;
  }

  @Override
  public CellDataTypeEnum supportExcelTypeKey() {
    return CellDataTypeEnum.STRING;
  }

  @Override
  public List<IdAndNameVo> convertToJavaData(ReadConverterContext<?> context) {
    // NOOP
    return null;
  }

  @Override
  public WriteCellData<String> convertToExcelData(
      WriteConverterContext<List<IdAndNameVo>> context) {
    return isNotEmpty(context.getValue()) ? new WriteCellData<>(
        context.getValue().stream().map(IdAndNameVo::getName)
            .collect(Collectors.joining(","))) : new WriteCellData<>("");
  }

}
