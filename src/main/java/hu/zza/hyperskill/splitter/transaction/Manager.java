package hu.zza.hyperskill.splitter.transaction;

import hu.zza.clim.parameter.Parameter;
import hu.zza.clim.parameter.ParameterName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public abstract class Manager {

  private static final List<Account> TEMPORARY_GROUP = new ArrayList<>();
  private static final List<Account> EXCLUDING_LIST = new ArrayList<>();


  public static int manageTeam(Map<ParameterName, Parameter> parameterMap) {
    String name = ParameterParser.getName(parameterMap);
    List<Account> accountList = ParameterParser.getAccountList(parameterMap);

    try {
      switch (ParameterParser.getMethod(parameterMap)) {
        case SHOW:
          if (RepositoryManager.existGroupByName(name)) {
            System.out.println(RepositoryManager.teamOf(name));
          } else {
            System.out.println("Unknown group");
          }
          break;

        case CREATE:
          RepositoryManager.createTeam(name, accountList);
          break;

        case ADD:
          RepositoryManager.manageTeam(RepositoryManager.teamOf(name), accountList, true);
          break;

        case REMOVE:
          RepositoryManager.manageTeam(RepositoryManager.teamOf(name), accountList, false);
          break;
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    return 0;
  }


  public static int secretSanta(Map<ParameterName, Parameter> parameterMap) {
    Team team = ParameterParser.getTeam(parameterMap);

    List<Account> members = team.getMembersStream().collect(Collectors.toList());
    if (members.size() < 1) {
      throw new IllegalArgumentException(
          String.format("The Team '(%s)' is empty, Secret Santa Draw fails.",
              team.getName()
          ));
    }

    Integer[] shuffledIndices = createShuffledIntList(members.size()).toArray(new Integer[0]);
    List<String> result = new ArrayList<>();
    for (int i = 0; i < shuffledIndices.length - 1; i++) {
      result.add(String.format("%s gift to %s",
          members.get(shuffledIndices[i]),
          members.get(shuffledIndices[i + 1])
      ));
    }

    result.add(String.format("%s gift to %s",
        members.get(shuffledIndices[shuffledIndices.length - 1]),
        members.get(shuffledIndices[0])
    ));

    result.stream().sorted().forEach(System.out::println);

    return 0;
  }


  private static List<Integer> createShuffledIntList(int size) {
    var intList = IntStream.range(0, size)
        .collect(ArrayList<Integer>::new, ArrayList::add, ArrayList::addAll);
    Collections.shuffle(intList);
    return intList;
  }


  static List<Account> createTemporaryTeam(List<String> stringList) {
    TEMPORARY_GROUP.clear();
    EXCLUDING_LIST.clear();

    for (var item : stringList) {

      boolean toExclude = item.startsWith("-");
      String name = toExclude || item.startsWith("+") ? item.substring(1) : item;

      if (name.equals(name.toUpperCase())) {
        if (RepositoryManager.existGroupByName(name)) {
          RepositoryManager.teamOf(name).getMembersStream()
              .forEach(m -> addToTemporaryTeam(m, toExclude));
        }
      } else {
        addToTemporaryTeam(RepositoryManager.accountOf(name), toExclude);
      }
    }

    // Delete duplicates and sort by names before excluding.
    var result = TEMPORARY_GROUP.stream().distinct().sorted().collect(Collectors.toList());

    // Delete last occurrences of EXCLUDING_LIST elements in TEMPORARY_GROUP.
    EXCLUDING_LIST.stream().filter(result::contains).forEach(result::remove);

    return result;
  }


  private static void addToTemporaryTeam(Account account, boolean toExclude) {
    if (toExclude) {
      EXCLUDING_LIST.add(account);
    } else {
      TEMPORARY_GROUP.add(account);
    }
  }
}
