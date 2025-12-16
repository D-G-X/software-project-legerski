package de.hft.licensing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
public class LicensingApplication {

    public static void main(final String[] args) {
        SpringApplication.run(LicensingApplication.class, args);
    }

}
