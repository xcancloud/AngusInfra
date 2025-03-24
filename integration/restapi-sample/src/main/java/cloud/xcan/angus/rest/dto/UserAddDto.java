package cloud.xcan.angus.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record UserAddDto(

    @NotEmpty
    @Schema(description = "Unique username", example = "john_doe",
        requiredMode = RequiredMode.REQUIRED)
    String username,

    @Email
    @Schema(description = "Valid email address", example = "john@example.com",
        requiredMode = RequiredMode.REQUIRED)
    String email
) {

}
