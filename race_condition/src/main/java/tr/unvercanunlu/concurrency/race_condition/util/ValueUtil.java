package tr.unvercanunlu.concurrency.race_condition.util;

import java.util.Random;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValueUtil {

  // random
  private static final Random random = new Random(System.nanoTime());

  public static long generateNumber(long min, long max) {
    return min + random.nextLong(max - min);
  }

}
