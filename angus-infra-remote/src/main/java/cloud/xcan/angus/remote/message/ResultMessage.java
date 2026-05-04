package cloud.xcan.angus.remote.message;

import static cloud.xcan.angus.remote.ApiConstant.OK_CODE;

import java.io.Serializable;

public interface ResultMessage extends Serializable {

  String getCode();

  String getMsg();

  Object[] getArgs();

  Object getData();

  String getCauseMessage();

  default boolean is4xxException() {
    return false;
  }

  default boolean is5xxException() {
    return false;
  }

  public default boolean is2xxException() {
    return !OK_CODE.equals(this.getCode());
  }
}
