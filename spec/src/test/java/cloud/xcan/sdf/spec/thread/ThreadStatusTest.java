package cloud.xcan.sdf.spec.thread;

import org.junit.Test;

public class ThreadStatusTest {

  @Test
  public void testThreadStatus() throws InterruptedException {
    // Create a new thread
    Thread thread = new Thread(() -> {
      try {
        Thread.sleep(2000); // Simulate thread execution tasks
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    });

    // Start Thread
    thread.start();

    // Check thread status
    Thread.State state = thread.getState();
    System.out.println("Thread State: " + state); //-> Thread State: RUNNABLE

    // Wait for a period of time
    Thread.sleep(1000);

    // Recheck thread status
    state = thread.getState();
    System.out.println("Thread State: " + state); //-> Thread State: TIMED_WAITING

    // Waiting for thread execution to completed
    thread.join();

    // Finally, check the thread status
    state = thread.getState();
    System.out.println("Thread State: " + state); //-> Thread State: TERMINATED
  }
}
