package analytics.repository;

import analytics.model.DepartmentSales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface DepartmentSalesRepository extends JpaRepository<DepartmentSales,Long> {

    @Query(value="select MAX(hr) from DepartmentSales",nativeQuery = true)
    int findMaxHour();

    @Query(value="select DISTINCT d.storeNumber from DepartmentSales d",nativeQuery = true)
    List<Integer> findStores();

    @Query(value="select DISTINCT d.department from DepartmentSales d WHERE d.storeNumber = ?1",nativeQuery = true)
    List<Integer> findDeptByStore(int storeNumber);

    @Query(value="select COUNT(DISTINCT d.department) from DepartmentSales d WHERE d.storeNumber = ?1",nativeQuery = true)
    int findCountOfDeptByStore(int storeNumber);


    @Query(value="select * from DepartmentSales d WHERE t.hr = ?1 AND t.storeNumber = ?2 ORDER BY d.totalSales DESC",nativeQuery = true)
    List<DepartmentSales> findRankedListOfDepartmentsByHourAndStore(int hr, int storeNumber);

}
