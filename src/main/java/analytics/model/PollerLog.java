package analytics.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class PollerLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;
    public long lastTransactionIdProcessed;

    public PollerLog() {
    }

    public PollerLog(long lastTransactionIdProcessed) {
        this.lastTransactionIdProcessed = lastTransactionIdProcessed;
    }

    public long getLastTransactionIdProcessed() {
        return lastTransactionIdProcessed;
    }

    public void setLastTransactionIdProcessed(long lastTransactionIdProcessed) {
        this.lastTransactionIdProcessed = lastTransactionIdProcessed;
    }
}
