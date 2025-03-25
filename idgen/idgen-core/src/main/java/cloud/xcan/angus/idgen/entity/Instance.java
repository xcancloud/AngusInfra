package cloud.xcan.angus.idgen.entity;

import cloud.xcan.angus.api.pojo.instance.InstanceInfo;
import cloud.xcan.angus.api.pojo.instance.InstanceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * InstanceInfo Entity
 *
 * @author liuxiaolong
 */
@Setter
@Getter
@Accessors(chain = true)
@EntityListeners(AuditingEntityListener.class)
@Entity(name = "instance")
@GenericGenerator(name = "idgen-uuid", strategy = "org.hibernate.id.UUIDGenerator")
public class Instance implements InstanceInfo {

  /**
   * Entity unique pk (table unique)
   */
  @Id
  @GeneratedValue(generator = "idgen-uuid")
  private String pk;

  /**
   * Instance ID(+1 to the ID every time the app starts)
   */
  private Long id;

  /**
   * HostName, IP.
   */
  private String host;

  /**
   * Port: Timestamp + Random(0-10000)
   */
  private String port;

  /**
   * type of {@link InstanceType}
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "instance_type")
  private InstanceType instanceType;

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

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
