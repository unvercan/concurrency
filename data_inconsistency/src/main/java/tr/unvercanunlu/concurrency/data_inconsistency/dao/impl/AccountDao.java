package tr.unvercanunlu.concurrency.data_inconsistency.dao.impl;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import tr.unvercanunlu.concurrency.data_inconsistency.dao.IAccountDao;
import tr.unvercanunlu.concurrency.data_inconsistency.model.entity.Account;
import tr.unvercanunlu.concurrency.data_inconsistency.repository.IRepository;
import tr.unvercanunlu.concurrency.data_inconsistency.repository.impl.AccountRepository;
import tr.unvercanunlu.concurrency.data_inconsistency.util.ValueUtil;

public class AccountDao implements IAccountDao {

  // repository
  private final IRepository<String, Account> accountRepository = new AccountRepository();

  @Override
  public Account save(Account account) {
    // validations
    if (account == null) {
      throw new IllegalArgumentException("Account not valid!");
    }

    // generate and assign an IBAN if not already set.
    if ((account.getIban() == null) || account.getIban().isBlank()) {
      account.setIban(ValueUtil.generateIban());
    }

    // critical section - begin
    if (accountRepository.getEntities().containsKey(account.getIban())) {
      account.setUpdatedAt(ZonedDateTime.now());
    }

    accountRepository.getEntities()
        .put(account.getIban(), account);
    // critical section - end

    return account;
  }

  @Override
  public Optional<Account> retrieveByID(String iban) {
    // validations
    if ((iban == null) || iban.isBlank()) {
      throw new IllegalArgumentException("IBAN not valid!");
    }

    // critical section - begin
    Account account = accountRepository.getEntities()
        .getOrDefault(iban, null);
    // critical section - end

    return Optional.ofNullable(account);
  }

  @Override
  public List<Account> retrieveAll() {
    // critical section - begin
    return accountRepository.getEntities()
        .values()
        .stream()
        .toList();
    // critical section - end
  }

  @Override
  public void deleteByID(String iban) {
    // validations
    if ((iban == null) || iban.isBlank()) {
      throw new IllegalArgumentException("IBAN not valid!");
    }

    // No need to delete account if IBAN doesn't exist
    if (!accountRepository.getEntities().containsKey(iban)) {
      return;
    }

    // critical section - begin
    accountRepository.getEntities().remove(iban);
    // critical section - end
  }

  @Override
  public boolean checkExistByID(String iban) {
    // validations
    if ((iban == null) || iban.isBlank()) {
      throw new IllegalArgumentException("IBAN not valid!");
    }

    // critical section - begin
    return accountRepository.getEntities()
        .containsKey(iban);
    // critical section - end
  }

}
