package cloud.xcan.angus.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "user")
@Setter
@Getter
@Accessors(chain = true)
public class User {

  @Id
  @Column(name = "username", unique = true, nullable = false)
  private String username;

  @Column(name = "email", nullable = false)
  private String email;

}
