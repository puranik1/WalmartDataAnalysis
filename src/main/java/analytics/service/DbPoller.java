package analytics.service;

import analytics.model.*;
import analytics.repository.DepartmentSalesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import analytics.repository.CurrentRegisterStatusRepository;
import analytics.repository.PollerLogRepository;
import analytics.repository.TransactionRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

/*
* Code logic:
* fetchStatusForStoreRegister() method polls the DB periodically
* Assumption : Continuous flow of messages(in form of transaction) will be coming in real time from every register which is active
* If we receive a transaction from a register, it means the register is active
* If we do not get transaction for a certain register for 'x' number of seconds, that means the register has stopped taking transactions
* I have maintained couple of tables in DB for keeping track of current status for all registers:
*   Transactions -> Store for all transactions
*   CurrentRegisterStatus -> Provides current status of every register
*   PollerLog -> keeps track of the last processed TransactionId
*   DepartmentSales -> Stores per department total sale every hour
*
* The code looks at a batch of transactions from last run upto this run
* */
@Service
public class DbPoller {

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    PollerLogRepository pollerLogRepository;

    @Autowired
    CurrentRegisterStatusRepository currentRegisterStatusRepository;

    @Autowired
    OutputStream outputStream;


    @Scheduled(fixedDelay = 30000) //Time period between transactions in milliseconds
    public void fetchStatusForStoreRegister(){

        UUID processId = UUID.randomUUID();

        System.out.println("Running Db polling started for getting register status: " + LocalDateTime.now());

        int registerMaxIdleTime = 300; //time in seconds

        long maxTransactionId = 0;
        if(transactionRepository.findMaxTransactionId() !=null){
            maxTransactionId  = transactionRepository.findMaxTransactionId();
        }

        List<PollerLog> pollerLogs =  pollerLogRepository.findAll();
        long lastTransactionIdProcessed = 0;
        if(pollerLogs !=null && pollerLogs.size() > 0){
            lastTransactionIdProcessed = pollerLogs.get(0).getLastTransactionIdProcessed();
        }

        List<Object[]> storeRegistersObjs = transactionRepository.findStoreNumberAndRegisterWhereTransactionIdGreaterThan(lastTransactionIdProcessed,maxTransactionId);
        List<StoreRegister> storeRegisters = new ArrayList<>();
        for(Object[] obj : storeRegistersObjs){
            StoreRegister reg = new StoreRegister((Integer)obj[0],obj[1].toString(),Long.parseLong(obj[2].toString()));
            storeRegisters.add(reg);
        }

        System.out.println("Total number of distinct registers we received transactions from since last batch run: " + storeRegisters.size());

        for(StoreRegister storeRegister : storeRegisters){

            CurrentRegisterStatus temp = currentRegisterStatusRepository.findByStoreAndRegister(storeRegister.getStore(),storeRegister.getRegister());
            //If we have a record for this register in our table
            if(temp != null){
                //Since we received a new transaction from this register, we update the current status to Accepting and set the flag
                // setIsStatusChangeReported = 0 which is used down the line to see what needs to be put to the output stream
                if(temp.getCurrentStatus().equals("Not Accepting")){

                    temp.setLastStatus("Not Accepting");
                    temp.setCurrentStatus("Accepting");
                    temp.setLastTransactionDate(storeRegister.getLastTransaction());
                    temp.setProcessId(processId);
                    temp.setIsStatusChangeReported(0);
                    currentRegisterStatusRepository.save(temp);

                }
                else{
                    temp.setLastStatus("Accepting");
                    temp.setCurrentStatus("Accepting");
                    temp.setLastTransactionDate(storeRegister.getLastTransaction());
                    temp.setIsStatusChangeReported(1);
                    currentRegisterStatusRepository.save(temp);

                }


             }
            else {
                //If we dont have this register(getting transaction for the first time), we add a entry
                currentRegisterStatusRepository.save(new CurrentRegisterStatus(storeRegister.getStore(),storeRegister.getRegister(),"Not Accepting","Accepting",storeRegister.getLastTransaction(),processId,0));
            }

        }

        Set<CurrentRegisterStatus> currentRegisterStatuses = new HashSet<>(currentRegisterStatusRepository.findAll());
        System.out.println("Total number of register statuses in DB: " + currentRegisterStatuses.size());

        for(CurrentRegisterStatus currentRegisterStatus : currentRegisterStatuses){
            //If the change was done in this particular process run and t was not already reported, we report the change
            if(currentRegisterStatus.getCurrentStatus().equals("Accepting") && currentRegisterStatus.getLastStatus().equals("Not Accepting") && currentRegisterStatus.getProcessId().equals(processId) && currentRegisterStatus.getIsStatusChangeReported() == 0){
                outputStream.writeRegisterStatus(currentRegisterStatus.getStore()+"-"+currentRegisterStatus.getRegister()+" "+ LocalDateTime.now().toString()+": Accepting");
                currentRegisterStatus.setIsStatusChangeReported(1);
                currentRegisterStatusRepository.save(currentRegisterStatus);

            }
            else if(currentRegisterStatus.getCurrentStatus().equals("Not Accepting") && currentRegisterStatus.getLastStatus().equals("Accepting") && currentRegisterStatus.getProcessId().equals(processId)  && currentRegisterStatus.getIsStatusChangeReported() == 0){
                outputStream.writeRegisterStatus(currentRegisterStatus.getStore()+"-"+currentRegisterStatus.getRegister()+" "+LocalDateTime.now().toString()+": Not Accepting");
                currentRegisterStatus.setIsStatusChangeReported(1);
                currentRegisterStatusRepository.save(currentRegisterStatus);

            }
            //If the last transaction for a counter has been before the idelTimeOut period(configurable), we mark the register status as Not Accepting
            else if((Instant.now().toEpochMilli() - currentRegisterStatus.getLastTransactionDate()) > registerMaxIdleTime && !currentRegisterStatus.getProcessId().equals(processId)){
                outputStream.writeRegisterStatus(currentRegisterStatus.getStore()+"-"+currentRegisterStatus.getRegister()+" "+LocalDateTime.now().toString()+": Not Accepting");


                currentRegisterStatus.setLastStatus("Accepting");
                currentRegisterStatus.setCurrentStatus("Not Accepting");
                currentRegisterStatus.setProcessId(processId);
                currentRegisterStatus.setIsStatusChangeReported(1);
                currentRegisterStatusRepository.save(currentRegisterStatus);

            }


        }

        pollerLogRepository.deleteAll();
        pollerLogRepository.save(new PollerLog(maxTransactionId));

        System.out.println("Running Db polling completed : " + LocalDateTime.now());
    }

}
