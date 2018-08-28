package analytics.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Transactions")
public class Transactions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long transactionId;

    private String id;
    private String orderId;
    private long orderTime;
    private int storeNumber;
    private String department;
    private String register;
    private double amount;
    private String upc;
    private String name;
    private String description;
    private LocalDateTime insertedDate;


    public Transactions() {
    }

    public Transactions(String id, String orderId, long orderTime, int storeNumber, String department, String register, double amount, String upc, String name, String description, LocalDateTime insertedDate) {
        this.id = id;
        this.orderId = orderId;
        this.orderTime = orderTime;
        this.storeNumber = storeNumber;
        this.department = department;
        this.register = register;
        this.amount = amount;
        this.upc = upc;
        this.name = name;
        this.description = description;
        this.insertedDate = insertedDate;
    }

    public LocalDateTime getInsertedDate() {
        return insertedDate;
    }

    public void setInsertedDate(LocalDateTime insertedDate) {
        this.insertedDate = insertedDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public long getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(long orderTime) {
        this.orderTime = orderTime;
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

    public String getRegister() {
        return register;
    }

    public void setRegister(String register) {
        this.register = register;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getUpc() {
        return upc;
    }

    public void setUpc(String upc) {
        this.upc = upc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
