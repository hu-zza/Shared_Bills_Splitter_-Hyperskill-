package hu.zza.hyperskill.splitter.transaction;

import hu.zza.hyperskill.splitter.config.MenuConstant;
import hu.zza.hyperskill.splitter.config.MenuLeaf;
import hu.zza.hyperskill.splitter.config.MenuParameter;
import hu.zza.hyperskill.splitter.menu.parameter.Parameter;
import hu.zza.hyperskill.splitter.menu.parameter.ParameterName;

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


public abstract class Ledger
{
    public static final  BigDecimal   HUNDRED       = new BigDecimal("100");
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_DOWN;
    private static final int          PRECISION     = 2;
    private static final BigDecimal   CENT          = new BigDecimal("0.01");
    
    
    public static int makeMicroTransaction(Map<ParameterName, Parameter> parameterMap)
    {
        var        command         = (MenuLeaf) parameterMap.get(MenuParameter.COMMAND).getValue();
        var        reversed        = command == MenuLeaf.BORROW;
        var        transactionDate = (LocalDate) parameterMap.get(MenuParameter.DATE).getOrDefault();
        Account    accountA        = RepositoryManager.accountOf((String) parameterMap
                                                                                  .get(MenuParameter.FROM)
                                                                                  .getValue());
        Account    accountB        = RepositoryManager.accountOf((String) parameterMap
                                                                                  .get(MenuParameter.TO)
                                                                                  .getValue());
        BigDecimal amount          = new BigDecimal((String) parameterMap.get(MenuParameter.AMOUNT).getValue());
        
        RepositoryManager.makeTransaction(transactionDate, accountA, accountB, amount, reversed);
        
        return 0;
    }
    
    
    public static int makeMacroTransaction(Map<ParameterName, Parameter> parameterMap)
    {
        var command         = (MenuLeaf) parameterMap.get(MenuParameter.COMMAND).getValue();
        var reversed        = command == MenuLeaf.CASHBACK;
        var transactionDate = (LocalDate) parameterMap.get(MenuParameter.DATE).getOrDefault();
        Account buyer = RepositoryManager.accountOf((String) parameterMap.get(MenuParameter.NAME).getValue());
        BigDecimal cost = new BigDecimal((String) parameterMap.get(MenuParameter.AMOUNT).getValue());
        
        List<String>  rawAccountList = Manager.getStringList(parameterMap);
        List<Account> accountList    = Manager.createTemporaryTeam(rawAccountList);
        
        int listSize = accountList.size();
        if (listSize < 1)
        {
            throw new IllegalArgumentException(String.format(
                    "The account list '(%s)' is effectively empty, transaction fails.",
                    String.join(", ", rawAccountList)
            ));
        }
        
        BigDecimal[] costs = divideCostBetween(cost, listSize);
        int          index = 0;
        
        for (var a : accountList)
        {
            if (!Objects.deepEquals(a, buyer))
            {
                RepositoryManager.makeTransaction(transactionDate, buyer, a, costs[index], reversed);
            }
            index++;
        }
        
        return 0;
    }
    
    public static int getPerfectBalance(Map<ParameterName, Parameter> parameterMap)
    {
        System.out.println("Chuck owes Bob 30.00");
        List<Account> accountList = Manager.createTemporaryTeam(Manager.getStringList(parameterMap));
        accountList.forEach(a -> {a.getIncomingStream().forEach(System.out::println); a.getOutgoingStream().forEach(System.out::println); });
        return 0;
    }
    
