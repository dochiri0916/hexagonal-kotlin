package com.dochiri.hexagonal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class HexagonalApplication {

    static void main(String[] args) {
        SpringApplication.run(HexagonalApplication.class, args);
    }

}
