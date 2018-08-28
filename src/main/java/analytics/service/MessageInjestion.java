package analytics.service;

import analytics.model.Transactions;
import analytics.model.TransactionJson;
import com.google.gson.Gson;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
* Code to injest incoming messages (currently from the sample json file)
* */
@Service
public class MessageInjestion {

    Gson gson = new Gson();

    //Input file path (currently code looks at the sample json file provided)
    String fileName = "C:\\Walmart\\sales.json";

    //Method to read the input file and convert it to list of Transactions
    public List<Transactions> getTransactions(){

        System.out.println("Getting input from file and converting to list of Transactions : " + LocalDateTime.now());

        List<String> lines = Collections.emptyList();
        try{
            lines = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Retrieved "+lines.size()+" number of lines from input file : " + LocalDateTime.now());
        List<Transactions> transactions = new ArrayList<>();
        for(String line : lines){
            TransactionJson json = gson.fromJson(line,TransactionJson.class);
            Transactions tr = new Transactions();

            try{
                tr.setId(json.getId());
                tr.setOrderId(json.getOrder_id());
                tr.setOrderTime(json.getOrder_time());
                tr.setStoreNumber(json.getStore_number());
                tr.setDepartment(json.getDepartment());
                tr.setRegister(json.getRegister());
                double amt = 0;

                try{
                    amt = Double.parseDouble(json.getAmount());
                }catch (Exception e){
                }

                tr.setAmount(amt);
                tr.setUpc(json.getUpc());
                tr.setName(json.getName());
                tr.setDescription(json.getDescription());
                tr.setInsertedDate(LocalDateTime.now());
            }
            catch (Exception e){
                System.out.println("Error converting to POJO! : " + e);
            }
            transactions.add(tr);
        }

        System.out.println("Parsed "+lines.size()+" number of json lines to transactions : " +LocalDateTime.now());
        return transactions;

    }

}
