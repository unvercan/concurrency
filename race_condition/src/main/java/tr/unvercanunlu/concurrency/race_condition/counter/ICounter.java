package tr.unvercanunlu.concurrency.race_condition.counter;

public interface ICounter {

  long retrieve();

  void increment();

  void decrement();

  void reset();

}
