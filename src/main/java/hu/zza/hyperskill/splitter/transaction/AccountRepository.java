package hu.zza.hyperskill.splitter.transaction;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AccountRepository extends CrudRepository<Account, Integer> {

  Optional<Account> findFirstByName(String name);
}
