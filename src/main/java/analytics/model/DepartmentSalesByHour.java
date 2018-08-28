package analytics.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;


@Entity
public class DepartmentSalesByHour {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private int storeNumber;
    private String department;
    private int hour;
    private int rank;


}
