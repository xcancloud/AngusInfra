package cloud.xcan.sdf.idgen.entity;

import cloud.xcan.sdf.idgen.bid.DateFormat;
import cloud.xcan.sdf.idgen.bid.Format;
import cloud.xcan.sdf.idgen.bid.Mode;
import cloud.xcan.sdf.idgen.bid.Scope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Setter
@Getter
@Accessors(chain = true)
@EntityListeners(AuditingEntityListener.class)
@Entity(name = "id_config")
@GenericGenerator(name = "idgen-uuid", strategy = "org.hibernate.id.UUIDGenerator")
public class IdConfig implements Serializable {

  /*
   * Entity unique pk (table unique)
   */
  @Id
  @GeneratedValue(generator = "idgen-uuid")
  private String pk;

  /**
   * Unique business identifier.
   */
  @Column(name = "biz_key")
  private String bizKey;

  /**
   * ID format, type of {@link Format}
   */
  @Enumerated(EnumType.STRING)
  private Format format;

  /**
   * Fixed prefix.
   */
  private String prefix;

  /**
   * Date format, type of {@link DateFormat}
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "date_format")
  private DateFormat dateFormat;

  /**
   * ID number length.
   * <p>
   * When ID is less than or equal to 0, 0 is not added to the left.
   */
  @Column(name = "seq_length")
  private Integer seqLength;

  /**
   * ID generation method, type of {@link Mode}
   */
  @Enumerated(EnumType.STRING)
  private Mode mode;

  /**
   * ID unique range, type of {@link Scope}
   */
  @Enumerated(EnumType.STRING)
  private Scope scope;

  /**
   * After specifying the tenant {@link Scope}, set the tenant ID correspondingly. If it is platform
   * scope, the default value is -1.
   */
  @Column(name = "tenant_id")
  private Long tenantId;

  /**
   * When generated in DB mode, it indicates the current maximum available id, if generated based on
   * the REDIS mode, it indicates the current maximum initialization value, and the current actual
   * value is the redis storage value.
   */
  @Column(name = "max_id")
  private Long maxId;

  /**
   * The length of the segment or the maximum number of cache IDs.
   */
  private Long step;

  /**
   * Created time
   */
  @CreatedDate
  @Column(name = "create_date")
  private LocalDateTime createDate;

  /**
   * Last modifiedTime
   */
  @LastModifiedDate
  @Column(name = "last_modified_date")
  private LocalDateTime lastModifiedDate;

}
