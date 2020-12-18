package hu.zza.hyperskill.splitter.transaction;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface AccountRepository extends CrudRepository<Account, Integer>
{
    Optional<Account> findFirstByName(String name);
}
