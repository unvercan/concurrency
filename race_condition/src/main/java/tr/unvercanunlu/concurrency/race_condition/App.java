package tr.unvercanunlu.concurrency.race_condition;

import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import tr.unvercanunlu.concurrency.race_condition.config.Config;
import tr.unvercanunlu.concurrency.race_condition.counter.ICounter;
import tr.unvercanunlu.concurrency.race_condition.counter.impl.AtomicCounter;
import tr.unvercanunlu.concurrency.race_condition.counter.impl.Counter;
import tr.unvercanunlu.concurrency.race_condition.counter.impl.LockedCounter;
import tr.unvercanunlu.concurrency.race_condition.task.ITaskRunner;
import tr.unvercanunlu.concurrency.race_condition.task.impl.TaskRunner;
import tr.unvercanunlu.concurrency.race_condition.util.ValueUtil;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class App {

  // runner
  private final ITaskRunner taskRunner = new TaskRunner();

  // logger
  private final Logger logger = Logger.getLogger(this.getClass().getName());

  public static void main(String[] args) {
    App app = new App();

    app.problem();
    app.solutionWithAtomic();
    app.solutionWithLock();
  }

  @SneakyThrows
  private void problem() {
    logger.log(Level.INFO, "Problem: Race Condition, Reason: Multiple threads can access and modify shared data concurrently.");

    // write
    long increments = ValueUtil.generateNumber(Config.OPERATION_COUNT_MIN, Config.OPERATION_COUNT_MAX);

    // write
    long decrements = ValueUtil.generateNumber(Config.OPERATION_COUNT_MIN, Config.OPERATION_COUNT_MAX);

    // retrieve
    long retrieves = ValueUtil.generateNumber(Config.OPERATION_COUNT_MIN, Config.OPERATION_COUNT_MAX);

    ICounter counter = new Counter();
    taskRunner.run(counter, increments, decrements, retrieves);
  }

  @SneakyThrows
  private void solutionWithLock() {
    logger.log(Level.INFO, "Problem: Race Condition, Reason: Multiple threads can access and modify shared data concurrently, "
        + "Solution with Synchronized: At most one thread can modify shared data.");

    // write
    long increments = ValueUtil.generateNumber(Config.OPERATION_COUNT_MIN, Config.OPERATION_COUNT_MAX);

    // write
    long decrements = ValueUtil.generateNumber(Config.OPERATION_COUNT_MIN, Config.OPERATION_COUNT_MAX);

    // retrieve
    long retrieves = ValueUtil.generateNumber(Config.OPERATION_COUNT_MIN, Config.OPERATION_COUNT_MAX);

    ICounter counter = new LockedCounter();
    taskRunner.run(counter, increments, decrements, retrieves);
  }

  @SneakyThrows
  private void solutionWithAtomic() {
    logger.log(Level.INFO,
        "Problem: Race Condition, Reason: Multiple threads can access and modify shared data concurrently, Solution with Atomicity");

    // write
    long increments = ValueUtil.generateNumber(Config.OPERATION_COUNT_MIN, Config.OPERATION_COUNT_MAX);

    // write
    long decrements = ValueUtil.generateNumber(Config.OPERATION_COUNT_MIN, Config.OPERATION_COUNT_MAX);

    // retrieve
    long retrieves = ValueUtil.generateNumber(Config.OPERATION_COUNT_MIN, Config.OPERATION_COUNT_MAX);

    ICounter counter = new AtomicCounter();
    taskRunner.run(counter, increments, decrements, retrieves);
  }

}
