package tr.unvercanunlu.concurrency.race_condition.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Config {

  public static final long OPERATION_COUNT_MIN = 10_000;
  public static final long OPERATION_COUNT_MAX = 25_000;

}
