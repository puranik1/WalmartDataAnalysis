package analytics.service;

import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
/*
* Code to Write output stream for register status
* */
@Service
public class OutputStream {

    //Method to write output streams for register status
    public void writeRegisterStatus(String message){

        System.out.println("Writing register status messages : " + LocalDateTime.now());

        String filePath = "C:\\Walmart\\OutputStreamRegisterStatus.txt";

        PrintWriter out = null;
        BufferedWriter bufWriter;

        try{
            bufWriter =
                    Files.newBufferedWriter(
                            Paths.get(filePath),
                            Charset.forName("UTF8"),
                            StandardOpenOption.WRITE,
                            StandardOpenOption.APPEND,
                            StandardOpenOption.CREATE);
            out = new PrintWriter(bufWriter, true);
        }catch(IOException e){
        }

        out.println(message);
        out.close();


    }

}
