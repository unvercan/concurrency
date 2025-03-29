package tr.unvercanunlu.concurrency.data_inconsistency.service.impl;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import tr.unvercanunlu.concurrency.data_inconsistency.dao.IAccountDao;
import tr.unvercanunlu.concurrency.data_inconsistency.model.dto.AccountDto;
import tr.unvercanunlu.concurrency.data_inconsistency.model.entity.Account;
import tr.unvercanunlu.concurrency.data_inconsistency.service.IAccountService;
import tr.unvercanunlu.concurrency.data_inconsistency.util.ValidateUtil;
import tr.unvercanunlu.concurrency.data_inconsistency.util.ValueUtil;

// Solution: Single Global Lock
// 1) Simple:
// - There is one global lock for all operation
// - No need to manage multiple locks
// - No need to cleanup
// 2) Data-consistency:
// - All operations are globally synchronized
// - No race condition
// 3) Performance problem:
// - Single global lock means only one thread can do operation at a time
// - When multiple threads attempt doing operation, they must wait for each other, even for different accounts
@RequiredArgsConstructor
public class SingleGlobalLockAccountService implements IAccountService {

  // dao
  private final IAccountDao accountDao;

  // entity to dto mapper
  private final Function<Account, AccountDto> accountEntityDtoMapper =
      entity -> Optional.ofNullable(entity)
          .map(e -> AccountDto.builder()
              .iban(e.getIban())
              .balance(e.getBalance())
              .build())
          .orElse(null);

  // global lock
  private final ReentrantLock lock = new ReentrantLock();

  @Override
  public AccountDto open() {
    Account entity = Account.builder()
        .iban(ValueUtil.generateIban())
        .balance(0)
        .createdAt(ZonedDateTime.now())
        .build();

    // lock for operation
    lock.lock();

    try {

      // critical section - begin
      // operation
      entity = accountDao.save(entity);
      // critical section - end

    } finally {
      // unlock for operation
      lock.unlock();
    }

    // mapping
    return accountEntityDtoMapper.apply(entity);
  }

  @Override
  public void close(String iban) {
    // validation
    ValidateUtil.validateIban(iban);

    // lock for operation
    lock.lock();

    try {

      // critical section - begin
      // operation
      accountDao.deleteByID(iban);
      // critical section - end

    } finally {
      // unlock for operation
      lock.unlock();
    }
  }

  @Override
  public AccountDto get(String iban) {
    // validation
    ValidateUtil.validateIban(iban);

    // lock for operation
    lock.lock();

    Account entity = null;

    try {

      // critical section - begin
      // operation
      entity = accountDao.retrieveByID(iban)
          .orElseThrow(() -> new RuntimeException("Account not found!"));
      // critical section - end

    } finally {
      // unlock for operation
      lock.unlock();
    }

    // mapping
    return accountEntityDtoMapper.apply(entity);
  }

  @Override
  public AccountDto withdraw(String iban, double amount) {
    // validation
    ValidateUtil.validateIban(iban);
    ValidateUtil.validateAmount(amount);

    // lock for operation
    lock.lock();

    Account entity = null;

    try {

      // critical section - begin
      // operation
      entity = accountDao.retrieveByID(iban)
          .orElseThrow(() -> new RuntimeException("Account not found!"));

      // validation
      if (entity.getBalance() < amount) {
        throw new RuntimeException("Amount not valid: Amount is not enough!");
      }

      // operation
      double newBalance = entity.getBalance() - amount;
      entity.setBalance(newBalance);
      entity = accountDao.save(entity);
      // critical section - end

    } finally {
      // unlock for operation
      lock.unlock();
    }

    // mapping
    return accountEntityDtoMapper.apply(entity);
  }

  @Override
  public AccountDto deposit(String iban, double amount) {
    // validation
    ValidateUtil.validateIban(iban);
    ValidateUtil.validateAmount(amount);

    // lock for operation
    lock.lock();

    Account entity = null;

    try {

      // critical section - begin
      // operation
      entity = accountDao.retrieveByID(iban)
          .orElseThrow(() -> new RuntimeException("Account not found!"));

      // operation
      double newBalance = entity.getBalance() + amount;
      entity.setBalance(newBalance);
      entity = accountDao.save(entity);
      // critical section - end

    } finally {
      // unlock for operation
      lock.unlock();
    }

    // mapping
    return accountEntityDtoMapper.apply(entity);
  }

  @Override
  public AccountDto transfer(String ibanFrom, String ibanTo, double amount) {
    // validation
    ValidateUtil.validateIban(ibanFrom);
    ValidateUtil.validateIban(ibanTo);
    ValidateUtil.validateAmount(amount);

    // lock for operation
    lock.lock();

    Account entityFrom = null;
    Account entityTo = null;

    try {

      // critical section - begin
      // operation
      entityFrom = accountDao.retrieveByID(ibanFrom)
          .orElseThrow(() -> new RuntimeException("Account not found!"));

      // operation
      entityTo = accountDao.retrieveByID(ibanTo)
          .orElseThrow(() -> new RuntimeException("Account not found!"));

      // validation
      if (entityFrom.getBalance() < amount) {
        throw new RuntimeException("Amount not valid: Amount is not enough!");
      }

      // operation
      double newBalanceFrom = entityFrom.getBalance() - amount;
      entityFrom.setBalance(newBalanceFrom);
      entityFrom = accountDao.save(entityFrom);

      // operation
      double newBalanceTo = entityTo.getBalance() + amount;
      entityTo.setBalance(newBalanceTo);
      accountDao.save(entityTo);
      // critical section - end

    } finally {
      // unlock for operation
      lock.unlock();
    }

    // mapping
    return accountEntityDtoMapper.apply(entityFrom);
  }

}
