package tr.unvercanunlu.concurrency.race_condition.task.impl;

import java.lang.Thread.State;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import tr.unvercanunlu.concurrency.race_condition.counter.ICounter;
import tr.unvercanunlu.concurrency.race_condition.task.ITaskRunner;

public class TaskRunner implements ITaskRunner {

  // logger
  private final Logger logger = Logger.getLogger(this.getClass().getName());

  @SneakyThrows
  @Override
  public void run(ICounter counter, long increments, long decrements, long retrieves) {
    String message = "Parameters: #increments=%d #decrements=%d #retrieves=%d".formatted(increments, decrements, retrieves);
    logger.log(Level.INFO, message);

    long expected = increments - decrements;

    Map<Integer, Set<Thread>> threadMap = new HashMap<>();

    for (int i = 0; i < increments; i++) {
      String name = "thread-%s-%d".formatted("increment", i);

      Runnable task = () -> {
        try {
          counter.increment();

        } catch (Exception e) {
          logger.log(Level.SEVERE, "Increment operation failed: {0}", e.getMessage());
        }
      };

      Thread thread = new Thread(task, name);

      threadMap.computeIfAbsent(1, change -> new HashSet<>()).add(thread);
    }

    for (int i = 0; i < decrements; i++) {
      String name = "thread-%s-%d".formatted("decrement", i);

      Runnable task = () -> {
        try {
          counter.decrement();

        } catch (Exception e) {
          logger.log(Level.SEVERE, "Decrement operation failed: {0}", e.getMessage());
        }
      };

      Thread thread = new Thread(task, name);

      threadMap.computeIfAbsent(-1, change -> new HashSet<>()).add(thread);
    }

    for (int i = 0; i < decrements; i++) {
      String name = "thread-%s-%d".formatted("retrieve", i);

      Runnable task = () -> {
        try {
          counter.retrieve();

        } catch (Exception e) {
          logger.log(Level.SEVERE, "Retrieve operation failed: {0}", e.getMessage());
        }
      };

      Thread thread = new Thread(task, name);

      threadMap.computeIfAbsent(0, change -> new HashSet<>()).add(thread);
    }

    Duration timeout = Duration.of(10, ChronoUnit.SECONDS);

    Set<Thread> threads = threadMap.values()
        .stream()
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());

    logger.log(Level.INFO, "Total {0} threads are created. Threads are starting.", threads.size());

    long start = System.nanoTime();

    // start threads
    threads.forEach(Thread::start);

    logger.log(Level.INFO, "All threads are started. Waiting for completing all threads...");

    // waits threads to complete
    for (Thread thread : threads) {
      try {
        thread.join(timeout.toMillis());

      } catch (InterruptedException e) {
        logger.log(Level.SEVERE, "Thread %s is interrupted: %s".formatted(thread.getName(), e.getMessage()));

        thread.interrupt();
      }
    }

    while (!threads.stream().map(Thread::getState).allMatch(state -> state.equals(State.TERMINATED))) {
      logger.log(Level.INFO, "Waiting...");
    }

    long end = System.nanoTime();

    logger.log(Level.INFO, "Waiting for completing all threads is completed.");

    Duration duration = Duration.of(end - start, ChronoUnit.MILLIS);
    logger.log(Level.INFO, "Duration: {0} milliseconds.", duration.toMillis());

    long actual = counter.retrieve();
    long error = actual - expected;

    message = "Counter: Actual=%d Expected=%d Error=%d".formatted(actual, expected, error);
    logger.log(Level.INFO, message);
  }

}
