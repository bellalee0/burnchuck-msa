package com.example.burnchuck.domain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.example.burnchuck")
@EntityScan(basePackages = "com.example.burnchuck.common.entity")
@EnableJpaRepositories(basePackages = {
    "com.example.burnchuck.domain.meeting.repository",
    "com.example.burnchuck.domain.meetingLike.repository",
    "com.example.burnchuck.domain.category.repository"
})
public class MeetingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MeetingServiceApplication.class, args);
    }
}
