package cloud.xcan.sdf.validator;


import org.apache.commons.validator.routines.CreditCardValidator;

public enum CreditCardType {

  AMEX(CreditCardValidator.AMEX),
  DINERS(CreditCardValidator.DINERS),
  DISCOVER(CreditCardValidator.DISCOVER),
  MASTERCARD(CreditCardValidator.MASTERCARD),
  VISA(CreditCardValidator.VISA),
  VPAY(CreditCardValidator.VPAY),
  ALL(
      CreditCardValidator.AMEX +
          CreditCardValidator.DINERS +
          CreditCardValidator.DISCOVER +
          CreditCardValidator.MASTERCARD +
          CreditCardValidator.VISA +
          CreditCardValidator.VPAY
  );

  private Long internalValue;

  CreditCardType(Long internalValue) {
    this.internalValue = internalValue;
  }

  public Long getInternalValue() {
    return internalValue;
  }
}