    public static int getBalance(Map<ParameterName, Parameter> parameterMap)
    {
        MenuConstant method = (MenuConstant) parameterMap.get(MenuParameter.METHOD).getOrDefault();
        LocalDate    date   = (LocalDate) parameterMap.get(MenuParameter.DATE).getOrDefault();
        List<Account> accountList = Manager.createTemporaryTeam(Manager.getStringList(parameterMap));
        
        switch (method)
        {
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
    
    
    public static int writeOff(Map<ParameterName, Parameter> parameterMap)
    {
        var date = (LocalDate) parameterMap.get(MenuParameter.DATE).getOrDefault();
        RepositoryManager.writeOffTransactionsUntil(date.plusDays(1));
        return 0;
    }
    
    
    private static void printBalance(LocalDate date, List<Account> accountList)
    {
        var transactions = RepositoryManager.getTransactionsUntil(date);
        
        int     accountsCount = RepositoryManager.getAccountsCount() + 1;
        int[][] balanceMatrix = new int[accountsCount][accountsCount];
        
        transactions.forEach(t ->
                             {
                                 int idA = t.getAccountA().getId();
                                 int idB = t.getAccountB().getId();
                                 balanceMatrix[idA][idB] += t.getAmount().multiply(HUNDRED).doubleValue();
                             });
        
        double       balance;
        String       resultPattern = "%s owes %s %.2f";
        Locale       locale        = Locale.US;
        List<String> resultList    = new ArrayList<>();
        
        for (int i = 1; i < accountsCount; i++)
        {
            for (int j = 0; j < i; j++)
            {
                balance = (balanceMatrix[i][j] - balanceMatrix[j][i]) / 100.0;
                if (0 < balance)
                {
                    resultList.add(String.format(
                            locale,
                            resultPattern,
                            RepositoryManager.getAccountById(j).get(),
                            RepositoryManager.getAccountById(i).get(),
                            balance
                    ));
                }
                else if (balance < 0)
                {
                    resultList.add(String.format(
                            locale,
                            resultPattern,
                            RepositoryManager.getAccountById(i).get(),
                            RepositoryManager.getAccountById(j).get(),
                            -balance
                    ));
                }
            }
        }
        
        
        if (!accountList.isEmpty())
        {
            Set<String>       nameSet  = accountList.stream().map(Account::getName).collect(Collectors.toSet());
            Predicate<String> relevant = t -> nameSet.contains(t.substring(0, t.indexOf(' ')));
            List<String>      filtered = resultList.stream().filter(relevant).collect(Collectors.toList());
            resultList = filtered;
        }
        
        if (resultList.isEmpty())
        {
            System.out.println("No repayments need");
        }
        else
        {
            resultList.stream().sorted().forEach(System.out::println);
        }
        
    }
    
    
    private static BigDecimal[] divideCostBetween(BigDecimal amount, int participants)
    {
        var result = new BigDecimal[participants];
        
        var subResult = divideAndRemainder(amount, new BigDecimal(participants));
        
        for (var i = 0; i < participants; i++)
        {
            result[i] = BigDecimal.ZERO.add(subResult[0]);
        }
        
        int remainingCents = (int) (subResult[1].doubleValue() * 100);
        
        if (0 != remainingCents)
        {
            var reverse = remainingCents < 0;
            remainingCents = Math.abs(remainingCents);
            
            for (int i = 0; i < remainingCents; i++)
            {
                if (reverse)
                {
                    result[result.length - 1 - i] = result[result.length - 1 - i].subtract(CENT);
                }
                else
                {
                    result[i] = result[i].add(CENT);
                }
            }
        }
        
        return result;
    }
    
    
    private static BigDecimal[] divideAndRemainder(BigDecimal dividend, BigDecimal divisor)
    {
        return new BigDecimal[] {
                divideWithCents(dividend, divisor), getRemainderCents(dividend, divisor)
        };
    }
    
    
    private static BigDecimal divideWithCents(BigDecimal dividend, BigDecimal divisor)
    {
        return dividend.divide(divisor, PRECISION, ROUNDING_MODE);
    }
    
    
    /**
     * Returns a BigDecimal whose value is (dividend % divisor).
     * <p>
     * The remainder is given by same method as <code>remainder</code> from <code>BigDecimal</code> class:
     * <code>dividend.subtract(divideBetween(dividend, divisor).multiply(divisor))</code>
     * Note that this is not the modulo operation (the result can be negative).
     *
     * @param dividend value to be divided by <code>divisor</code>.
     * @param divisor  value by which <code>dividend</code> is to be divided.
     *
     * @return dividend % divisor.
     */
    private static BigDecimal getRemainderCents(BigDecimal dividend, BigDecimal divisor)
    {
        return dividend.subtract(divideWithCents(dividend, divisor).multiply(divisor));
    }
}
