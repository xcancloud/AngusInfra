package cloud.xcan.angus.job.web;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Minimal Boot configuration for {@code job-starter} (no {@code @SpringBootApplication} main).
 * <p>
 * {@code @SpringBootConfiguration} does not imply {@code @ComponentScan} (unlike
 * {@code @SpringBootApplication}). For {@code @WebMvcTest(controllers = ...)} the slice expects
 * controllers to be picked up from that scan; without it, no {@code RequestMappingHandlerMapping}
 * entries exist and every request hits {@code ResourceHttpRequestHandler} (404). Explicit
 * {@code @Import} registers the MVC types under test.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@Import({JobController.class})
public class JobWebMvcSliceApplication {

}
