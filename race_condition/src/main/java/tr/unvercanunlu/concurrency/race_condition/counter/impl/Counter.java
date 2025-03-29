package tr.unvercanunlu.concurrency.race_condition.counter.impl;

import tr.unvercanunlu.concurrency.race_condition.counter.ICounter;

public class Counter implements ICounter {

  private long value = 0;

  @Override
  public long retrieve() {
    return value;
  }

  @Override
  public void increment() {
    value += 1;
  }

  @Override
  public void decrement() {
    value -= 1;
  }

  @Override
  public void reset() {
    value = 0;
  }

}
