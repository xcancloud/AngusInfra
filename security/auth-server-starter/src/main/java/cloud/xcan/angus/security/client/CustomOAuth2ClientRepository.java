package cloud.xcan.angus.security.client;

import java.util.List;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

public interface CustomOAuth2ClientRepository extends RegisteredClientRepository {

   List<CustomOAuth2RegisteredClient> findAllBy(String filter, String... args);

   void deleteByClientId(String clientId);

   void deleteById(String id);
}
