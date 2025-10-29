package cloud.xcan.angus.plugin.examples.jenkins;

import cloud.xcan.angus.plugin.api.PluginController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RestController
public class JenkinsController extends PluginController {

    private final HttpClient http = HttpClient.newHttpClient();

    @PostMapping("/jenkins/job/build")
    public String triggerBuild(@RequestParam String jenkinsUrl, @RequestParam String jobName) throws IOException, InterruptedException {
        String url = String.format("%s/job/%s/build", jenkinsUrl, jobName);
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).POST(HttpRequest.BodyPublishers.noBody()).build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        return resp.statusCode() == 201 ? "queued" : String.valueOf(resp.statusCode());
    }

    @GetMapping("/jenkins/job/status")
    public String getLastBuildStatus(@RequestParam String jenkinsUrl, @RequestParam String jobName) throws IOException, InterruptedException {
        String url = String.format("%s/job/%s/lastBuild/api/json", jenkinsUrl, jobName);
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) return String.valueOf(resp.statusCode());
        return resp.body();
    }
}

