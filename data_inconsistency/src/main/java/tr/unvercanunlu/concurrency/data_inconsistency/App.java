package tr.unvercanunlu.concurrency.data_inconsistency;

import java.util.logging.Level;
import java.util.logging.Logger;
import tr.unvercanunlu.concurrency.data_inconsistency.config.Config;
import tr.unvercanunlu.concurrency.data_inconsistency.dao.IAccountDao;
import tr.unvercanunlu.concurrency.data_inconsistency.dao.impl.AccountDao;
import tr.unvercanunlu.concurrency.data_inconsistency.service.IAccountService;
import tr.unvercanunlu.concurrency.data_inconsistency.service.impl.AccountLockAccountService;
import tr.unvercanunlu.concurrency.data_inconsistency.service.impl.AccountReadWriteLockAccountService;
import tr.unvercanunlu.concurrency.data_inconsistency.service.impl.AccountService;
import tr.unvercanunlu.concurrency.data_inconsistency.service.impl.SingleGlobalLockAccountService;
import tr.unvercanunlu.concurrency.data_inconsistency.task.ITaskRunner;
import tr.unvercanunlu.concurrency.data_inconsistency.task.impl.TaskRunner;
import tr.unvercanunlu.concurrency.data_inconsistency.util.ValueUtil;

public class App {

  // runner
  private final ITaskRunner taskRunner = new TaskRunner();

  // logger
  private final Logger logger = Logger.getLogger(this.getClass().getName());

  public static void main(String[] args) {
    App app = new App();

    app.problem();
    app.solutionWithSingleGlobalLock();
    app.solutionWithAccountLock();
    app.solutionWithAccountReadWriteLock();
  }

  public void problem() {
    logger.log(Level.INFO, "Problem - begin");

    // accounts
    long accounts = ValueUtil.generateNumber(Config.ACCOUNT_COUNT_MIN, Config.ACCOUNT_COUNT_MAX);

    // write operations
    long deposits = ValueUtil.generateNumber(Config.DEPOSIT_OPERATION_COUNT_MIN, Config.DEPOSIT_OPERATION_COUNT_MAX);
    long withdraws = ValueUtil.generateNumber(Config.WITHDRAW_OPERATION_COUNT_MIN, Config.WITHDRAW_OPERATION_COUNT_MAX);

    // read operations
    long checkBalances = ValueUtil.generateNumber(Config.CHECK_BALANCE_OPERATION_COUNT_MIN, Config.CHECK_BALANCE_OPERATION_COUNT_MAX);

    IAccountDao dao = new AccountDao();
    IAccountService service = new AccountService(dao);
    taskRunner.run(service, accounts, deposits, withdraws, checkBalances);

    logger.log(Level.INFO, "Problem - end");
  }

  public void solutionWithSingleGlobalLock() {
    logger.log(Level.INFO, "Single Global Lock - begin");

    // accounts
    long accounts = ValueUtil.generateNumber(Config.ACCOUNT_COUNT_MIN, Config.ACCOUNT_COUNT_MAX);

    // write operations
    long deposits = ValueUtil.generateNumber(Config.DEPOSIT_OPERATION_COUNT_MIN, Config.DEPOSIT_OPERATION_COUNT_MAX);
    long withdraws = ValueUtil.generateNumber(Config.WITHDRAW_OPERATION_COUNT_MIN, Config.WITHDRAW_OPERATION_COUNT_MAX);

    // read operations
    long checkBalances = ValueUtil.generateNumber(Config.CHECK_BALANCE_OPERATION_COUNT_MIN, Config.CHECK_BALANCE_OPERATION_COUNT_MAX);

    IAccountDao dao = new AccountDao();
    IAccountService service = new SingleGlobalLockAccountService(dao);
    taskRunner.run(service, accounts, deposits, withdraws, checkBalances);

    logger.log(Level.INFO, "Single Global Lock - end");
  }

  public void solutionWithAccountLock() {
    logger.log(Level.INFO, "Account Lock - begin");

    // accounts
    long accounts = ValueUtil.generateNumber(Config.ACCOUNT_COUNT_MIN, Config.ACCOUNT_COUNT_MAX);

    // write operations
    long deposits = ValueUtil.generateNumber(Config.DEPOSIT_OPERATION_COUNT_MIN, Config.DEPOSIT_OPERATION_COUNT_MAX);
    long withdraws = ValueUtil.generateNumber(Config.WITHDRAW_OPERATION_COUNT_MIN, Config.WITHDRAW_OPERATION_COUNT_MAX);

    // read operations
    long checkBalances = ValueUtil.generateNumber(Config.CHECK_BALANCE_OPERATION_COUNT_MIN, Config.CHECK_BALANCE_OPERATION_COUNT_MAX);

    IAccountDao dao = new AccountDao();
    IAccountService service = new AccountLockAccountService(dao);
    taskRunner.run(service, accounts, deposits, withdraws, checkBalances);

    logger.log(Level.INFO, "Account Lock - end");
  }

  public void solutionWithAccountReadWriteLock() {
    logger.log(Level.INFO, "Account Read-Write Lock - begin");

    // accounts
    long accounts = ValueUtil.generateNumber(Config.ACCOUNT_COUNT_MIN, Config.ACCOUNT_COUNT_MAX);

    // write operations
    long deposits = ValueUtil.generateNumber(Config.DEPOSIT_OPERATION_COUNT_MIN, Config.DEPOSIT_OPERATION_COUNT_MAX);
    long withdraws = ValueUtil.generateNumber(Config.WITHDRAW_OPERATION_COUNT_MIN, Config.WITHDRAW_OPERATION_COUNT_MAX);

    // read operations
    long checkBalances = ValueUtil.generateNumber(Config.CHECK_BALANCE_OPERATION_COUNT_MIN, Config.CHECK_BALANCE_OPERATION_COUNT_MAX);

    IAccountDao dao = new AccountDao();
    IAccountService service = new AccountReadWriteLockAccountService(dao);
    taskRunner.run(service, accounts, deposits, withdraws, checkBalances);

    logger.log(Level.INFO, "Account Read-Write Lock - begin");
  }

}
