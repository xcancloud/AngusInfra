package cloud.xcan.angus.rest;

import cloud.xcan.angus.domain.User;
import cloud.xcan.angus.domain.UserRepo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/doorapi/v1/user")
@Tag(name = "User Management by Door", description = "Operations related to user management")
public class DoorRest {

  @Resource
  private UserRepo userRepo;

  @Operation(
      summary = "List users", description = "Retrieve paginated list of users",
      parameters = {
          @Parameter(name = "page", description = "Page number", example = "0"),
          @Parameter(name = "size", description = "Items per page", example = "10")
      }
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully retrieved users")
  })
  @GetMapping
  public ApiLocaleResult<PageResult<User>> getUsers(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ApiLocaleResult.success(PageResult.of(userRepo.findAll(PageRequest.of(page, size))));
  }

}
