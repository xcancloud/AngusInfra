package cloud.xcan.sdf.spec.utils;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class CircularListTest {

  @Test
  public void testCircularRead() {
    List<String> dataList = List.of("A", "B", "C");
    CircularList reader = new CircularList(dataList, true);

    // Read all elements in a loop

    String[] results = new String[5];
    for (int i = 0; i < 5; i++) {
      String value = reader.readNext(); // Set to true to restart from beginning
      results[i] = value;
    }
    Assert.assertArrayEquals(results, new String[]{"A", "B", "C", "A", "B"});

    reader = new CircularList(dataList, false);
    results = new String[5];
    for (int i = 0; i < 5; i++) {
      String value = reader.readNext(); // Set to true to restart from ending
      results[i] = value;
    }
    Assert.assertArrayEquals(results, new String[]{"A", "B", "C", "EOF", "EOF"});
  }

}
