package uk.gov.digital.ho.hocs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class CorrespondenceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CorrespondenceApplication.class, args);
    }
}
