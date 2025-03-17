package cloud.xcan.sdf.core.jpa.auditor;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.sdf.spec.experimental.Entity;
import cloud.xcan.sdf.spec.experimental.EntitySupport;
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
  @Column(name = "last_modified_date", columnDefinition = "TIMESTAMP")
  protected LocalDateTime lastModifiedDate;

  @LastModifiedBy
  @Column(name = "last_modified_by")
  protected Long lastModifiedBy;

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

  public LocalDateTime getLastModifiedDate() {
    return lastModifiedDate;
  }

  public T setLastModifiedDate(LocalDateTime lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
    return (T) this;
  }

  public Long getLastModifiedBy() {
    return lastModifiedBy;
  }

  public T setLastModifiedBy(Long lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
    return (T) this;
  }
}
