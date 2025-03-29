package tr.unvercanunlu.concurrency.data_inconsistency.util;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tr.unvercanunlu.concurrency.data_inconsistency.config.Config;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValueUtil {

  // random
  private static final Random random = new Random(System.nanoTime());

  public static long generateNumber(long min, long max) {
    return min + random.nextLong(max - min);
  }

  public static String generateIban() {
    return Config.COUNTRY_CODE +
        IntStream.range(0, Config.IBAN_DIGIT_LENGTH)
            .mapToLong(i -> ValueUtil.generateNumber(0, 9))
            .mapToObj(String::valueOf)
            .collect(Collectors.joining());
  }

}
