package tr.unvercanunlu.concurrency.data_inconsistency.task;

import tr.unvercanunlu.concurrency.data_inconsistency.service.IAccountService;

public interface ITaskRunner {

  void run(IAccountService service, long accounts, long deposits, long withdraws, long checkBalances);

}
