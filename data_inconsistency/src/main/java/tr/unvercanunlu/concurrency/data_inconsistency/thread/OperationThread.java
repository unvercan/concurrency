package tr.unvercanunlu.concurrency.data_inconsistency.thread;

import lombok.Getter;

@Getter
public class OperationThread extends Thread {

  private final String iban;
  private final double amountChange;

  public OperationThread(String name, Runnable task, String iban, double amountChange) {
    super(task, name);
    this.iban = iban;
    this.amountChange = amountChange;
  }

}
