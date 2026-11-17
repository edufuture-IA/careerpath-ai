package pe.edu.upc.careerpath_ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CareerpathAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(CareerpathAiApplication.class, args);
    }
}