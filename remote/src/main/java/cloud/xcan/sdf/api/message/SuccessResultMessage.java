package cloud.xcan.sdf.api.message;


import static cloud.xcan.sdf.api.ApiConstant.OK_CODE;
import static cloud.xcan.sdf.api.message.SuccessResultMessage.M.OK_MSG;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class SuccessResultMessage implements ResultMessage {

  private final String msg;

  public SuccessResultMessage() {
    this(OK_MSG);
  }

  public SuccessResultMessage(String message) {
    super();
    this.msg = message;
  }

  public SuccessResultMessage of(String message) {
    return new SuccessResultMessage(message);
  }

  @Override
  public String getCode() {
    return OK_CODE;
  }

  @Override
  public Object[] getArgs() {
    return null;
  }

  @Override
  public Object getData() {
    return null;
  }

  @Override
  public String getCauseMessage() {
    return null;
  }

  /**
   * Result message
   */
  public interface M {

    /**
     * Default success message
     **/
    String OK_MSG = "xcm.success";
  }
}
