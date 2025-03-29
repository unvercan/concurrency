package tr.unvercanunlu.concurrency.data_inconsistency.model.entity;

import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Account {

  private String iban;
  private double balance;
  private ZonedDateTime createdAt;
  private ZonedDateTime updatedAt;

}
