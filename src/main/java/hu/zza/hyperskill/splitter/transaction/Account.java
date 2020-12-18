package hu.zza.hyperskill.splitter.transaction;


import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;


@Entity
class Account implements Comparable<Account>
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int    id   = 0;
    private String name = "";
    
    @OneToMany(mappedBy = "accountA", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Transaction> outgoing = new ArrayList<>();
    
    @OneToMany(mappedBy = "accountB", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Transaction> incoming = new ArrayList<>();
    
    @ManyToMany(mappedBy = "memberSet", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Team> membershipSet = new HashSet<>();
    
    
    Account()
    {}
    
    
    Account(String name)
    {
        this.name = name;
    }
    
    
    int getId()
    {
        return id;
    }
    
    
    String getName()
    {
        return name;
    }
    
    
    Stream<Transaction> getIncomingStream()
    {
        return incoming.stream().sorted();
    }
    
    
    Stream<Transaction> getOutgoingStream()
    {
        return outgoing.stream().sorted();
    }
    
    
    Stream<Team> getMembershipStream()
    {
        return membershipSet.stream().sorted();
    }
    
    
    @Override
    public int compareTo(Account o)
    {
        return Comparator.comparing(Account::getName).compare(this, o);
    }
    
    
    @Override
    public int hashCode()
    {
        return Objects.hash(id, name);
    }
    
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (this.getClass() != obj.getClass()) return false;
        
        Account other = (Account) obj;
        return Objects.equals(id, other.getId()) && Objects.equals(name, other.getName());
    }
    
    
    @Override
    public String toString()
    {
        
        return name;
    }
    
    
    boolean join(Team team)
    {
        return membershipSet.add(team);
    }
    
    
    boolean leave(Team team)
    {
        return membershipSet.remove(team);
    }
}
