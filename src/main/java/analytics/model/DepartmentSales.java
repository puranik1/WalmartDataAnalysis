package analytics.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;


@Entity
public class DepartmentSales {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private int storeNumber;
    private String department;
    private double totalSales;
    private int hr;

    public DepartmentSales() {
    }

    public DepartmentSales(int storeNumber, String department, double totalSales, int hr) {
        this.storeNumber = storeNumber;
        this.department = department;
        this.totalSales = totalSales;
        this.hr = hr;
    }

    public int getHr() {
        return hr;
    }

    public void setHr(int hr) {
        this.hr = hr;
    }

    public int getStoreNumber() {
        return storeNumber;
    }

    public void setStoreNumber(int storeNumber) {
        this.storeNumber = storeNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public double getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(double totalSales) {
        this.totalSales = totalSales;
    }
}
