package tr.unvercanunlu.concurrency.data_inconsistency.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Config {

  public static final int IBAN_COUNTRY_CODE_LENGTH = 2;
  public static final int IBAN_DIGIT_LENGTH = 26;
  public static final int IBAN_TOTAL_LENGTH = IBAN_COUNTRY_CODE_LENGTH + IBAN_DIGIT_LENGTH;

  public static final String COUNTRY_CODE = "PL";

  public static final long ACCOUNT_COUNT_MIN = 3;
  public static final long ACCOUNT_COUNT_MAX = 10;

  public static final long DEPOSIT_OPERATION_COUNT_MIN = 1_000;
  public static final long DEPOSIT_OPERATION_COUNT_MAX = 10_000;

  public static final long CHECK_BALANCE_OPERATION_COUNT_MIN = 1_000;
  public static final long CHECK_BALANCE_OPERATION_COUNT_MAX = 10_000;

  public static final long WITHDRAW_OPERATION_COUNT_MIN = 1_000;
  public static final long WITHDRAW_OPERATION_COUNT_MAX = 10_000;

  public static final long DEPOSIT_AMOUNT_MIN = 100;
  public static final long DEPOSIT_AMOUNT_MAX = 1_000;

  public static final long WITHDRAW_AMOUNT_MIN = 1;
  public static final long WITHDRAW_AMOUNT_MAX = 10;

}
