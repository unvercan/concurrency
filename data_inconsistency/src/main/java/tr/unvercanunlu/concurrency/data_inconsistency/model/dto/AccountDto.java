package tr.unvercanunlu.concurrency.data_inconsistency.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AccountDto {

  private String iban;
  private double balance;

}
