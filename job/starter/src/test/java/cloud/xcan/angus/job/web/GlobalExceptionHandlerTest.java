package cloud.xcan.angus.job.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cloud.xcan.angus.remote.ApiLocaleResult;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
        .standaloneSetup(new ProbeController())
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
  }

  @Test
  void illegalArgumentReturns400() throws Exception {
    mockMvc.perform(get("/probe/illegal-argument"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("E1"))
        .andExpect(jsonPath("$.message").value("bad input"));
  }

  @Test
  void illegalStateReturns409() throws Exception {
    mockMvc.perform(get("/probe/illegal-state"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("E1"));
  }

  @Test
  void notFoundReturns404() throws Exception {
    mockMvc.perform(get("/probe/not-found"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("E1"));
  }

  @Test
  void genericExceptionReturns500Sanitized() throws Exception {
    mockMvc.perform(get("/probe/boom"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.code").value("E2"))
        .andExpect(jsonPath("$.message").value(
            "Internal server error. Please contact the administrator."));
  }

  @RestController
  static class ProbeController {

    @GetMapping("/probe/illegal-argument")
    public ApiLocaleResult<Void> ia() {
      throw new IllegalArgumentException("bad input");
    }

    @GetMapping("/probe/illegal-state")
    public ApiLocaleResult<Void> is() {
      throw new IllegalStateException("conflict");
    }

    @GetMapping("/probe/not-found")
    public ApiLocaleResult<Void> nf() {
      throw new EntityNotFoundException("missing");
    }

    @GetMapping("/probe/boom")
    public ApiLocaleResult<Void> boom() {
      throw new RuntimeException("secret jdbc:mysql://host");
    }
  }
}
