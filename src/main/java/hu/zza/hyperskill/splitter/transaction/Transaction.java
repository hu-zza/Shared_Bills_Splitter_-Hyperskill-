package hu.zza.hyperskill.splitter.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
class Transaction implements Comparable<Transaction> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id = 0;

  private LocalDate date = LocalDate.now();

  @ManyToOne
  @JoinColumn(name = "ACCOUNT_A")
  private Account accountA = new Account();

  @ManyToOne
  @JoinColumn(name = "ACCOUNT_B")
  private Account accountB = new Account();

  private BigDecimal amount = BigDecimal.ZERO;
  private boolean active = true;

  public Transaction() {}

  Transaction(LocalDate date, Account accountA, Account accountB, BigDecimal amount) {
    this.date = date;
    this.accountA = accountA;
    this.accountB = accountB;
    this.amount = amount;
  }

  Account getAccountA() {
    return accountA;
  }

  Account getAccountB() {
    return accountB;
  }

  LocalDate getDate() {
    return date;
  }

  BigDecimal getAmount() {
    return amount;
  }

  boolean isActive() {
    return active;
  }

  Transaction writeOff() {
    this.active = false;
    return this;
  }

  @Override
  public int compareTo(Transaction o) {
    return Comparator.comparingInt(Transaction::getId).compare(this, o);
  }

  int getId() {
    return id;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }

    Transaction other = (Transaction) obj;
    return Objects.equals(id, other.getId());
  }

  @Override
  public String toString() {
    return String.format(
        "On %s %s lent %.2f to %s.", date.toString(), accountA, amount.doubleValue(), accountB);
  }
}
