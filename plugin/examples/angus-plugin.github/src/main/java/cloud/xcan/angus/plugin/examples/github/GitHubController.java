package cloud.xcan.angus.plugin.examples.github;

import cloud.xcan.angus.plugin.api.PluginController;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GitHubController extends PluginController {

  private final HttpClient http = HttpClient.newHttpClient();

  @GetMapping("/repos/readme")
  public String getReadme(@RequestParam String owner, @RequestParam String repo)
      throws IOException, InterruptedException {
    String url = String.format("https://raw.githubusercontent.com/%s/%s/main/README.md", owner,
        repo);
    HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
    HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
    if (resp.statusCode() == 200) {
      return resp.body();
    }
    return "";
  }
}

