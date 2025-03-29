package tr.unvercanunlu.concurrency.data_inconsistency.service.impl;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import tr.unvercanunlu.concurrency.data_inconsistency.dao.IAccountDao;
import tr.unvercanunlu.concurrency.data_inconsistency.model.dto.AccountDto;
import tr.unvercanunlu.concurrency.data_inconsistency.model.entity.Account;
import tr.unvercanunlu.concurrency.data_inconsistency.service.IAccountService;
import tr.unvercanunlu.concurrency.data_inconsistency.util.ValidateUtil;
import tr.unvercanunlu.concurrency.data_inconsistency.util.ValueUtil;

// Solution: Account-Level Explicit Locks
// 1) Fine-Grained Locking:
// - Uses a separate ReentrantLock per account instead of a global lock.
// - Ensures only one thread modifies an account at a time while allowing parallel operations on different accounts.
// 2) Concurrency & Performance:
// - Allows concurrent read/write operations on different accounts without blocking unrelated transactions.
// - Reduces contention compared to a global lock approach, leading to improved system throughput.
// 3) Lock Cleanup:
// - Uses a thread-safe ConcurrentHashMap to manage per-account locks.
// - Automatically removes locks after an account is deleted to free up resources.
// 4) Deadlock Prevention:
// - Implements tryLock with a timeout in close() to avoid indefinite waiting on a lock.
// - Acquires locks in a consistent order (transfer(): locks ibanFrom first, then ibanTo)
@RequiredArgsConstructor
public class AccountLockAccountService implements IAccountService {

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

  // thread-safe storage for account-level locks
  // ensures that only one thread creates a lock per account at a time
  private final ConcurrentHashMap<String, ReentrantLock> accountLocks = new ConcurrentHashMap<>();

  @Override
  public AccountDto open() {
    Account entity = Account.builder()
        .iban(ValueUtil.generateIban())
        .balance(0)
        .createdAt(ZonedDateTime.now())
        .build();

    // get lock
    ReentrantLock lock = getOrCreateLock(entity.getIban());

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

    // get lock
    ReentrantLock lock = getOrCreateLock(iban);

    try {
      // lock for operation with timeout
      // prevents deadlock: If one thread is deleting an account and removes its lock, another thread may still be waiting for the old lock. A timeout avoids indefinite waiting.
      if (!lock.tryLock(1, TimeUnit.SECONDS)) {
        throw new RuntimeException("Failed to get write lock for deletion!");
      }

      try {

        // critical section - begin
        // operation
        accountDao.deleteByID(iban);
        // critical section - end

      } finally {
        // unlock for operation
        lock.unlock();

        // remove lock for deleted account
        cleanupLock(iban, lock);
      }

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Thread interrupted while deleting account!", e);
    }
  }

  @Override
  public AccountDto get(String iban) {
    // validation
    ValidateUtil.validateIban(iban);

    // get lock
    ReentrantLock lock = getOrCreateLock(iban);

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

    // get lock
    ReentrantLock lock = getOrCreateLock(iban);

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

    // get lock
    ReentrantLock lock = getOrCreateLock(iban);

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

    // get lock
    ReentrantLock lockFrom = getOrCreateLock(ibanFrom);
    ReentrantLock lockTo = getOrCreateLock(ibanTo);

    // lock for operation
    lockFrom.lock();
    lockTo.lock();

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
      lockFrom.unlock();
      lockTo.unlock();
    }

    // mapping
    return accountEntityDtoMapper.apply(entityFrom);
  }

  private ReentrantLock getOrCreateLock(String iban) {
    // validation
    if ((iban == null) || iban.isBlank()) {
      throw new IllegalArgumentException("IBAN not valid!");
    }

    return accountLocks.computeIfAbsent(iban, id -> new ReentrantLock());
  }

  // cleanup: remove the lock after account deletion to free resources.
  private void cleanupLock(String iban, ReentrantLock lock) {
    // lock for operation
    // acquire lock to ensure no active operations exist before removing
    if (lock.getHoldCount() == 0) {

      try {
        accountLocks.remove(iban, lock);
      } finally {
        // unlock for operation
        lock.unlock();
      }
    }
  }

}
