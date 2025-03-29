package tr.unvercanunlu.concurrency.race_condition.counter.impl;

import java.util.concurrent.atomic.AtomicLong;
import tr.unvercanunlu.concurrency.race_condition.counter.ICounter;

// Ensure atomic operations
public class AtomicCounter implements ICounter {

  private final AtomicLong value = new AtomicLong(0);

  @Override
  public long retrieve() {
    return value.get();
  }

  @Override
  public void increment() {
    value.getAndIncrement();
  }

  @Override
  public void decrement() {
    value.getAndDecrement();
  }

  @Override
  public void reset() {
    value.set(0);
  }

}
