package analytics.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public class CurrentRegisterStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private int store;
    private String register;
    private String lastStatus;
    private String currentStatus;
    private long lastTransactionDate;
    private UUID processId;
    private int isStatusChangeReported;

    public CurrentRegisterStatus() {
    }

    public CurrentRegisterStatus(int store, String register, String lastStatus, String currentStatus, long lastTransactionDate, UUID processId, int isStatusChangeReported) {
        this.store = store;
        this.register = register;
        this.lastStatus = lastStatus;
        this.currentStatus = currentStatus;
        this.lastTransactionDate = lastTransactionDate;
        this.processId = processId;
        this.isStatusChangeReported = isStatusChangeReported;
    }

    public CurrentRegisterStatus(int store, String register) {
        this.store = store;
        this.register = register;
    }

    public int getIsStatusChangeReported() {
        return isStatusChangeReported;
    }

    public void setIsStatusChangeReported(int isStatusChangeReported) {
        this.isStatusChangeReported = isStatusChangeReported;
    }

    public String getLastStatus() {
        return lastStatus;
    }

    public void setLastStatus(String lastStatus) {
        this.lastStatus = lastStatus;
    }

    public UUID getProcessId() {
        return processId;
    }

    public void setProcessId(UUID processId) {
        this.processId = processId;
    }

    public long getLastTransactionDate() {
        return lastTransactionDate;
    }

    public void setLastTransactionDate(long lastTransactionDate) {
        this.lastTransactionDate = lastTransactionDate;
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

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }
}
