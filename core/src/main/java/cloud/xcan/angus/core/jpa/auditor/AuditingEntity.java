package cloud.xcan.angus.core.jpa.auditor;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.spec.experimental.Entity;
import cloud.xcan.angus.spec.experimental.EntitySupport;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditingEntity<T extends Entity<T, ID>, ID> extends EntitySupport<T, ID> {

  public AuditingEntity() {
  }

  @CreatedDate
  @DateTimeFormat(pattern = DATE_FMT)
  //@Temporal(TemporalType.TIMESTAMP)
  @Column(name = "created_date", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
  protected LocalDateTime createdDate;

  @CreatedBy
  @Column(name = "created_by", nullable = false, updatable = false)
  protected Long createdBy;

  @LastModifiedDate
  @DateTimeFormat(pattern = DATE_FMT)
  //@Temporal(TemporalType.TIMESTAMP)
  @Column(name = "modified_date", columnDefinition = "TIMESTAMP")
  protected LocalDateTime modifiedDate;

  @LastModifiedBy
  @Column(name = "modified_by")
  protected Long modifiedBy;

  public LocalDateTime getCreatedDate() {
    return createdDate;
  }

  public T setCreatedDate(LocalDateTime createdDate) {
    this.createdDate = createdDate;
    return (T) this;
  }

  public Long getCreatedBy() {
    return createdBy;
  }

  public T setCreatedBy(Long createdBy) {
    this.createdBy = createdBy;
    return (T) this;
  }

  public LocalDateTime getModifiedDate() {
    return modifiedDate;
  }

  public T setModifiedDate(LocalDateTime modifiedDate) {
    this.modifiedDate = modifiedDate;
    return (T) this;
  }

  public Long getModifiedBy() {
    return modifiedBy;
  }

  public T setModifiedBy(Long modifiedBy) {
    this.modifiedBy = modifiedBy;
    return (T) this;
  }
}
