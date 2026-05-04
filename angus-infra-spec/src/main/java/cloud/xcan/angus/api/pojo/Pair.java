package cloud.xcan.angus.api.pojo;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Setter
@Getter
public final class Pair<K, V> implements Entry<K, V>, Comparator<Pair<K, V>> {

  public K name;
  public V value;

  public Pair() { // For jackson
  }

  public Pair(K name, V value) {
    this.name = name;
    this.value = value;
  }

  public K first() {
    return name;
  }

  public V second() {
    return value;
  }

  public static <K, V> Pair<K, V> of(K k, V v) {
    return new Pair<>(k, v);
  }

  public static String format(List<Pair<String, String>> pairs) {
    return format(pairs, ";", "=");
  }

  public static String format(List<Pair<String, String>> pairs, String pairSeparator,
      String paramSeparator) {
    if (isEmpty(pairs)) {
      return null;
    }
    pairSeparator = nullSafe(pairSeparator, ";");
    paramSeparator = nullSafe(paramSeparator, "=");
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < pairs.size(); i++) {
      if (i > 0) {
        buf.append(pairSeparator);
      }
      buf.append(pairs.get(i).name).append(paramSeparator).append(pairs.get(i).value);
    }
    return buf.toString();
  }

  public static List<Pair<String, String>> parse(String pairsString) {
    return parse(pairsString, ";", "=");
  }

  public static List<Pair<String, String>> parse(String pairsString, String pairSeparator,
      String paramSeparator) {
    if (isEmpty(pairsString)) {
      return null;
    }
    pairSeparator = nullSafe(pairSeparator, ";");
    paramSeparator = nullSafe(paramSeparator, "=");
    String[] pairsArray = pairsString.split(pairSeparator);
    List<Pair<String, String>> pairs = new ArrayList<>(pairsArray.length);
    for (String pair : pairsArray) {
      String[] params = pair.split(paramSeparator);
      pairs.add(Pair.of(params[0], params[1]));
    }
    return pairs;
  }

  @JsonIgnore
  @Schema(hidden = true)
  @Override
  public K getKey() {
    return name;
  }

  @Override
  public V setValue(V value) {
    this.value = value;
    return value;
  }

  @Override
  public int compare(Pair<K, V> o1, Pair<K, V> o2) {
    return o1.getName().toString().compareTo(o2.getName().toString());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Pair)) {
      return false;
    }
    Pair<?, ?> pair = (Pair<?, ?>) o;
    return Objects.equals(name, pair.name) && Objects.equals(value, pair.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, value);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("name", name)
        .append("value", value)
        .toString();
  }
}
