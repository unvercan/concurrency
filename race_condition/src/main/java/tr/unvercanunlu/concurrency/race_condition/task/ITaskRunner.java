package tr.unvercanunlu.concurrency.race_condition.task;

import tr.unvercanunlu.concurrency.race_condition.counter.ICounter;

public interface ITaskRunner {

  void run(ICounter counter, long increments, long decrements, long retrieves);

}
