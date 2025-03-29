package tr.unvercanunlu.concurrency.data_inconsistency.repository.impl;

import lombok.Getter;
import tr.unvercanunlu.concurrency.data_inconsistency.model.entity.Account;
import tr.unvercanunlu.concurrency.data_inconsistency.repository.IRepository;

@Getter
public class AccountRepository extends IRepository<String, Account> {

}
