package hu.zza.hyperskill.splitter.transaction;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, Integer> {

  List<Transaction> findByDateBeforeAndActiveTrue(LocalDate localDate);
}
