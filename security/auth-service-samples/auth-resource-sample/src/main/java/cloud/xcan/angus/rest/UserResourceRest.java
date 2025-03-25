package cloud.xcan.angus;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserResourceRest {

  @GetMapping("/messages")
  public List<Message> messages() {
    return List.of(new Message("Hello"), new Message("Goodbye"));
  }

  public record Message(String message) {

  }

}
