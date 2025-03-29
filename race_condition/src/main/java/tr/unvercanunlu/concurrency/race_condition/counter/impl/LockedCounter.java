package tr.unvercanunlu.concurrency.race_condition.counter.impl;

import tr.unvercanunlu.concurrency.race_condition.counter.ICounter;

// Ensure only one thread can modify the value at a time by locking
public class LockedCounter implements ICounter {

  private long value = 0;

  @Override
  public synchronized long retrieve() {
    return value;
  }

  @Override
  public synchronized void increment() {
    value += 1;
  }

  @Override
  public synchronized void decrement() {
    value -= 1;
  }

  @Override
  public synchronized void reset() {
    value = 0;
  }

}
