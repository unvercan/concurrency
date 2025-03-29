package tr.unvercanunlu.concurrency.data_inconsistency.repository;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public abstract class IRepository<T, E> {

  private final Map<T, E> entities = new HashMap<>();

}
