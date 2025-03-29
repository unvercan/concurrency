package tr.unvercanunlu.concurrency.data_inconsistency.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tr.unvercanunlu.concurrency.data_inconsistency.config.Config;
import tr.unvercanunlu.concurrency.data_inconsistency.model.entity.Account;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidateUtil {

  public static void validateAccount(Account account) {
    if (account == null) {
      throw new RuntimeException("Account not valid: Account is null!");
    }
  }

  public static void validateAmount(double amount) {
    if (amount < 0) {
      throw new RuntimeException("Amount not valid: amount is negative!");
    }

    if (amount == 0) {
      throw new RuntimeException("Amount not valid: amount is zero!");
    }
  }

  public static void validateIban(String iban) {
    if (iban == null) {
      throw new RuntimeException("IBAN not valid: IBAN is null!");
    }

    if (iban.trim().isBlank()) {
      throw new RuntimeException("IBAN not valid: IBAN is empty!");
    }

    if (iban.trim().length() != Config.IBAN_TOTAL_LENGTH) {
      throw new RuntimeException("IBAN not valid: expected length and actual length of iban are not matched!");
    }

    if (!iban.trim().substring(0, 2).toUpperCase().chars().mapToObj(i -> (char) i).allMatch(Character::isUpperCase)) {
      throw new RuntimeException("IBAN not valid: not all letters of country part are letter!");
    }

    if (!iban.trim().substring(0, 2).equalsIgnoreCase(Config.COUNTRY_CODE)) {
      throw new RuntimeException("IBAN not valid: country is different!");
    }

    if (!iban.trim().substring(2).chars().mapToObj(i -> (char) i).allMatch(Character::isDigit)) {
      throw new RuntimeException("IBAN not valid: not all digits of numeric part are numeric!");
    }
  }

}
