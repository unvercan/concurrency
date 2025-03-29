package tr.unvercanunlu.concurrency.data_inconsistency.task.impl;

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
import java.util.stream.LongStream;
import lombok.SneakyThrows;
import tr.unvercanunlu.concurrency.data_inconsistency.config.Config;
import tr.unvercanunlu.concurrency.data_inconsistency.model.dto.AccountDto;
import tr.unvercanunlu.concurrency.data_inconsistency.service.IAccountService;
import tr.unvercanunlu.concurrency.data_inconsistency.task.ITaskRunner;
import tr.unvercanunlu.concurrency.data_inconsistency.thread.OperationThread;
import tr.unvercanunlu.concurrency.data_inconsistency.util.ValueUtil;

public class TaskRunner implements ITaskRunner {

  // logger
  private final Logger logger = Logger.getLogger(this.getClass().getName());

  @SneakyThrows
  @Override
  public void run(IAccountService service, long accounts, long deposits, long withdraws, long checkBalances) {
    String message = "Parameters: #accounts=%d #deposits=%d #check-balances=%d #withdraws=%d".formatted(accounts, deposits, checkBalances, withdraws);
    logger.log(Level.INFO, message);

    Set<String> ibans = LongStream.range(0, accounts)
        .mapToObj(i -> service.open())
        .map(AccountDto::getIban)
        .peek(iban -> System.out.println("Account with " + iban + " IBAN is opened"))
        .collect(Collectors.toSet());

    logger.log(Level.INFO, "Total {0} accounts are opened.", accounts);

    Map<String, Double> expectedBalances = new HashMap<>();

    Map<String, Set<OperationThread>> threadMap = new HashMap<>();

    for (String iban : ibans) {
      double expectedBalance = 0d;

      for (int i = 0; i < deposits; i++) {
        double amount = ValueUtil.generateNumber(Config.DEPOSIT_AMOUNT_MIN, Config.DEPOSIT_AMOUNT_MAX);

        String name = "thread-%s-%s-%d".formatted(iban, "deposit", i);

        Runnable task = () -> {
          try {
            service.deposit(iban, amount);

          } catch (Exception e) {
            logger.log(Level.SEVERE, "Deposit operation failed: {0}", e.getMessage());

            expectedBalances.put(iban, expectedBalances.get(iban) - amount);
          }
        };

        OperationThread thread = new OperationThread(name, task, iban, amount);

        threadMap.computeIfAbsent(iban, key -> new HashSet<>()).add(thread);

        expectedBalance += amount;
      }

      for (int i = 0; i < withdraws; i++) {
        double amount = ValueUtil.generateNumber(Config.WITHDRAW_AMOUNT_MIN, Config.WITHDRAW_AMOUNT_MAX);

        String name = "thread-%s-%s-%d".formatted(iban, "withdraw", i);

        Runnable task = () -> {
          try {
            service.withdraw(iban, amount);

          } catch (Exception e) {
            logger.log(Level.SEVERE, "Withdraw operation failed: {0}", e.getMessage());

            expectedBalances.put(iban, expectedBalances.get(iban) + amount);
          }
        };

        OperationThread thread = new OperationThread(name, task, iban, amount);

        threadMap.computeIfAbsent(iban, key -> new HashSet<>()).add(thread);

        expectedBalance -= amount;
      }

      for (int i = 0; i < checkBalances; i++) {
        String name = "thread-%s-%s-%d".formatted(iban, "check-balance", i);

        Runnable task = () -> {
          try {
            service.get(iban);

          } catch (Exception e) {
            logger.log(Level.SEVERE, "Check balance operation failed: {0}", e.getMessage());
          }
        };

        OperationThread thread = new OperationThread(name, task, iban, 0);

        threadMap.computeIfAbsent(iban, key -> new HashSet<>()).add(thread);
      }

      expectedBalances.put(iban, expectedBalance);
    }

    Duration timeout = Duration.of(10, ChronoUnit.SECONDS);

    Set<OperationThread> threads = threadMap.values()
        .stream()
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());

    logger.log(Level.INFO, "Total {0} threads are created. Threads are starting.", threads.size());

    long start = System.nanoTime();

    // start threads
    threads.forEach(Thread::start);

    logger.log(Level.INFO, "All threads are started. Waiting for completing all threads...");

    // waits threads to complete
    for (OperationThread thread : threads) {
      try {
        thread.join(timeout.toMillis());

      } catch (InterruptedException e) {
        logger.log(Level.SEVERE, "Thread %s is interrupted: %s".formatted(thread.getName(), e.getMessage()));

        thread.interrupt();
      }
    }

    while (!threads.stream().map(OperationThread::getState).allMatch(state -> state.equals(State.TERMINATED))) {
      logger.log(Level.INFO, "Waiting...");
    }

    long end = System.nanoTime();

    logger.log(Level.INFO, "Waiting for completing all threads is completed.");

    Duration duration = Duration.of(end - start, ChronoUnit.MILLIS);
    logger.log(Level.INFO, "Duration: {0} milliseconds.", duration.toMillis());

    long dataInconsistentAccounts = 0;

    for (String iban : ibans) {
      AccountDto account = service.get(iban);

      double actual = account.getBalance();
      double expected = expectedBalances.get(iban);
      double error = actual - expectedBalances.get(iban);

      message = "IBAN=%s Actual=%f Expected=%f Error=%f".formatted(iban, actual, expected, error);
      logger.log(Level.INFO, message);

      if (error != 0) {
        dataInconsistentAccounts++;
      }
    }

    message = "Data inconsistency occurs in %d accounts.".formatted(dataInconsistentAccounts);
    logger.log(Level.INFO, message);
  }

}
