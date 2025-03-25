package cloud.xcan.angus.api.pojo.manifest;

import cloud.xcan.angus.api.enums.ProductConsumerType;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class License implements Serializable {

  private String lcsNo;

  private String mainLcsNo;

  private String provider;

  private String issuer;

  private Long holderId;

  private String holder;

  private String orderNo;

  private String subject;

  private String info;

  private String signature;

  private String productSignatureArtifact;

  private String productSignature;

  private LocalDateTime issuedDate;

  private LocalDateTime expiredDate;

  private ProductConsumerType consumerType;

  private Integer consumerAmount;

  private Integer testConcurrency;

  private Integer testNodeNumber;

  private Integer testConcurrentTaskNumber;

  private Map<String, String> extras;

  public boolean isExpired() {
    return Objects.nonNull(expiredDate) && expiredDate.isBefore(LocalDateTime.now());
  }

  private License(Builder builder) {
    setLcsNo(builder.lcsNo);
    setMainLcsNo(builder.mainLcsNo);
    setProvider(builder.provider);
    setIssuer(builder.issuer);
    setHolderId(builder.holderId);
    setHolder(builder.holder);
    setOrderNo(builder.orderNo);
    setSubject(builder.subject);
    setInfo(builder.info);
    setSignature(builder.signature);
    setProductSignatureArtifact(builder.productSignatureArtifact);
    setProductSignature(builder.productSignature);
    setIssuedDate(builder.issuedDate);
    setExpiredDate(builder.expiredDate);
    setConsumerType(builder.consumerType);
    setConsumerAmount(builder.consumerAmount);
    setTestConcurrency(builder.testConcurrency);
    setTestNodeNumber(builder.testNodeNumber);
    setTestConcurrentTaskNumber(builder.testConcurrentTaskNumber);
    setExtras(builder.extras);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static Builder newBuilder(License copy) {
    Builder builder = new Builder();
    builder.lcsNo = copy.getLcsNo();
    builder.mainLcsNo = copy.getMainLcsNo();
    builder.provider = copy.getProvider();
    builder.issuer = copy.getIssuer();
    builder.holderId = copy.getHolderId();
    builder.holder = copy.getHolder();
    builder.orderNo = copy.getOrderNo();
    builder.subject = copy.getSubject();
    builder.info = copy.getInfo();
    builder.signature = copy.getSignature();
    builder.productSignatureArtifact = copy.getProductSignatureArtifact();
    builder.productSignature = copy.getProductSignature();
    builder.issuedDate = copy.getIssuedDate();
    builder.expiredDate = copy.getExpiredDate();
    builder.consumerType = copy.getConsumerType();
    builder.consumerAmount = copy.getConsumerAmount();
    builder.testConcurrency = copy.getTestConcurrency();
    builder.testNodeNumber = copy.getTestNodeNumber();
    builder.testConcurrentTaskNumber = copy.getTestConcurrentTaskNumber();
    builder.extras = copy.getExtras();
    return builder;
  }

  public static final class Builder {

    private String lcsNo;
    private String mainLcsNo;
    private String provider;
    private String issuer;
    private Long holderId;
    private String holder;
    private String orderNo;
    private String subject;
    private String info;
    private String signature;
    private String productSignatureArtifact;
    private String productSignature;
    private LocalDateTime issuedDate;
    private LocalDateTime expiredDate;
    private ProductConsumerType consumerType;
    private Integer consumerAmount;
    private Integer testConcurrency;
    private Integer testNodeNumber;
    private Integer testConcurrentTaskNumber;
    private Map<String, String> extras;

    private Builder() {
    }

    public Builder lcsNo(String lcsNo) {
      this.lcsNo = lcsNo;
      return this;
    }

    public Builder mainLcsNo(String mainLcsNo) {
      this.mainLcsNo = mainLcsNo;
      return this;
    }

    public Builder provider(String provider) {
      this.provider = provider;
      return this;
    }

    public Builder issuer(String issuer) {
      this.issuer = issuer;
      return this;
    }

    public Builder holderId(Long holderId) {
      this.holderId = holderId;
      return this;
    }

    public Builder holder(String holder) {
      this.holder = holder;
      return this;
    }

    public Builder orderNo(String orderNo) {
      this.orderNo = orderNo;
      return this;
    }

    public Builder subject(String subject) {
      this.subject = subject;
      return this;
    }

    public Builder info(String info) {
      this.info = info;
      return this;
    }

    public Builder signature(String signature) {
      this.signature = signature;
      return this;
    }

    public Builder productSignatureArtifact(String productSignatureArtifact) {
      this.productSignatureArtifact = productSignatureArtifact;
      return this;
    }

    public Builder productSignature(String productSignature) {
      this.productSignature = productSignature;
      return this;
    }

    public Builder issuedDate(LocalDateTime issuedDate) {
      this.issuedDate = issuedDate;
      return this;
    }

    public Builder expiredDate(LocalDateTime expiredDate) {
      this.expiredDate = expiredDate;
      return this;
    }

    public Builder consumerType(ProductConsumerType consumerType) {
      this.consumerType = consumerType;
      return this;
    }

    public Builder consumerAmount(Integer consumerAmount) {
      this.consumerAmount = consumerAmount;
      return this;
    }

    public Builder testConcurrency(Integer testConcurrency) {
      this.testConcurrency = testConcurrency;
      return this;
    }

    public Builder testNodeNumber(Integer testNodeNumber) {
      this.testNodeNumber = testNodeNumber;
      return this;
    }

    public Builder testConcurrentTaskNumber(Integer testConcurrentTaskNumber) {
      this.testConcurrentTaskNumber = testConcurrentTaskNumber;
      return this;
    }

    public Builder extras(Map<String, String> extras) {
      this.extras = extras;
      return this;
    }

    public License build() {
      return new License(this);
    }
  }
}
