package com.iitp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class IitpApplication {

    public static void main(String[] args) {
        SpringApplication.run(IitpApplication.class, args);
    }

}
