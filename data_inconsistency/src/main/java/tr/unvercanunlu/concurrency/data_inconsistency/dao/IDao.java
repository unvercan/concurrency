package tr.unvercanunlu.concurrency.data_inconsistency.dao;

import java.util.List;
import java.util.Optional;

public interface IDao<T, E> {

  E save(E entity);

  Optional<E> retrieveByID(T iban);

  List<E> retrieveAll();

  void deleteByID(T iban);

  boolean checkExistByID(T iban);

}
