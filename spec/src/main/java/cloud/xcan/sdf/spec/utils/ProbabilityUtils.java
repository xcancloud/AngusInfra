package cloud.xcan.sdf.spec.utils;

import java.util.Random;

public class ProbabilityUtils {

  private final static Random random = new Random();

  private ProbabilityUtils() {
  }

  /**
   * Return true with a probability of 1/n; otherwise, return false.
   *
   * @param n The denominator of the probability.
   * @return Return true with a probability of 1/n.
   */
  public static boolean randomWithProbability(int n) {
    // Generate a random number between 0 and n-1.
    int randomNumber = random.nextInt(n);
    // If the generated random number is 0, return true; otherwise, return false.
    return randomNumber == 0;
  }

}
