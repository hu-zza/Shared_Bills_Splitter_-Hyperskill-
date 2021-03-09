package hu.zza.hyperskill.splitter.transaction;

import static hu.zza.hyperskill.splitter.config.MenuParameter.FROM;
import static hu.zza.hyperskill.splitter.config.MenuParameter.LIST;
import static hu.zza.hyperskill.splitter.config.MenuParameter.NAME;
import static hu.zza.hyperskill.splitter.config.MenuParameter.TO;

import hu.zza.clim.parameter.Parameter;
import hu.zza.clim.parameter.ParameterName;
import hu.zza.hyperskill.splitter.config.MenuConstant;
import hu.zza.hyperskill.splitter.config.MenuLeaf;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public abstract class Ledger {

  public static final BigDecimal HUNDRED = new BigDecimal("100");
  private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_DOWN;
  private static final int PRECISION = 2;
  private static final BigDecimal CENT = new BigDecimal("0.01");


  public static int makeMicroTransaction(Map<ParameterName, Parameter> parameterMap) {

    RepositoryManager.makeTransaction(
        ParameterParser.getDate(parameterMap),
        ParameterParser.getAccount(parameterMap, FROM),
        ParameterParser.getAccount(parameterMap, TO),
        ParameterParser.getAmount(parameterMap),
        ParameterParser.getCommand(parameterMap) == MenuLeaf.BORROW
    );

    return 0;
  }


  public static int makeMacroTransaction(Map<ParameterName, Parameter> parameterMap) {
    LocalDate transactionDate = ParameterParser.getDate(parameterMap);
    Account buyer = ParameterParser.getAccount(parameterMap, NAME);
    BigDecimal cost = ParameterParser.getAmount(parameterMap);
    boolean reversed = ParameterParser.getCommand(parameterMap) == MenuLeaf.CASHBACK;

    List<Account> accountList = ParameterParser.getAccountList(parameterMap);

    int listSize = accountList.size();
    if (listSize < 1) {
      throw new IllegalArgumentException(String.format(
          "The account list '(%s)' is effectively empty, transaction fails.",
          parameterMap.get(LIST).getValue()
      ));
    }

    BigDecimal[] costs = divideCostBetween(cost, listSize);
    int index = 0;

    for (var a : accountList) {
      if (!Objects.deepEquals(a, buyer)) {
        RepositoryManager.makeTransaction(transactionDate, buyer, a, costs[index], reversed);
      }
      index++;
    }

    return 0;
  }


  public static int getPerfectBalance(Map<ParameterName, Parameter> parameterMap) {
    System.out.println("Chuck owes Bob 30.00");
    List<Account> accountList = ParameterParser.getAccountList(parameterMap);
    accountList.forEach(a ->
    {
      a.getIncomingStream().forEach(System.out::println);
      a.getOutgoingStream().forEach(System.out::println);
    });
    return 0;
  }


  public static int getBalance(Map<ParameterName, Parameter> parameterMap) {
    MenuConstant method = ParameterParser.getMethod(parameterMap);
    LocalDate date = ParameterParser.getDate(parameterMap);
    List<Account> accountList = ParameterParser.getAccountList(parameterMap);

    switch (method) {
      case OPEN:
        printBalance(date.withDayOfMonth(1).minusDays(1), accountList);
        break;
      case CLOSE:
      default:
        printBalance(date, accountList);
        break;
    }

    return 0;
  }


  public static int writeOff(Map<ParameterName, Parameter> parameterMap) {
    LocalDate date = ParameterParser.getDate(parameterMap);
    RepositoryManager.writeOffTransactionsUntil(date.plusDays(1));
    return 0;
  }


  private static void printBalance(LocalDate date, List<Account> accountList) {
    var transactions = RepositoryManager.getTransactionsUntil(date);

    int accountsCount = RepositoryManager.getAccountsCount() + 1;
    int[][] balanceMatrix = new int[accountsCount][accountsCount];

    transactions.forEach(t ->
    {
      int idA = t.getAccountA().getId();
      int idB = t.getAccountB().getId();
      balanceMatrix[idA][idB] += t.getAmount().multiply(HUNDRED).doubleValue();
    });

    double balance;
    String resultPattern = "%s owes %s %.2f";
    Locale locale = Locale.US;
    List<String> resultList = new ArrayList<>();

    for (int i = 1; i < accountsCount; i++) {
      for (int j = 0; j < i; j++) {
        balance = (balanceMatrix[i][j] - balanceMatrix[j][i]) / 100.0;
        if (0 < balance) {
          resultList.add(String.format(locale,
              resultPattern,
              RepositoryManager.getAccountById(j).get(),
              RepositoryManager.getAccountById(i).get(),
              balance
          ));
        } else if (balance < 0) {
          resultList.add(String.format(locale,
              resultPattern,
              RepositoryManager.getAccountById(i).get(),
              RepositoryManager.getAccountById(j).get(),
              -balance
          ));
        }
      }
    }

    if (!accountList.isEmpty()) {
      Set<String> nameSet = accountList.stream().map(Account::getName).collect(Collectors.toSet());
      Predicate<String> relevant = t -> nameSet.contains(t.substring(0, t.indexOf(' ')));
      List<String> filtered = resultList.stream().filter(relevant).collect(Collectors.toList());
      resultList = filtered;
    }

    if (resultList.isEmpty()) {
      System.out.println("No repayments need");
    } else {
      resultList.stream().sorted().forEach(System.out::println);
    }

  }


  private static BigDecimal[] divideCostBetween(BigDecimal amount, int participants) {
    var result = new BigDecimal[participants];

    var subResult = divideAndRemainder(amount, new BigDecimal(participants));

    for (var i = 0; i < participants; i++) {
      result[i] = BigDecimal.ZERO.add(subResult[0]);
    }

    int remainingCents = (int) (subResult[1].doubleValue() * 100);

    if (0 != remainingCents) {
      var reverse = remainingCents < 0;
      remainingCents = Math.abs(remainingCents);

      for (int i = 0; i < remainingCents; i++) {
        if (reverse) {
          result[result.length - 1 - i] = result[result.length - 1 - i].subtract(CENT);
        } else {
          result[i] = result[i].add(CENT);
        }
      }
    }

    return result;
  }


  private static BigDecimal[] divideAndRemainder(BigDecimal dividend, BigDecimal divisor) {
    return new BigDecimal[]{
        divideWithCents(dividend, divisor), getRemainderCents(dividend, divisor)
    };
  }


  private static BigDecimal divideWithCents(BigDecimal dividend, BigDecimal divisor) {
    return dividend.divide(divisor, PRECISION, ROUNDING_MODE);
  }


  /**
   * Returns a BigDecimal whose value is (dividend % divisor).
   * <p>
   * The remainder is given by same method as <code>remainder</code> from <code>BigDecimal</code>
   * class:
   * <code>dividend.subtract(divideBetween(dividend, divisor).multiply(divisor))</code>
   * Note that this is not the modulo operation (the result can be negative).
   *
   * @param dividend value to be divided by <code>divisor</code>.
   * @param divisor  value by which <code>dividend</code> is to be divided.
   * @return dividend % divisor.
   */
  private static BigDecimal getRemainderCents(BigDecimal dividend, BigDecimal divisor) {
    return dividend.subtract(divideWithCents(dividend, divisor).multiply(divisor));
  }
}
