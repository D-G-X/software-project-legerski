package de.hft.licensing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class LicensingApplication {

    public static void main(final String[] args) {
        SpringApplication.run(LicensingApplication.class, args);
        System.out.println("--------------------------------");
        System.out.println("LicensingApplication started");
    }

}
