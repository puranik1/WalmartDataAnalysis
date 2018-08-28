package analytics.service;

import analytics.model.Transactions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import analytics.repository.RegisterStatusMapRepository;
import analytics.repository.TransactionRepository;

import java.time.LocalDateTime;
import java.util.List;

/*
*
* Code to process messages and save to DB in Transactions table
* */
@Service
public class RegisterStatus {

    @Autowired
    MessageInjestion messageInjestion;

    @Autowired
    TransactionRepository transactionRepository;

    //Method to Injest messages
    public void processMessages(){

        System.out.println("Started processing messages : " + LocalDateTime.now());

        List<Transactions> transactions = messageInjestion.getTransactions();

        for(Transactions t : transactions) {
            transactionRepository.save(t);
        }
        System.out.println("Completed processing messages : " + LocalDateTime.now());

    }
}
