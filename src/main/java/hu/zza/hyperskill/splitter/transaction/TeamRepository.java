package hu.zza.hyperskill.splitter.transaction;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface TeamRepository extends CrudRepository<Team, Integer>
{
    Optional<Team> findFirstByName(String name);
}
