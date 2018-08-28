package analytics.repository;

import analytics.model.PollerLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PollerLogRepository extends JpaRepository<PollerLog,Long>{


}
