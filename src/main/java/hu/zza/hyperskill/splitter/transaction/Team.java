package hu.zza.hyperskill.splitter.transaction;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Stream;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

@Entity
class Team implements Comparable<Team> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id = 0;

  private String name = "";

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "TEAM_ACCOUNT",
      joinColumns = {@JoinColumn(name = "TEAM_ID")},
      inverseJoinColumns = {@JoinColumn(name = "ACCOUNT_ID")})
  private Set<Account> memberSet = new HashSet<>();

  public Team() {}

  Team(String name) {
    this(name, Set.of());
  }

  Team(String name, Set<Account> members) {
    this.name = name;
    this.memberSet.addAll(members);
  }

  Stream<Account> getMembersStream() {
    return memberSet.stream().sorted();
  }

  @Override
  public int compareTo(Team o) {
    return Comparator.comparing(Team::getName).compare(this, o);
  }

  String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name);
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

    Team other = (Team) obj;
    return Objects.equals(id, other.getId()) && Objects.equals(name, other.getName());
  }

  int getId() {
    return id;
  }

  @Override
  public String toString() {
    var stringJoiner = new StringJoiner(System.lineSeparator());
    memberSet.stream()
        .sorted(Comparator.comparing(Account::getName))
        .forEach(m -> stringJoiner.add(m.getName()));
    return stringJoiner.toString();
  }

  boolean addMembers(List<Account> accountList) {
    return memberSet.addAll(accountList);
  }

  boolean removeMembers(List<Account> accountList) {
    return memberSet.removeAll(accountList);
  }
}
