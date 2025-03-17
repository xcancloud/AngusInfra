package cloud.xcan.sdf.spec.experimental;

import java.io.Serializable;

public class SimpleResult implements Serializable {

  public static final String SUCCESS_CODE = "S";

  private String code = SUCCESS_CODE;

  private String message;

  public SimpleResult() {
  }

  public SimpleResult(String code, String message) {
    this.code = code;
    this.message = message;
  }

  public SimpleResult setCode(String code) {
    this.code = code;
    return this;
  }

  public SimpleResult setMessage(String message) {
    this.message = message;
    return this;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  public boolean isSuccess() {
    return SUCCESS_CODE.equals(code);
  }

}
