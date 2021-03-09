package hu.zza.hyperskill.splitter.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class RepositoryManager {

  private static TeamRepository teamRepository;
  private static AccountRepository accountRepository;
  private static TransactionRepository transactionRepository;


  @Autowired
  RepositoryManager(TeamRepository teamRepo, AccountRepository accountRepo,
      TransactionRepository transactionRepo
  ) {
    if (teamRepository == null && accountRepository == null && transactionRepository == null) {
      teamRepository = teamRepo;
      accountRepository = accountRepo;
      transactionRepository = transactionRepo;
    }
  }


  static int getAccountsCount() {
    return (int) accountRepository.count();
  }


  static boolean existGroupByName(String name) {
    return getTeamByName(name).isPresent();
  }


  static Optional<Account> getAccountById(int id) {
    return accountRepository.findById(id);
  }


  static Optional<Account> getAccountByName(String name) {
    return accountRepository.findFirstByName(name);
  }


  static Account accountOf(String name) {
    return getAccountByName(name).orElseGet(() -> createAccount(name));
  }


  static Account createAccount(String name) {
    return accountRepository.save(new Account(name));
  }


  static Optional<Team> getTeamByName(String name) {
    return teamRepository.findFirstByName(name);
  }


  static Team teamOf(String name) {
    return getTeamByName(name).orElseGet(() -> createTeam(name, List.of()));
  }


  static Team createTeam(String name, List<Account> accounts) {
    if (existGroupByName(name)) {
      //Because of testcases...
      //throw new IllegalArgumentException(String.format("Team %s is exist. You can not overwrite.", name));

      // tmp patch for tests, TODO: restore
      Team team = teamOf(name);

      team = manageTeam(team, team.getMembersStream().collect(Collectors.toList()), false);
      return manageTeam(team, accounts, true);
    }

    return manageTeam(teamRepository.save(new Team(name)), accounts, true);
  }


  static Team manageTeam(Team team, List<Account> accountList, boolean join) {
    Team finalTeam = teamOf(team.getName());
    accountList = accountList
        .stream()
        .map(Account::getName)
        .map(RepositoryManager::accountOf)
        .collect(Collectors.toList());

    if (join) {
      team.addMembers(accountList);
      accountList.forEach(a -> a.join(finalTeam));
    } else {
      team.removeMembers(accountList);
      accountList.forEach(a -> a.leave(finalTeam));
    }

    accountRepository.saveAll(accountList);
    return teamRepository.save(team);
  }


  static void makeTransaction(LocalDate date, Account accountA, Account accountB, BigDecimal amount,
      boolean reversed) {
    transactionRepository.save(createTransaction(date,
        reversed ? accountB : accountA,
        reversed ? accountA : accountB,
        amount
    ));
  }


  static Transaction createTransaction(LocalDate date, Account from, Account to,
      BigDecimal amount) {
    return transactionRepository.save(new Transaction(date, from, to, amount));
  }


  static List<Transaction> getTransactionsUntil(LocalDate localDate) {
    return transactionRepository.findByDateBeforeAndActiveTrue(localDate.plusDays(1));
  }


  static void writeOffTransactionsUntil(LocalDate localDate) {
    // Maybe a "counter-borrow" would be more sufficient:
    // The creditor takes over the loan.
    // Otherwise repayments after write-off seems like loans from debtor.
    transactionRepository
        .findByDateBeforeAndActiveTrue(localDate.plusDays(1))
        .stream()
        .map(Transaction::writeOff)
        .forEach(transactionRepository::save);
  }

}
