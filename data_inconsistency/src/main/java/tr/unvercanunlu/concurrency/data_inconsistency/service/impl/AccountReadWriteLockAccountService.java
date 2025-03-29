package tr.unvercanunlu.concurrency.data_inconsistency.service.impl;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import tr.unvercanunlu.concurrency.data_inconsistency.dao.IAccountDao;
import tr.unvercanunlu.concurrency.data_inconsistency.model.dto.AccountDto;
import tr.unvercanunlu.concurrency.data_inconsistency.model.entity.Account;
import tr.unvercanunlu.concurrency.data_inconsistency.service.IAccountService;
import tr.unvercanunlu.concurrency.data_inconsistency.util.ValidateUtil;
import tr.unvercanunlu.concurrency.data_inconsistency.util.ValueUtil;

// Solution: Account-Level Read-Write Locks
// 1) Improved Concurrency:
// - Uses a separate read-write lock for each account instead of a global lock.
// - Multiple threads can read the same account simultaneously (concurrent reads).
// - Only one thread can write to an account at a time (write operations are exclusive).
// 2) Data-Consistency:
// - Read operations are consistent since multiple threads can read without interference.
// - Write operations remain synchronized per account, avoiding race conditions.
// 3) Performance Improvement Over Global Lock:
// - Different accounts can be accessed concurrently without blocking each other.
// - Avoids the global lock bottleneck where all operations were serialized.
// 4) Lock Cleanup:
// - Locks are stored in a thread-safe ConcurrentHashMap to ensure per-account locking.
// - After account deletion, the corresponding lock is removed to free resources.
@RequiredArgsConstructor
public class AccountReadWriteLockAccountService implements IAccountService {

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

  // thread-safe storage for account-level read-write locks.
  // ensures that only one thread creates a lock per account at a time.
  private final ConcurrentHashMap<String, ReentrantReadWriteLock> accountLocks = new ConcurrentHashMap<>();

  @Override
  public AccountDto open() {
    Account entity = Account.builder()
        .iban(ValueUtil.generateIban())
        .balance(0)
        .createdAt(ZonedDateTime.now())
        .build();

    // get lock
    ReentrantReadWriteLock lock = getOrCreateLock(entity.getIban());

    // lock for write operation
    lock.writeLock().lock();

    try {
      // critical section - begin
      // operation
      entity = accountDao.save(entity);
      // critical section - end

    } finally {
      // unlock for write operation
      lock.writeLock().unlock();
    }

    // mapping
    return accountEntityDtoMapper.apply(entity);
  }

  @Override
  public void close(String iban) {
    // validation
    ValidateUtil.validateIban(iban);

    // get lock
    ReentrantReadWriteLock lock = getOrCreateLock(iban);

    // lock for write operation
    lock.writeLock().lock();

    try {
      // lock for operation with timeout
      // prevents deadlock: If one thread is deleting an account and removes its lock, another thread may still be waiting for the old lock. A timeout avoids indefinite waiting.
      if (!lock.writeLock().tryLock(1, TimeUnit.SECONDS)) {
        throw new RuntimeException("Failed to get write lock for deletion!");
      }

      try {
        // critical section - begin
        // operation
        accountDao.deleteByID(iban);
        // critical section - end
      } finally {
        // unlock for write operation
        lock.writeLock().unlock();

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
    ReentrantReadWriteLock lock = getOrCreateLock(iban);

    // lock for read operation
    lock.readLock().lock();

    Account entity = null;

    try {
      // critical section - begin
      // operation
      entity = accountDao.retrieveByID(iban)
          .orElseThrow(() -> new RuntimeException("Account not found!"));
      // critical section - end

    } finally {
      // unlock for read operation
      lock.readLock().unlock();
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
    ReentrantReadWriteLock lock = getOrCreateLock(iban);

    // lock for write and read operation
    lock.writeLock().lock();
    lock.readLock().lock();

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
      // unlock for write and read operation
      lock.writeLock().unlock();
      lock.readLock().unlock();
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
    ReentrantReadWriteLock lock = getOrCreateLock(iban);

    // lock for write and read operation
    lock.writeLock().lock();
    lock.readLock().lock();

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
      // unlock for write and read operation
      lock.writeLock().unlock();
      lock.readLock().unlock();
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
    ReentrantReadWriteLock lockFrom = getOrCreateLock(ibanFrom);
    ReentrantReadWriteLock lockTo = getOrCreateLock(ibanTo);

    // lock for write and read operation
    lockFrom.writeLock().lock();
    lockFrom.readLock().lock();
    lockTo.writeLock().lock();
    lockTo.readLock().lock();

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
      // unlock for write and read operation
      lockFrom.writeLock().unlock();
      lockFrom.readLock().unlock();
      lockTo.writeLock().unlock();
      lockTo.readLock().unlock();
    }

    // mapping
    return accountEntityDtoMapper.apply(entityFrom);
  }

  private ReentrantReadWriteLock getOrCreateLock(String iban) {
    // validation
    if ((iban == null) || iban.isBlank()) {
      throw new IllegalArgumentException("IBAN not valid!");
    }

    return accountLocks.computeIfAbsent(iban, id -> new ReentrantReadWriteLock());
  }

  // cleanup: remove the lock after account deletion to free resources.
  private void cleanupLock(String iban, ReentrantReadWriteLock lock) {
    // lock for write operation
    // acquire a write lock to ensure no active read or write operations exist before removing
    if (!lock.isWriteLockedByCurrentThread() && !lock.isWriteLocked()
        && lock.getReadLockCount() == 0 && lock.writeLock().tryLock()) {

      try {
        accountLocks.remove(iban, lock);
      } finally {
        // unlock for write operation
        lock.writeLock().unlock();
      }
    }
  }

}
