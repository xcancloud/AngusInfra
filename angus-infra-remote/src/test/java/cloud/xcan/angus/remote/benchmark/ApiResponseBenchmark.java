package cloud.xcan.angus.remote.benchmark;

import cloud.xcan.angus.remote.ApiResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * API响应序列化性能基准测试
 * <p>
 * 测试以下场景： 1. 简单对象序列化（Object只含原始类型字段） 2. 复杂对象序列化（包含嵌套对象） 3. 集合序列化（List<Object>） 4. 反序列化性能
 * <p>
 * 目标：序列化延迟 < 1ms（1000个对象），吞吐量 > 100K ops/sec
 * <p>
 * 运行方式： mvn exec:java -Dexec.mainClass="cloud.xcan.angus.remote.benchmark.ApiResponseBenchmark"
 */
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 2)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class ApiResponseBenchmark {

  private ObjectMapper objectMapper;
  private ApiResult<String> simpleResult;
  private ApiResult<UserDTO> complexResult;
  private ApiResult<List<UserDTO>> listResult;
  private String simpleJson;
  private String complexJson;
  private String listJson;

  @Setup
  public void setup() throws IOException {
    objectMapper = new ObjectMapper();

    // 简单结果对象（避免与 success(String message) 重载冲突，data 为 String 时用构造器）
    simpleResult = new ApiResult<>("Hello World");

    // 复杂结果对象（包含嵌套对象）
    UserDTO user = new UserDTO();
    user.setId(1L);
    user.setUsername("john.doe");
    user.setEmail("john@example.com");
    user.setAge(30);
    user.setActive(true);
    complexResult = ApiResult.success(user);

    // 列表结果对象
    List<UserDTO> users = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      UserDTO u = new UserDTO();
      u.setId((long) i);
      u.setUsername(String.format("user_%d", i));
      u.setEmail(String.format("user_%d@example.com", i));
      u.setAge(20 + i);
      u.setActive(i % 2 == 0);
      users.add(u);
    }
    listResult = ApiResult.success(users);

    // 预先序列化JSON
    simpleJson = objectMapper.writeValueAsString(simpleResult);
    complexJson = objectMapper.writeValueAsString(complexResult);
    listJson = objectMapper.writeValueAsString(listResult);
  }

  /**
   * 序列化简单结果对象（String）
   */
  @Benchmark
  public String serializeSimpleResult() throws IOException {
    return objectMapper.writeValueAsString(simpleResult);
  }

  /**
   * 序列化复杂结果对象（嵌套对象）
   */
  @Benchmark
  public String serializeComplexResult() throws IOException {
    return objectMapper.writeValueAsString(complexResult);
  }

  /**
   * 序列化列表结果对象
   */
  @Benchmark
  public String serializeListResult() throws IOException {
    return objectMapper.writeValueAsString(listResult);
  }

  /**
   * 反序列化简单结果对象
   */
  @Benchmark
  public ApiResult<String> deserializeSimpleResult() throws IOException {
    return objectMapper.readValue(simpleJson,
        objectMapper.getTypeFactory().constructParametricType(ApiResult.class, String.class));
  }

  /**
   * 反序列化复杂结果对象
   */
  @Benchmark
  public ApiResult<UserDTO> deserializeComplexResult() throws IOException {
    return objectMapper.readValue(complexJson,
        objectMapper.getTypeFactory().constructParametricType(ApiResult.class, UserDTO.class));
  }

  /**
   * 反序列化列表结果对象
   */
  @Benchmark
  @SuppressWarnings("unchecked")
  public ApiResult<List<UserDTO>> deserializeListResult() throws IOException {
    return objectMapper.readValue(listJson,
        objectMapper.getTypeFactory().constructParametricType(ApiResult.class,
            objectMapper.getTypeFactory().constructCollectionType(List.class, UserDTO.class)));
  }

  /**
   * 批量序列化（10个对象）
   */
  @Benchmark
  public List<String> batchSerialize() throws IOException {
    List<String> results = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      UserDTO user = new UserDTO();
      user.setId((long) i);
      user.setUsername(String.format("user_%d", i));
      user.setEmail(String.format("user_%d@example.com", i));
      user.setAge(20 + i);
      user.setActive(i % 2 == 0);
      ApiResult<UserDTO> result = ApiResult.success(user);
      results.add(objectMapper.writeValueAsString(result));
    }
    return results;
  }

  /**
   * 序列化+反序列化往返测试
   */
  @Benchmark
  public UserDTO roundTrip() throws IOException {
    String json = objectMapper.writeValueAsString(complexResult);
    ApiResult<UserDTO> result = objectMapper.readValue(json,
        objectMapper.getTypeFactory().constructParametricType(ApiResult.class, UserDTO.class));
    return result.getData();
  }

  /**
   * 测试方法数据模型
   */
  public static class UserDTO {

    private Long id;
    private String username;
    private String email;
    private Integer age;
    private Boolean active;

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public Integer getAge() {
      return age;
    }

    public void setAge(Integer age) {
      this.age = age;
    }

    public Boolean getActive() {
      return active;
    }

    public void setActive(Boolean active) {
      this.active = active;
    }
  }

  /**
   * 运行所有基准测试
   */
  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
        .include(ApiResponseBenchmark.class.getSimpleName())
        .result("benchmark_api_response.json")
        .resultFormat(org.openjdk.jmh.results.format.ResultFormatType.JSON)
        .build();

    new Runner(opt).run();
  }
}
