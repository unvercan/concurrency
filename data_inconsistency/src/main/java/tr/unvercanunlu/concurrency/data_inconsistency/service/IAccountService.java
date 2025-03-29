package tr.unvercanunlu.concurrency.data_inconsistency.service;

import tr.unvercanunlu.concurrency.data_inconsistency.model.dto.AccountDto;

public interface IAccountService {

  AccountDto open();

  void close(String iban);

  AccountDto get(String iban);

  AccountDto withdraw(String iban, double amount);

  AccountDto deposit(String iban, double amount);

  AccountDto transfer(String ibanFrom, String ibanTo, double amount);

}
