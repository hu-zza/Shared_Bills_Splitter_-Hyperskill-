package hu.zza.hyperskill.splitter.transaction;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends CrudRepository<Team, Integer> {

  Optional<Team> findFirstByName(String name);
}
