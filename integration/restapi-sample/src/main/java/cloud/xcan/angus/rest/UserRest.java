package cloud.xcan.angus.rest;

import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.domain.User;
import cloud.xcan.angus.domain.UserRepo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.rest.dto.UserAddDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "User Management", description = "Operations related to user management")
public class UserRest {

  @Resource
  private UserRepo userRepo;

  @Operation(
      summary = "Create user", description = "Register new user with department and group assignments"
  )
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "User created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid input"),
      @ApiResponse(responseCode = "409", description = "Username already exists")
  })
  @PostMapping
  @Transactional
  public ApiLocaleResult<User> createUser(@RequestBody @Valid UserAddDto request) {
    User oldUser = userRepo.findByUsername(request.username());
    ProtocolAssert.assertResourceExisted(oldUser, request.username());

    User newUser = new User().setUsername(request.username())
        .setEmail(request.email());
    userRepo.save(newUser);
    return ApiLocaleResult.success(newUser);
  }

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
