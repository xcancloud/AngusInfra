package cloud.xcan.angus.spec;

import java.util.StringJoiner;

class CloneObject1 implements Cloneable {

  private int id;
  private String name;

  private MyObject2 object2;
  private MyObject3 object3;

  public CloneObject1(int id, String name, MyObject2 object2, MyObject3 object3) {
    this.id = id;
    this.name = name;
    this.object2 = object2;
    this.object3 = object3;
  }

  public MyObject2 getObject2() {
    return object2;
  }

  public MyObject3 getObject3() {
    return object3;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    CloneObject1 object1 = (CloneObject1) super.clone();
    object1.object2 = (MyObject2) object2.clone();
    //object1.object3 = (MyObject3) object3.clone();
    return object1;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", CloneObject1.class.getSimpleName() + "[", "]")
        .add("id=" + id)
        .add("name='" + name + "'")
        .add("object2=" + object2)
        .toString();
  }
}

class MyObject2 implements Cloneable {

  private String name;

  public MyObject2(String name) {
    this.name = name;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", MyObject2.class.getSimpleName() + "[", "]")
        .add("id=" + name)
        .toString();
  }
}

class MyObject3 {

  private String name;

  public MyObject3(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", MyObject2.class.getSimpleName() + "[", "]")
        .add("id=" + name)
        .toString();
  }
}

public class CloneObjectTest {

  public static void main(String[] args) {
    CloneObject1 originalObject = new CloneObject1(1, "Original",
        new MyObject2("Original Object2"), new MyObject3("Original Object3"));
    System.out.println("Original Object: " + originalObject);
    System.out.println("Original Object: " + originalObject.hashCode());
    System.out.println("Original Object2: " + originalObject.getObject2().hashCode());
    System.out.println("Original Object3: " + originalObject.getObject3().hashCode());

    try {
      CloneObject1 clonedObject = (CloneObject1) originalObject.clone();
      System.out.println("Cloned Object: " + clonedObject);
      System.out.println("Cloned Object: " + clonedObject.hashCode());
      System.out.println("Cloned Object2: " + clonedObject.getObject2().hashCode());
      System.out.println("Cloned Object3: " + clonedObject.getObject3().hashCode());
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
  }
}
