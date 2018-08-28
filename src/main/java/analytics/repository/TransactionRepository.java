package analytics.repository;

import analytics.model.StoreRegister;
import analytics.model.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TransactionRepository extends JpaRepository<Transactions,Long>{


    @Query(value = "select t.storeNumber, t.register, MAX(t.orderTime) FROM Transactions t WHERE t.transactionId > ?1 AND t.transactionId <= ?2 GROUP BY t.storeNumber, t.register",nativeQuery = true)
    List<Object[]> findStoreNumberAndRegisterWhereTransactionIdGreaterThan(long lastTransactionIdProcessed, long maxTransactionId);

    @Query(value="select MAX(transactionId) from Transactions",nativeQuery = true)
    Long findMaxTransactionId();

    @Query(value="select t.storeNumber, t.department, SUM(t.amount) from Transactions WHERE (t.orderTime + 360) >= ?1 GROUP BY t.storeNumber, t.department ORDER BY t.storeNumber, SUM(t.amount) DESC",nativeQuery = true)
    List<Object[]> findAllDepartSalesInLastHour(long currentTime);

}
