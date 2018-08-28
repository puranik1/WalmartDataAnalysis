package analytics.model;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;


@Entity
public class RegisterStatusMap implements Serializable{

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    public int storeNumber;
    public int register;

    public long lastTransactionTime;

    public RegisterStatusMap() {
    }

    public RegisterStatusMap(int storeNumber, int register, long lastTransactionTime) {
        this.storeNumber = storeNumber;
        this.register = register;
        this.lastTransactionTime = lastTransactionTime;
    }

    public int getStoreNumber() {
        return storeNumber;
    }

    public void setStoreNumber(int storeNumber) {
        this.storeNumber = storeNumber;
    }

    public int getRegister() {
        return register;
    }

    public void setRegister(int register) {
        this.register = register;
    }

    public long getLastTransactionTime() {
        return lastTransactionTime;
    }

    public void setLastTransactionTime(long lastTransactionTime) {
        this.lastTransactionTime = lastTransactionTime;
    }
}
