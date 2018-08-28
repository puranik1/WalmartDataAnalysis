package analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Springboot Application, deletes old output files and runs the application
 */
@SpringBootApplication
@EnableScheduling
public class Application {

    public static void main(String[] args) {

        String folderPath = "C:\\Walmart\\";

        String registerStatusFilePath = folderPath + "OutputStreamRegisterStatus.txt";

        File file = new File(registerStatusFilePath);
        try {
            boolean result = Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        registerStatusFilePath = folderPath + "OutputStreamItem.txt";

        file = new File(registerStatusFilePath);
        try {
            boolean result = Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        registerStatusFilePath = folderPath + "OutputStreamDept.txt";

        file = new File(registerStatusFilePath);
        try {
            boolean result = Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        registerStatusFilePath = folderPath + "deptdetails.txt";

        file = new File(registerStatusFilePath);
        try {
            boolean result = Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        registerStatusFilePath = folderPath + "itemdetails.txt";

        file = new File(registerStatusFilePath);
        try {
            boolean result = Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }


        SpringApplication.run(Application.class,args);
    }

}
