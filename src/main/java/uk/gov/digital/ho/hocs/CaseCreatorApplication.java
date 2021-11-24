package uk.gov.digital.ho.hocs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class CaseCreatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CaseCreatorApplication.class, args);
    }

}
