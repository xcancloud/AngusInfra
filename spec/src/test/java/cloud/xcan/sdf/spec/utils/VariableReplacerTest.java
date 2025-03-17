package cloud.xcan.sdf.spec.utils;

import static cloud.xcan.sdf.spec.experimental.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class VariableReplacerTest {

  @Test
  public void testReplaceString(){
    String input = "Hello, ${name}! Your age is ${age}.";
    Map<String, Object> variables = Map.of("name", "John", "age", "30");
    String result = VariableReplacer.replaceVariables(input, variables);
    String expected = "Hello, John! Your age is 30.";
    Assert.assertEquals(expected, result);
  }

  @Test
  public void testReplaceFile() throws IOException {
    URL resource = ClassLoader.getSystemResource("VariableReplacer.txt");
    File outFile = new File(new File(resource.getFile()).getParent()
        + File.separator + "VariableReplacer-Result.txt");
    VariableReplacer.replaceVariables(resource.getFile(), outFile.getPath(), Map.of("d","d", "zz", "z"));
    String result = FileUtils.readFileToString(outFile, UTF_8);
    String expected = "abc\n"
        + "abcde${fg}\n"
        + "wyz\n"
        + "xyz\n";
    Assert.assertEquals(expected, result);
  }

}
