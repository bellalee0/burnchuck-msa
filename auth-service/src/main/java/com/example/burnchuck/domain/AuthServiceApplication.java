package com.example.burnchuck.domain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.example.burnchuck")
@EntityScan(basePackages = "com.example.burnchuck.common.entity")
@EnableJpaRepositories(basePackages = "com.example.burnchuck.domain.auth.repository")
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
