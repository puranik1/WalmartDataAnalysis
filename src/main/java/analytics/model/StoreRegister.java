package analytics.model;


public class StoreRegister {
    int store;
    String register;
    long lastTransaction;

    public StoreRegister() {
    }

    public StoreRegister(int store, String register, long lastTransaction) {
        this.store = store;
        this.register = register;
        this.lastTransaction = lastTransaction;
    }

    public int getStore() {
        return store;
    }

    public void setStore(int store) {
        this.store = store;
    }

    public String getRegister() {
        return register;
    }

    public void setRegister(String register) {
        this.register = register;
    }

    public long getLastTransaction() {
        return lastTransaction;
    }

    public void setLastTransaction(long lastTransaction) {
        this.lastTransaction = lastTransaction;
    }
}
