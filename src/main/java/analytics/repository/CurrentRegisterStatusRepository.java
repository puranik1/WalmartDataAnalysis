package analytics.repository;

import analytics.model.CurrentRegisterStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CurrentRegisterStatusRepository extends JpaRepository<CurrentRegisterStatus,Long> {

    public void deleteByStoreAndRegister(int store, String register);
    public CurrentRegisterStatus findByStoreAndRegister(int store, String register);

}
