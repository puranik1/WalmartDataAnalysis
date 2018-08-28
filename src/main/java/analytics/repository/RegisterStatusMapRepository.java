package analytics.repository;

import analytics.model.RegisterStatusMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RegisterStatusMapRepository extends JpaRepository<RegisterStatusMap,Long> {

    RegisterStatusMap findByStoreNumberAndRegister(int storeNumber, int register);
}
