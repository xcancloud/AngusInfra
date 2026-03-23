package cloud.xcan.angus.plugin.support;

import cloud.xcan.angus.plugin.api.PluginController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sample")
public class SamplePluginController extends PluginController {

  @GetMapping("/hello")
  public String hello() {
    return "hello";
  }
}
