package analytics.model;

import java.math.BigInteger;


public class TransactionJson {

    private String id;
    private String order_id;
    private long order_time;
    private int store_number;
    private String department;
    private String register;
    private String amount;
    private String upc;
    private String name;
    private String description;

    public TransactionJson() {
    }

    public TransactionJson(String id, String order_id, long order_time, int store_number, String department, String register, String amount, String upc, String name, String description) {
        this.id = id;
        this.order_id = order_id;
        this.order_time = order_time;
        this.store_number = store_number;
        this.department = department;
        this.register = register;
        this.amount = amount;
        this.upc = upc;
        this.name = name;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public long getOrder_time() {
        return order_time;
    }

    public void setOrder_time(long order_time) {
        this.order_time = order_time;
    }

    public int getStore_number() {
        return store_number;
    }

    public void setStore_number(int store_number) {
        this.store_number = store_number;
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

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
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
