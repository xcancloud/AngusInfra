package cloud.xcan.angus.job.web;

import jakarta.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates exceptions thrown by job-module endpoints into structured HTTP responses.
 *
 * <h3>Security (CWE-209: Information Exposure Through Error Message)</h3>
 * <ul>
 *   <li>Generic exceptions ({@link Exception}) return only a fixed "Internal Server Error"
 *       message.  The full stack trace is written to the log — never to the response body.</li>
 *   <li>Business exceptions ({@link IllegalArgumentException}, {@link IllegalStateException},
 *       {@link EntityNotFoundException}) convey user-facing messages because they are
 *       intentionally constructed with safe, non-sensitive text.</li>
 * </ul>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /**
   * Handles Bean Validation failures ({@code @Valid} on controller parameters). Returns HTTP 400
   * with field → message pairs — safe to expose.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(
      MethodArgumentNotValidException ex) {
    Map<String, String> fieldErrors = new HashMap<>();
    for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
      fieldErrors.put(fe.getField(), fe.getDefaultMessage());
    }
    return ResponseEntity.badRequest()
        .body(errorBody("Validation failed", fieldErrors));
  }

  /**
   * Handles explicit bad-input signals raised by the service layer. Returns HTTP 400 with the
   * exception's message (assumed to be safe).
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
    return ResponseEntity.badRequest()
        .body(errorBody(ex.getMessage(), null));
  }

  /**
   * Handles state-machine violations (e.g. triggering a RUNNING job). Returns HTTP 409 Conflict.
   */
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(errorBody(ex.getMessage(), null));
  }

  /**
   * Handles lookups for non-existent resources. Returns HTTP 404.
   */
  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleNotFound(EntityNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(errorBody(ex.getMessage(), null));
  }

  /**
   * Catch-all for unexpected exceptions.
   *
   * <p>The exception details are written to the log with a correlation hint but
   * are NOT included in the response body to prevent information leakage (CWE-209).
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
    log.error("Unhandled exception in job module", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(errorBody("Internal server error. Please contact the administrator.", null));
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private static Map<String, Object> errorBody(String message, Object details) {
    Map<String, Object> body = new HashMap<>();
    body.put("success", false);
    body.put("message", message);
    if (details != null) {
      body.put("details", details);
    }
    return body;
  }
}
