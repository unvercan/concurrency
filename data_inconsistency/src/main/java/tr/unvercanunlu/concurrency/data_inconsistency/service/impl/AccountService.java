package tr.unvercanunlu.concurrency.data_inconsistency.service.impl;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import tr.unvercanunlu.concurrency.data_inconsistency.dao.IAccountDao;
import tr.unvercanunlu.concurrency.data_inconsistency.model.dto.AccountDto;
import tr.unvercanunlu.concurrency.data_inconsistency.model.entity.Account;
import tr.unvercanunlu.concurrency.data_inconsistency.service.IAccountService;
import tr.unvercanunlu.concurrency.data_inconsistency.util.ValidateUtil;
import tr.unvercanunlu.concurrency.data_inconsistency.util.ValueUtil;

@RequiredArgsConstructor
public class AccountService implements IAccountService {

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

  @Override
  public AccountDto open() {
    Account entity = Account.builder()
        .iban(ValueUtil.generateIban())
        .balance(0)
        .createdAt(ZonedDateTime.now())
        .build();

    // critical section - begin
    // operation
    entity = accountDao.save(entity);
    // critical section - end

    // mapping
    return accountEntityDtoMapper.apply(entity);
  }

  @Override
  public void close(String iban) {
    // validation
    ValidateUtil.validateIban(iban);

    // critical section - begin
    // operation
    accountDao.deleteByID(iban);
    // critical section - end
  }

  @Override
  public AccountDto get(String iban) {
    // validation
    ValidateUtil.validateIban(iban);

    // critical section - begin
    // operation
    Account entity = accountDao.retrieveByID(iban)
        .orElseThrow(() -> new RuntimeException("Account not found!"));
    // critical section - end

    // mapping
    return accountEntityDtoMapper.apply(entity);
  }

  @Override
  public AccountDto withdraw(String iban, double amount) {
    // validation
    ValidateUtil.validateIban(iban);
    ValidateUtil.validateAmount(amount);

    // critical section - begin
    // operation
    Account entity = accountDao.retrieveByID(iban)
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

    // mapping
    return accountEntityDtoMapper.apply(entity);
  }

  @Override
  public AccountDto deposit(String iban, double amount) {
    // validation
    ValidateUtil.validateIban(iban);
    ValidateUtil.validateAmount(amount);

    // critical section - begin
    // operation
    Account entity = accountDao.retrieveByID(iban)
        .orElseThrow(() -> new RuntimeException("Account not found!"));

    // operation
    double newBalance = entity.getBalance() + amount;
    entity.setBalance(newBalance);
    entity = accountDao.save(entity);
    // critical section - end

    // mapping
    return accountEntityDtoMapper.apply(entity);
  }

  @Override
  public AccountDto transfer(String ibanFrom, String ibanTo, double amount) {
    // validation
    ValidateUtil.validateIban(ibanFrom);
    ValidateUtil.validateIban(ibanTo);
    ValidateUtil.validateAmount(amount);

    // critical section - begin
    // operation
    Account entityFrom = accountDao.retrieveByID(ibanFrom)
        .orElseThrow(() -> new RuntimeException("Account not found!"));

    // operation
    Account entityTo = accountDao.retrieveByID(ibanTo)
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

    // mapping
    return accountEntityDtoMapper.apply(entityFrom);
  }

}
