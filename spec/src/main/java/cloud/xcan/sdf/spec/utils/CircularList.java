package cloud.xcan.sdf.spec.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CircularList implements ReadDynamicValue {

  private List<String> dataList;
  private boolean restartFromBeginning;
  private int currentIndex;
  private ReentrantReadWriteLock readWriteLock;

  public static final String END_CHARS = "EOF";


  /**
   * Constructor to initialize with the list of strings.
   *
   * @param dataList             List of strings to read from
   * @param restartFromBeginning true to restart from the beginning once the end is reached, false
   *                             otherwise
   */
  public CircularList(List<String> dataList, boolean restartFromBeginning) {
    this.dataList = dataList;
    this.restartFromBeginning = restartFromBeginning;
    this.currentIndex = 0;
    this.readWriteLock = new ReentrantReadWriteLock();
  }

  /**
   * Read the next value from the list.
   *
   * @return The next value in the list, or null if the list is empty, or `EOF` if read to the end
   */
  @Override
  public String readNext() {
    if (dataList.isEmpty()) {
      return null;
    }

    readWriteLock.readLock().lock();
    try {
      if (currentIndex >= dataList.size()) {
        if (restartFromBeginning) {
          currentIndex = 0; // Ensure currentIndex is at the end
        } else {
          return END_CHARS; // Mark read to the end
        }
      }
      String value = dataList.get(currentIndex);
      currentIndex++;
      return value;
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    CircularList cloned = (CircularList) super.clone();
    cloned.dataList = new ArrayList<>(dataList);
    cloned.restartFromBeginning = restartFromBeginning;
    cloned.currentIndex = currentIndex;
    //cloned.readWriteLock = readWriteLock;
    cloned.readWriteLock = new ReentrantReadWriteLock();
    return cloned;
  }
}
