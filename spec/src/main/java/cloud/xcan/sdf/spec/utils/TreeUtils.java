package cloud.xcan.sdf.spec.utils;


import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TreeUtils {

  private TreeUtils() { /* no instance */ }

  @SuppressWarnings("all")
  public static <T extends TreeNode> List<T> toTree(List<T> treeNodes) {
    return toTree(treeNodes, true);
  }

  @SuppressWarnings("all")
  public static <T extends TreeNode> List<T> toTree(List<T> treeNodes, boolean sort) {
    List<Long> orgIds = treeNodes.stream().map(TreeNode::getId).collect(Collectors.toList());
    List<T> collect = treeNodes.stream()
        .filter(o -> o.getPid() == -1L || o.getPid() == null || !orgIds.contains(o.getPid()))
        .map((o) -> {
              o.setChildren((List<T>) getChildren(o, treeNodes, sort));
              return o;
            }
        ).collect(Collectors.toList());
    if (sort) {
      collect.sort(Comparator.comparingLong(TreeNode::getSequence0));
    }
    return collect;
  }

  @SuppressWarnings("all")
  private static <T extends TreeNode> List<T> getChildren(T root, List<T> treeNodes, boolean sort) {
    List<T> collect = treeNodes.stream().filter(o -> {
      return Objects.equals(o.getPid(), root.getId());
    }).map((o) -> {
          o.setChildren((List<T>) getChildren(o, treeNodes, sort));
          return o;
        }
    ).collect(Collectors.toList());
    if (sort) {
      collect.sort(Comparator.comparingLong(TreeNode::getSequence0));
    }
    return collect;
  }

  public interface TreeNode<T> {

    Long getId();

    Long getPid();

    default Long getSequence0() {
      return getId();
    }

    void setChildren(List<T> children);

  }
}
